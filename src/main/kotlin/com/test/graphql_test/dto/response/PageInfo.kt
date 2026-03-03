package com.test.graphql_test.dto.response

data class PageInfo(
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)
