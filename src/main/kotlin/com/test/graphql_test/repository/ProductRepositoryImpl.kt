package com.test.graphql_test.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.test.graphql_test.dto.input.PageInput
import com.test.graphql_test.dto.input.ProductFilter
import com.test.graphql_test.dto.response.ProductResponse
import com.test.graphql_test.entity.Product
import com.test.graphql_test.entity.QCategory
import com.test.graphql_test.entity.QProduct
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : ProductRepositoryCustom {

    override fun findByFilter(filter: ProductFilter?, page: PageInput?): Pair<List<ProductResponse>, Long> {
        val product = QProduct.product
        val category = QCategory.category

        val predicate = BooleanBuilder()
        filter?.let { f ->
            f.nameContains?.let { predicate.and(product.name.containsIgnoreCase(it)) }
            f.minPrice?.let { predicate.and(product.price.goe(it)) }
            f.maxPrice?.let { predicate.and(product.price.loe(it)) }
            f.categoryId?.let { predicate.and(product.category.id.eq(it)) }
            f.minStock?.let { predicate.and(product.stock.goe(it)) }
        }

        val pageInput = page ?: PageInput()
        val offset = (pageInput.page * pageInput.size).toLong()

        val orderSpecifier = buildOrderSpecifier(pageInput, product)

        val results = queryFactory
            .selectFrom(product)
            .join(product.category, category).fetchJoin()
            .where(predicate)
            .orderBy(orderSpecifier)
            .offset(offset)
            .limit(pageInput.size.toLong())
            .fetch()
            .map { it.toResponse() }

        val total = queryFactory
            .select(product.count())
            .from(product)
            .where(predicate)
            .fetchOne() ?: 0L

        return Pair(results, total)
    }

    override fun findByIdWithCategory(id: Long): ProductResponse? {
        val product = QProduct.product
        val category = QCategory.category

        return queryFactory
            .selectFrom(product)
            .join(product.category, category).fetchJoin()
            .where(product.id.eq(id))
            .fetchOne()
            ?.toResponse()
    }

    private fun buildOrderSpecifier(page: PageInput, product: QProduct): OrderSpecifier<*> {
        val direction = if (page.sortDir == com.test.graphql_test.dto.input.SortDirection.DESC) Order.DESC else Order.ASC
        val sortBy = page.sortBy ?: "id"
        val path = PathBuilder(Product::class.java, "product")
        return OrderSpecifier(direction, path.getComparable(sortBy, Comparable::class.java))
    }

    private fun Product.toResponse() = ProductResponse(
        id = id,
        name = name,
        price = price,
        stock = stock,
        categoryId = category.id,
        categoryName = category.name,
        createdAt = createdAt.toString()
    )
}
