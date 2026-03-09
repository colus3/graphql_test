package com.test.graphql_test.config

import graphql.scalars.ExtendedScalars
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer

@Configuration
class GraphQlScalarConfig {

    @Bean
    fun runtimeWiringConfigurer(): RuntimeWiringConfigurer = RuntimeWiringConfigurer { builder ->
        builder
            .scalar(ExtendedScalars.GraphQLBigDecimal)
            .scalar(ExtendedScalars.GraphQLLong)
    }
}
