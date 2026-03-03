# Spring GraphQL + QueryDSL E-Commerce API

Spring Boot 4.0.3 + Kotlin으로 구현한 GraphQL API 학습 프로젝트입니다.
정규화된 MySQL 테이블을 QueryDSL로 조인하여 프론트엔드가 필요한 필드만 선택적으로 조회할 수 있는 GraphQL API를 제공합니다.

---

## 기술 스택

| 항목 | 버전 |
|------|------|
| Kotlin | 2.2.21 |
| Spring Boot | 4.0.3 |
| Java | 21 |
| Spring GraphQL | Boot 관리 버전 |
| Spring Data JPA | Boot 관리 버전 |
| QueryDSL | 5.1.0 (jakarta) |
| MySQL | 8.0 |

---

## 핵심 아키텍처

### 데이터 흐름

DB는 정규화된 5개 테이블로 구성되어 있고, QueryDSL로 조인하여 GraphQL 응답 타입으로 변환합니다.

```
DB 테이블 (정규화)                   GraphQL 응답 타입 (평탄화)
─────────────────────────────────────────────────────────────────
users.name         ─────────────────> OrderResponse.userName
users.email        ─────────────────> OrderResponse.userEmail
categories.name    ─────────────────> ProductResponse.categoryName
products.name      ─────────────────> OrderItemResponse.productName
order_items.unit_price ──────────────> OrderItemResponse.unitPrice
```

### N+1 문제 해결

`OrderRepositoryImpl`은 `fetchJoin()`으로 4개 테이블을 **단일 쿼리**로 조회합니다.

```
orders ─── users
       └── order_items ─── products
```

### 동적 필터링

`ProductRepositoryImpl`은 `BooleanBuilder`로 null이 아닌 필드만 WHERE 조건에 추가합니다.

---

## 프로젝트 구조

```
src/main/kotlin/com/test/graphql_test/
├── entity/                   # JPA 엔티티 (DB 테이블 매핑)
│   ├── User.kt
│   ├── Category.kt
│   ├── Product.kt
│   ├── Order.kt
│   └── OrderItem.kt
├── repository/               # 데이터 접근 레이어
│   ├── UserRepository.kt
│   ├── CategoryRepository.kt
│   ├── ProductRepositoryCustom.kt
│   ├── ProductRepositoryImpl.kt  ← QueryDSL 동적 필터 + JOIN
│   ├── ProductRepository.kt
│   ├── OrderRepositoryCustom.kt
│   ├── OrderRepositoryImpl.kt    ← QueryDSL 4테이블 fetchJoin
│   └── OrderRepository.kt
├── config/
│   └── QueryDslConfig.kt     # JPAQueryFactory Bean 등록
├── service/
│   ├── ProductService.kt
│   └── OrderService.kt
├── controller/
│   ├── ProductController.kt  # @QueryMapping, @MutationMapping
│   └── OrderController.kt
└── dto/
    ├── input/                # GraphQL 입력 타입
    │   ├── PageInput.kt
    │   ├── ProductFilter.kt
    │   ├── OrderFilter.kt
    │   ├── CreateProductInput.kt
    │   ├── CreateOrderInput.kt
    │   ├── OrderItemInput.kt
    │   ├── OrderStatus.kt    (enum)
    │   └── SortDirection.kt  (enum)
    └── response/             # GraphQL 응답 타입
        ├── ProductResponse.kt    # categoryName 포함 (JOIN)
        ├── OrderResponse.kt      # userName, userEmail, items 포함 (JOIN)
        ├── OrderItemResponse.kt  # productName 포함 (JOIN)
        ├── PageInfo.kt
        ├── ProductPage.kt
        └── OrderPage.kt
```

---

## 시작하기

### 사전 요구사항

- Docker Desktop
- JDK 21
- Gradle (또는 `./gradlew` 래퍼 사용)

### 1단계: MySQL 실행

```bash
docker compose up -d
```

`docker/mysql/init.sql`이 자동으로 실행되어 테이블과 샘플 데이터가 생성됩니다.

- 사용자 2명 (Alice Kim, Bob Lee)
- 카테고리 3개 (Electronics, Books, Clothing)
- 상품 4개 (Wireless Headphones, Kotlin in Action, Spring Boot T-Shirt, USB-C Hub)
- 주문 2건, 주문 항목 4건

### 2단계: Q클래스 생성

QueryDSL은 엔티티를 분석하여 타입 안전한 Q클래스를 생성합니다.

```bash
./gradlew compileKotlin
```

생성 위치: `build/generated/source/kapt/main/com/test/graphql_test/entity/`

### 3단계: 애플리케이션 실행

```bash
./gradlew bootRun
```

### 4단계: GraphiQL 접속

브라우저에서 [http://localhost:8080/graphiql](http://localhost:8080/graphiql) 접속

---

## GraphQL Schema

### Query

```graphql
# 상품 목록 조회 (동적 필터 + 페이지네이션)
products(filter: ProductFilter, page: PageInput): ProductPage!

# 상품 단건 조회
product(id: ID!): ProductResponse

# 주문 목록 조회
orders(filter: OrderFilter, page: PageInput): OrderPage!

# 주문 단건 조회
order(id: ID!): OrderResponse
```

### Mutation

```graphql
# 상품 등록
createProduct(input: CreateProductInput!): ProductResponse!

# 주문 생성
createOrder(input: CreateOrderInput!): OrderResponse!

# 주문 상태 변경
updateOrderStatus(id: ID!, status: OrderStatus!): OrderResponse!
```

---

## 사용 예시 (GraphiQL)

### 동적 필터 + 페이지네이션 + categoryName JOIN

```graphql
query {
  products(
    filter: { minPrice: "20.00" }
    page: { sortBy: "price", sortDir: ASC }
  ) {
    content {
      name
      price
      categoryName
    }
    pageInfo {
      totalElements
      totalPages
    }
  }
}
```

### 4개 테이블 JOIN — userName, userEmail, productName 한 번에 조회

```graphql
query {
  order(id: 1) {
    userName
    userEmail
    status
    items {
      productName
      quantity
      unitPrice
    }
  }
}
```

### 주문 생성 Mutation

```graphql
mutation {
  createOrder(input: {
    userId: 1
    items: [{ productId: 2, quantity: 3 }]
  }) {
    id
    userName
    items {
      productName
      unitPrice
    }
  }
}
```

### 주문 상태 변경

```graphql
mutation {
  updateOrderStatus(id: 1, status: PROCESSING) {
    id
    status
    userName
  }
}
```

---

## DB 스키마

```
users
 └── orders ─── order_items ─── products ─── categories
```

```sql
users        (id, name, email, created_at)
categories   (id, name)
products     (id, name, price, stock, category_id, created_at)
orders       (id, user_id, status, created_at)
order_items  (id, order_id, product_id, quantity, unit_price)
```

---

## QueryDSL 설정 포인트

### build.gradle.kts

```kotlin
// kapt 플러그인 (Q클래스 코드 생성)
kotlin("kapt") version "2.2.21"

// Spring Framework 7.x는 jakarta.* 패키지 사용 → :jakarta classifier 필수
implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")

kapt {
    arguments {
        arg("plugin", "com.querydsl.apt.jpa.JPAAnnotationProcessor")
    }
}
```

### JPAQueryFactory Bean

`QueryDslConfig.kt`에서 `JPAQueryFactory`를 Bean으로 등록하고, 각 `RepositoryImpl`에서 주입받아 사용합니다.
