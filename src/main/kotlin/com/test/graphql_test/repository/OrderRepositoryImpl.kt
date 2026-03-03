package com.test.graphql_test.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.test.graphql_test.dto.input.OrderFilter
import com.test.graphql_test.dto.input.PageInput
import com.test.graphql_test.dto.response.OrderItemResponse
import com.test.graphql_test.dto.response.OrderResponse
import com.test.graphql_test.entity.Order
import com.test.graphql_test.entity.QOrder
import com.test.graphql_test.entity.QOrderItem
import com.test.graphql_test.entity.QProduct
import com.test.graphql_test.entity.QUser
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
class OrderRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : OrderRepositoryCustom {

    override fun findByFilter(filter: OrderFilter?, page: PageInput?): Pair<List<OrderResponse>, Long> {
        val order = QOrder.order
        val user = QUser.user
        val orderItem = QOrderItem.orderItem
        val product = QProduct.product

        val predicate = BooleanBuilder()
        filter?.let { f ->
            f.userId?.let { predicate.and(order.user.id.eq(it)) }
            f.status?.let { predicate.and(order.status.eq(it.name)) }
            f.fromDate?.let {
                val from = LocalDate.parse(it).atStartOfDay()
                predicate.and(order.createdAt.goe(from))
            }
            f.toDate?.let {
                val to = LocalDate.parse(it).atTime(23, 59, 59)
                predicate.and(order.createdAt.loe(to))
            }
        }

        val pageInput = page ?: PageInput()
        val offset = (pageInput.page * pageInput.size).toLong()

        val orderIds = queryFactory
            .select(order.id)
            .from(order)
            .where(predicate)
            .orderBy(order.id.asc())
            .offset(offset)
            .limit(pageInput.size.toLong())
            .fetch()

        val total = queryFactory
            .select(order.count())
            .from(order)
            .where(predicate)
            .fetchOne() ?: 0L

        if (orderIds.isEmpty()) return Pair(emptyList(), total)

        val orders = queryFactory
            .selectFrom(order)
            .join(order.user, user).fetchJoin()
            .join(order.items, orderItem).fetchJoin()
            .join(orderItem.product, product).fetchJoin()
            .where(order.id.`in`(orderIds))
            .orderBy(order.id.asc())
            .distinct()
            .fetch()

        return Pair(orders.map { it.toResponse() }, total)
    }

    override fun findByIdWithDetails(id: Long): OrderResponse? {
        val order = QOrder.order
        val user = QUser.user
        val orderItem = QOrderItem.orderItem
        val product = QProduct.product

        return queryFactory
            .selectFrom(order)
            .join(order.user, user).fetchJoin()
            .join(order.items, orderItem).fetchJoin()
            .join(orderItem.product, product).fetchJoin()
            .where(order.id.eq(id))
            .distinct()
            .fetchOne()
            ?.toResponse()
    }

    private fun Order.toResponse() = OrderResponse(
        id = id,
        status = status,
        createdAt = createdAt.toString(),
        userId = user.id,
        userName = user.name,
        userEmail = user.email,
        items = items.map { item ->
            OrderItemResponse(
                id = item.id,
                productId = item.product.id,
                productName = item.product.name,
                quantity = item.quantity,
                unitPrice = item.unitPrice
            )
        }
    )
}
