package com.test.graphql_test.dto.response

data class OrderResponse(
    val id: Long,
    val status: String,
    val createdAt: String,
    val userId: Long,
    val userName: String,
    val userEmail: String,
    val items: List<OrderItemResponse>
)
