package com.test.graphql_test.dto.input

data class PageInput(
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String? = null,
    val sortDir: SortDirection = SortDirection.ASC
)
