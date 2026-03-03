package com.test.graphql_test.repository

import com.test.graphql_test.dto.input.OrderFilter
import com.test.graphql_test.dto.input.PageInput
import com.test.graphql_test.dto.response.OrderResponse

interface OrderRepositoryCustom {
    fun findByFilter(filter: OrderFilter?, page: PageInput?): Pair<List<OrderResponse>, Long>
    fun findByIdWithDetails(id: Long): OrderResponse?
}
