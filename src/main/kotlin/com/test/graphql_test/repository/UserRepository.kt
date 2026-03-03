package com.test.graphql_test.repository

import com.test.graphql_test.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>
