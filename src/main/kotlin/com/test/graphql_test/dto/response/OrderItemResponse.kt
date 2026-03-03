package com.test.graphql_test.dto.response

import java.math.BigDecimal

data class OrderItemResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal
)
