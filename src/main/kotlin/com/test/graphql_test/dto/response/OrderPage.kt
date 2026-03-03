package com.test.graphql_test.dto.response

data class OrderPage(
    val content: List<OrderResponse>,
    val pageInfo: PageInfo
)
