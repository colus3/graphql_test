package com.test.graphql_test.dto.input

data class OrderFilter(
    val userId: Long? = null,
    val status: OrderStatus? = null,
    val fromDate: String? = null,
    val toDate: String? = null
)
