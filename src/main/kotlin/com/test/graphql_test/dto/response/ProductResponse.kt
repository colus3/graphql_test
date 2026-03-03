package com.test.graphql_test.dto.response

import java.math.BigDecimal

data class ProductResponse(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val stock: Int,
    val categoryId: Long,
    val categoryName: String,
    val createdAt: String
)
