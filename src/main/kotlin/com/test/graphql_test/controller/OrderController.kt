package com.test.graphql_test.controller

import com.test.graphql_test.dto.input.CreateOrderInput
import com.test.graphql_test.dto.input.OrderFilter
import com.test.graphql_test.dto.input.OrderStatus
import com.test.graphql_test.dto.input.PageInput
import com.test.graphql_test.dto.response.OrderPage
import com.test.graphql_test.dto.response.OrderResponse
import com.test.graphql_test.service.OrderService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class OrderController(private val orderService: OrderService) {

    @QueryMapping
    fun orders(
        @Argument filter: OrderFilter?,
        @Argument page: PageInput?
    ): OrderPage = orderService.getOrders(filter, page)

    @QueryMapping
    fun order(@Argument id: Long): OrderResponse? = orderService.getOrder(id)

    @MutationMapping
    fun createOrder(@Argument input: CreateOrderInput): OrderResponse =
        orderService.createOrder(input)

    @MutationMapping
    fun updateOrderStatus(
        @Argument id: Long,
        @Argument status: OrderStatus
    ): OrderResponse = orderService.updateOrderStatus(id, status)
}
