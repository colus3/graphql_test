package com.test.graphql_test.service

import com.test.graphql_test.dto.input.CreateProductInput
import com.test.graphql_test.dto.input.PageInput
import com.test.graphql_test.dto.input.ProductFilter
import com.test.graphql_test.dto.response.PageInfo
import com.test.graphql_test.dto.response.ProductPage
import com.test.graphql_test.dto.response.ProductResponse
import com.test.graphql_test.entity.Product
import com.test.graphql_test.repository.CategoryRepository
import com.test.graphql_test.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ceil

@Service
@Transactional(readOnly = true)
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {

    fun getProducts(filter: ProductFilter?, page: PageInput?): ProductPage {
        val pageInput = page ?: PageInput()
        val (content, total) = productRepository.findByFilter(filter, pageInput)
        val totalPages = if (pageInput.size > 0) ceil(total.toDouble() / pageInput.size).toInt() else 0
        return ProductPage(
            content = content,
            pageInfo = PageInfo(
                totalElements = total,
                totalPages = totalPages,
                currentPage = pageInput.page,
                pageSize = pageInput.size
            )
        )
    }

    fun getProduct(id: Long): ProductResponse? = productRepository.findByIdWithCategory(id)

    @Transactional
    fun createProduct(input: CreateProductInput): ProductResponse {
        val category = categoryRepository.findById(input.categoryId)
            .orElseThrow { IllegalArgumentException("Category not found: ${input.categoryId}") }

        val product = productRepository.save(
            Product(
                name = input.name,
                price = input.price,
                stock = input.stock,
                category = category
            )
        )

        return productRepository.findByIdWithCategory(product.id)!!
    }
}
