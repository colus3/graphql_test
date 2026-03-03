package com.test.graphql_test.dto.input

data class CreateOrderInput(
    val userId: Long,
    val items: List<OrderItemInput>
)
