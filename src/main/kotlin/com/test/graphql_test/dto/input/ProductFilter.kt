package com.test.graphql_test.dto.input

import java.math.BigDecimal

data class ProductFilter(
    val nameContains: String? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val categoryId: Long? = null,
    val minStock: Int? = null
)
