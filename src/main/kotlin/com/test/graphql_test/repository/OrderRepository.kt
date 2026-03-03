package com.test.graphql_test.repository

import com.test.graphql_test.entity.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long>, OrderRepositoryCustom
