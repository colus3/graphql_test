package com.test.graphql_test.controller

import com.test.graphql_test.config.GraphQlScalarConfig
import com.test.graphql_test.dto.response.PageInfo
import com.test.graphql_test.dto.response.ProductPage
import com.test.graphql_test.dto.response.ProductResponse
import com.test.graphql_test.service.ProductService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.math.BigDecimal

@GraphQlTest(ProductController::class)
@Import(GraphQlScalarConfig::class)
class ProductControllerTest {

    @Autowired
    lateinit var graphQlTester: GraphQlTester

    @MockitoBean
    lateinit var productService: ProductService

    private fun productResponse(id: Long = 1L) = ProductResponse(
        id = id,
        name = "테스트 상품",
        price = BigDecimal("15000"),
        stock = 100,
        categoryId = 1L,
        categoryName = "전자기기",
        createdAt = "2024-01-01T00:00:00"
    )

    @Test
    fun `product - id로 상품 단건 조회`() {
        whenever(productService.getProduct(1L)).thenReturn(productResponse())

        graphQlTester.document("""
            query {
                product(id: "1") {
                    id
                    name
                    price
                    stock
                    categoryId
                    categoryName
                }
            }
        """).execute()
            .path("product.id").entity(String::class.java).isEqualTo("1")
            .path("product.name").entity(String::class.java).isEqualTo("테스트 상품")
            .path("product.stock").entity(Int::class.java).isEqualTo(100)
            .path("product.categoryName").entity(String::class.java).isEqualTo("전자기기")
    }

    @Test
    fun `product - 존재하지 않는 id 조회 시 null 반환`() {
        whenever(productService.getProduct(999L)).thenReturn(null)

        graphQlTester.document("""
            query {
                product(id: "999") {
                    id
                    name
                }
            }
        """).execute()
            .path("product").valueIsNull()
    }

    @Test
    fun `products - 필터 없이 전체 상품 목록 조회`() {
        val page = PageInfo(totalElements = 2L, totalPages = 1, currentPage = 0, pageSize = 20)
        whenever(productService.getProducts(anyOrNull(), anyOrNull())).thenReturn(
            ProductPage(
                content = listOf(productResponse(1L), productResponse(2L)),
                pageInfo = page
            )
        )

        graphQlTester.document("""
            query {
                products {
                    content {
                        id
                        name
                        price
                        stock
                    }
                    pageInfo {
                        totalElements
                        totalPages
                        currentPage
                        pageSize
                    }
                }
            }
        """).execute()
            .path("products.content").entityList(Any::class.java).hasSize(2)
            .path("products.content[0].name").entity(String::class.java).isEqualTo("테스트 상품")
            .path("products.pageInfo.totalElements").entity(Long::class.java).isEqualTo(2L)
    }

    @Test
    fun `products - 이름 필터로 상품 검색`() {
        val page = PageInfo(totalElements = 1L, totalPages = 1, currentPage = 0, pageSize = 20)
        whenever(productService.getProducts(anyOrNull(), anyOrNull())).thenReturn(
            ProductPage(content = listOf(productResponse()), pageInfo = page)
        )

        graphQlTester.document("""
            query {
                products(filter: { nameContains: "테스트" }) {
                    content {
                        id
                        name
                    }
                    pageInfo {
                        totalElements
                    }
                }
            }
        """).execute()
            .path("products.content[0].name").entity(String::class.java).isEqualTo("테스트 상품")
            .path("products.pageInfo.totalElements").entity(Long::class.java).isEqualTo(1L)
    }

    @Test
    fun `createProduct - 상품 생성`() {
        whenever(productService.createProduct(any())).thenReturn(productResponse())

        graphQlTester.document("""
            mutation {
                createProduct(input: {
                    name: "테스트 상품",
                    price: "15000",
                    stock: 100,
                    categoryId: 1
                }) {
                    id
                    name
                    price
                    stock
                    categoryId
                }
            }
        """).execute()
            .path("createProduct.id").entity(String::class.java).isEqualTo("1")
            .path("createProduct.name").entity(String::class.java).isEqualTo("테스트 상품")
            .path("createProduct.stock").entity(Int::class.java).isEqualTo(100)
    }
}