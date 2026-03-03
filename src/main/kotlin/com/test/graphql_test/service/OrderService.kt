package com.test.graphql_test.service

import com.test.graphql_test.dto.input.CreateOrderInput
import com.test.graphql_test.dto.input.OrderFilter
import com.test.graphql_test.dto.input.OrderStatus
import com.test.graphql_test.dto.input.PageInput
import com.test.graphql_test.dto.response.OrderPage
import com.test.graphql_test.dto.response.OrderResponse
import com.test.graphql_test.dto.response.PageInfo
import com.test.graphql_test.entity.Order
import com.test.graphql_test.entity.OrderItem
import com.test.graphql_test.repository.OrderRepository
import com.test.graphql_test.repository.ProductRepository
import com.test.graphql_test.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ceil

@Service
@Transactional(readOnly = true)
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) {

    fun getOrders(filter: OrderFilter?, page: PageInput?): OrderPage {
        val pageInput = page ?: PageInput()
        val (content, total) = orderRepository.findByFilter(filter, pageInput)
        val totalPages = if (pageInput.size > 0) ceil(total.toDouble() / pageInput.size).toInt() else 0
        return OrderPage(
            content = content,
            pageInfo = PageInfo(
                totalElements = total,
                totalPages = totalPages,
                currentPage = pageInput.page,
                pageSize = pageInput.size
            )
        )
    }

    fun getOrder(id: Long): OrderResponse? = orderRepository.findByIdWithDetails(id)

    @Transactional
    fun createOrder(input: CreateOrderInput): OrderResponse {
        val user = userRepository.findById(input.userId)
            .orElseThrow { IllegalArgumentException("User not found: ${input.userId}") }

        val order = Order(user = user)

        input.items.forEach { itemInput ->
            val product = productRepository.findById(itemInput.productId)
                .orElseThrow { IllegalArgumentException("Product not found: ${itemInput.productId}") }
            require(product.stock >= itemInput.quantity) {
                "Insufficient stock for product: ${product.name}"
            }
            product.stock -= itemInput.quantity
            order.items.add(
                OrderItem(
                    order = order,
                    product = product,
                    quantity = itemInput.quantity,
                    unitPrice = product.price
                )
            )
        }

        val saved = orderRepository.save(order)
        return orderRepository.findByIdWithDetails(saved.id)!!
    }

    @Transactional
    fun updateOrderStatus(id: Long, status: OrderStatus): OrderResponse {
        val order = orderRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Order not found: $id") }
        order.status = status.name
        return orderRepository.findByIdWithDetails(id)!!
    }
}
