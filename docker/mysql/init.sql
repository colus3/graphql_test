USE ecommerce;

CREATE TABLE users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE products (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    stock       INT          NOT NULL DEFAULT 0,
    category_id BIGINT       NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE orders (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE order_items (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT         NOT NULL,
    product_id BIGINT         NOT NULL,
    quantity   INT            NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_item_order   FOREIGN KEY (order_id)   REFERENCES orders (id),
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- Sample data
INSERT INTO users (name, email) VALUES
    ('Alice Kim',  'alice@example.com'),
    ('Bob Lee',    'bob@example.com');

INSERT INTO categories (name) VALUES
    ('Electronics'),
    ('Books'),
    ('Clothing');

INSERT INTO products (name, price, stock, category_id) VALUES
    ('Wireless Headphones', 79.99,  50, 1),
    ('Kotlin in Action',    39.99, 100, 2),
    ('Spring Boot T-Shirt', 24.99, 200, 3),
    ('USB-C Hub',           49.99,  75, 1);

INSERT INTO orders (user_id, status) VALUES
    (1, 'PENDING'),
    (2, 'SHIPPED');

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
    (1, 1, 1, 79.99),
    (1, 2, 2, 39.99),
    (2, 3, 3, 24.99),
    (2, 4, 1, 49.99);
