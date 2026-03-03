package com.test.graphql_test.dto.response

data class ProductPage(
    val content: List<ProductResponse>,
    val pageInfo: PageInfo
)
