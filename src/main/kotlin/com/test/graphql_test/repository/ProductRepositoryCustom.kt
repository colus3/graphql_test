package com.test.graphql_test.repository

import com.test.graphql_test.dto.input.PageInput
import com.test.graphql_test.dto.input.ProductFilter
import com.test.graphql_test.dto.response.ProductResponse

interface ProductRepositoryCustom {
    fun findByFilter(filter: ProductFilter?, page: PageInput?): Pair<List<ProductResponse>, Long>
    fun findByIdWithCategory(id: Long): ProductResponse?
}
