package com.test.graphql_test.repository

import com.test.graphql_test.entity.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long>
