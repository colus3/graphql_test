package com.test.graphql_test.controller

import com.test.graphql_test.dto.input.CreateProductInput
import com.test.graphql_test.dto.input.PageInput
import com.test.graphql_test.dto.input.ProductFilter
import com.test.graphql_test.dto.response.ProductPage
import com.test.graphql_test.dto.response.ProductResponse
import com.test.graphql_test.service.ProductService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ProductController(private val productService: ProductService) {

    @QueryMapping
    fun products(
        @Argument filter: ProductFilter?,
        @Argument page: PageInput?
    ): ProductPage = productService.getProducts(filter, page)

    @QueryMapping
    fun product(@Argument id: Long): ProductResponse? = productService.getProduct(id)

    @MutationMapping
    fun createProduct(@Argument input: CreateProductInput): ProductResponse =
        productService.createProduct(input)
}
