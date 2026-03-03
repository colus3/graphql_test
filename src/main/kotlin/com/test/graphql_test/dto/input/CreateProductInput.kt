package com.test.graphql_test.dto.input

import java.math.BigDecimal

data class CreateProductInput(
    val name: String,
    val price: BigDecimal,
    val stock: Int,
    val categoryId: Long
)
