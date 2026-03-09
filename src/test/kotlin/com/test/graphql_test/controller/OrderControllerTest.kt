package com.test.graphql_test.controller

import com.test.graphql_test.config.GraphQlScalarConfig
import com.test.graphql_test.dto.input.CreateOrderInput
import com.test.graphql_test.dto.input.OrderItemInput
import com.test.graphql_test.dto.input.OrderStatus
import com.test.graphql_test.dto.response.OrderItemResponse
import com.test.graphql_test.dto.response.OrderPage
import com.test.graphql_test.dto.response.OrderResponse
import com.test.graphql_test.dto.response.PageInfo
import com.test.graphql_test.service.OrderService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.math.BigDecimal

@GraphQlTest(OrderController::class)
@Import(GraphQlScalarConfig::class)
class OrderControllerTest {

    @Autowired
    lateinit var graphQlTester: GraphQlTester

    @MockitoBean
    lateinit var orderService: OrderService

    private fun orderResponse(id: Long = 1L) = OrderResponse(
        id = id,
        status = "PENDING",
        createdAt = "2024-01-01T00:00:00",
        userId = 1L,
        userName = "테스트 유저",
        userEmail = "test@example.com",
        items = listOf(
            OrderItemResponse(
                id = 1L,
                productId = 1L,
                productName = "상품A",
                quantity = 2,
                unitPrice = BigDecimal("10000")
            )
        )
    )

    @Test
    fun `order - id로 주문 단건 조회`() {
        whenever(orderService.getOrder(1L)).thenReturn(orderResponse())

        graphQlTester.document("""
            query {
                order(id: "1") {
                    id
                    status
                    userId
                    userName
                    userEmail
                    items {
                        id
                        productId
                        productName
                        quantity
                        unitPrice
                    }
                }
            }
        """).execute()
            .path("order.id").entity(String::class.java).isEqualTo("1")
            .path("order.status").entity(String::class.java).isEqualTo("PENDING")
            .path("order.userName").entity(String::class.java).isEqualTo("테스트 유저")
            .path("order.items[0].productName").entity(String::class.java).isEqualTo("상품A")
            .path("order.items[0].quantity").entity(Int::class.java).isEqualTo(2)
    }

    @Test
    fun `order - 존재하지 않는 id 조회 시 null 반환`() {
        whenever(orderService.getOrder(999L)).thenReturn(null)

        graphQlTester.document("""
            query {
                order(id: "999") {
                    id
                    status
                }
            }
        """).execute()
            .path("order").valueIsNull()
    }

    @Test
    fun `orders - 필터 없이 전체 주문 목록 조회`() {
        val page = PageInfo(totalElements = 1L, totalPages = 1, currentPage = 0, pageSize = 20)
        whenever(orderService.getOrders(anyOrNull(), anyOrNull())).thenReturn(
            OrderPage(content = listOf(orderResponse()), pageInfo = page)
        )

        graphQlTester.document("""
            query {
                orders {
                    content {
                        id
                        status
                        userName
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
            .path("orders.content[0].status").entity(String::class.java).isEqualTo("PENDING")
            .path("orders.pageInfo.totalElements").entity(Long::class.java).isEqualTo(1L)
            .path("orders.pageInfo.totalPages").entity(Int::class.java).isEqualTo(1)
    }

    @Test
    fun `createOrder - 주문 생성`() {
        whenever(orderService.createOrder(any())).thenReturn(orderResponse())

        graphQlTester.document("""
            mutation {
                createOrder(input: {
                    userId: 1,
                    items: [{ productId: 1, quantity: 2 }]
                }) {
                    id
                    status
                    userId
                    items {
                        productId
                        quantity
                        unitPrice
                    }
                }
            }
        """).execute()
            .path("createOrder.id").entity(String::class.java).isEqualTo("1")
            .path("createOrder.status").entity(String::class.java).isEqualTo("PENDING")
            .path("createOrder.items[0].quantity").entity(Int::class.java).isEqualTo(2)
    }

    @Test
    fun `updateOrderStatus - 주문 상태 변경`() {
        whenever(orderService.updateOrderStatus(eq(1L), eq(OrderStatus.SHIPPED)))
            .thenReturn(orderResponse().copy(status = "SHIPPED"))

        graphQlTester.document("""
            mutation {
                updateOrderStatus(id: "1", status: SHIPPED) {
                    id
                    status
                }
            }
        """).execute()
            .path("updateOrderStatus.id").entity(String::class.java).isEqualTo("1")
            .path("updateOrderStatus.status").entity(String::class.java).isEqualTo("SHIPPED")
    }
}