Source code sql
-- Tạo database
CREATE DATABASE group6_project_e_commerce;

-- Khởi tạo các kiểu dữ liệu ENUM cho các trường liên quan
CREATE TYPE address_type_enum AS ENUM ('shipping', 'billing', 'both');
CREATE TYPE address_status_enum AS ENUM ('active', 'inactive', 'default');
CREATE TYPE store_status_enum AS ENUM ('pending', 'active', 'banned');
CREATE TYPE order_status_enum AS ENUM ('pending', 'confirmed', 'shipping', 'delivered', 'cancelled');
CREATE TYPE delivery_status_enum AS ENUM ('picking', 'shipping', 'delivered', 'failed');
CREATE TYPE shipper_status_enum AS ENUM ('active', 'inactive', 'busy');
CREATE TYPE carrier_status_enum AS ENUM ('active', 'inactive');CREATE TYPE payment_method_enum AS ENUM ('COD', 'BANKING', 'MOMO', 'VNPAY', 'PAYPAL');
CREATE TYPE payment_status_enum AS ENUM ('pending', 'completed', 'failed');

-- Tạo các bảng trong database
-- 1. Bảng users
CREATE TABLE users (
user_id BIGSERIAL PRIMARY KEY,
username VARCHAR(50) NOT NULL UNIQUE,
email VARCHAR(100) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
first_name VARCHAR(50) NOT NULL,
last_name VARCHAR(50) NOT NULL,
phone VARCHAR(15) UNIQUE,
date_of_birth DATE,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP,
deleted BOOLEAN NOT NULL DEFAULT FALSE
);
-- 2. Bảng addresses
CREATE TABLE addresses (
address_id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL,
street VARCHAR(255) NOT NULL,
region VARCHAR(100),
city VARCHAR(100) NOT NULL,
country VARCHAR(100) NOT NULL,
postal_code VARCHAR(20),
address_type address_type_enum NOT NULL,
status address_status_enum NOT NULL,
CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users (user_id)
ON DELETE CASCADE
);
-- 3. Bảng roles
CREATE TABLE roles (
role_id BIGSERIAL PRIMARY KEY,
role_name VARCHAR(50) NOT NULL UNIQUE
);
-- 4. Bảng user_roles (Bảng trung gian N-N)
CREATE TABLE user_roles (
user_id BIGINT NOT NULL,
role_id BIGINT NOT NULL,
PRIMARY KEY (user_id, role_id),
CONSTRAINT fk_userrole_user FOREIGN KEY (user_id) REFERENCES users (user_id)
ON DELETE CASCADE,
CONSTRAINT fk_userrole_role FOREIGN KEY (role_id) REFERENCES roles (role_id) ON
DELETE CASCADE
);
-- 5. Bảng stores
CREATE TABLE stores (
store_id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL,
store_address VARCHAR(255),
store_name VARCHAR(100) NOT NULL UNIQUE,
description TEXT,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP,
hotline VARCHAR(15) NOT NULL,
status store_status_enum NOT NULL,
CONSTRAINT fk_store_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);
-- 6. Bảng carts
CREATE TABLE carts (
cart_id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL UNIQUE,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP,CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON
DELETE CASCADE
);
-- 7. Bảng categories
CREATE TABLE categories (
category_id BIGSERIAL PRIMARY KEY,
category_name VARCHAR(100) NOT NULL UNIQUE,
category_image VARCHAR(255)
);
-- 8. Bảng brands
CREATE TABLE brands (
brand_id BIGSERIAL PRIMARY KEY,
brand_name VARCHAR(100) NOT NULL UNIQUE,
brand_image VARCHAR(255)
);
-- 9. Bảng products
CREATE TABLE products (
product_id BIGSERIAL PRIMARY KEY,
product_name VARCHAR(255) NOT NULL,
store_id BIGINT NOT NULL,
category_id BIGINT NOT NULL,
brand_id BIGINT,
price DECIMAL(12,2) NOT NULL,
old_price DECIMAL(12,2),
quantity INT NOT NULL,
discontinued BOOLEAN NOT NULL DEFAULT FALSE,
description TEXT,
buy_turn INT NOT NULL DEFAULT 0,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP,
specification TEXT,
CONSTRAINT fk_product_store FOREIGN KEY (store_id) REFERENCES stores (store_id),
CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES
categories (category_id),CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brands
(brand_id) ON DELETE SET NULL
);
-- 10. Bảng cart_items
CREATE TABLE cart_items (
cart_item_id BIGSERIAL PRIMARY KEY,
cart_id BIGINT NOT NULL,
product_id BIGINT NOT NULL,
quantity INT NOT NULL,
CONSTRAINT fk_cartitem_cart FOREIGN KEY (cart_id) REFERENCES carts (cart_id) ON
DELETE CASCADE,
CONSTRAINT fk_cartitem_product FOREIGN KEY (product_id) REFERENCES products
(product_id) ON DELETE CASCADE
);
-- 11. Bảng orders
CREATE TABLE orders (
order_id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL,
order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
required_date DATE NOT NULL,
shipped_date DATE,
ship_address_id BIGINT NOT NULL,
freight DECIMAL(12,2) NOT NULL,
status order_status_enum NOT NULL,
note TEXT,
total_amount DECIMAL(12,2) NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP,
CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users (user_id),
CONSTRAINT fk_order_address FOREIGN KEY (ship_address_id) REFERENCES
addresses (address_id)
);
-- 12. Bảng order_details
CREATE TABLE order_details (
order_detail_id BIGSERIAL PRIMARY KEY,order_id BIGINT NOT NULL,
product_id BIGINT NOT NULL,
quantity INT NOT NULL,
price DECIMAL(12,2) NOT NULL,
discount DECIMAL(5,2),
total_amount DECIMAL(12,2) NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP,
CONSTRAINT fk_orderdetail_order FOREIGN KEY (order_id) REFERENCES orders
(order_id) ON DELETE CASCADE,
CONSTRAINT fk_orderdetail_product FOREIGN KEY (product_id) REFERENCES
products (product_id)
);
-- 13. Bảng images
CREATE TABLE images (
image_id BIGSERIAL PRIMARY KEY,
product_id BIGINT NOT NULL,
url VARCHAR(255) NOT NULL,
CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES products
(product_id) ON DELETE CASCADE
);
-- 14. Bảng carriers
CREATE TABLE carriers (
carrier_id BIGSERIAL PRIMARY KEY,
carrier_name VARCHAR(100) NOT NULL UNIQUE,
carrier_phone VARCHAR(15) NOT NULL,
carrier_address VARCHAR(255),
carrier_email VARCHAR(100) NOT NULL UNIQUE,
status carrier_status_enum NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- 15. Bảng shippers
CREATE TABLE shippers (shipper_id BIGSERIAL PRIMARY KEY,
shipper_name VARCHAR(100) NOT NULL,
shipper_phone VARCHAR(15) NOT NULL UNIQUE,
carrier_id BIGINT NOT NULL,
status shipper_status_enum NOT NULL,
avatar_url VARCHAR(255),
vehicle_num VARCHAR(20),
CONSTRAINT fk_shipper_carrier FOREIGN KEY (carrier_id) REFERENCES carriers
(carrier_id)
);
-- 16. Bảng deliveries
CREATE TABLE deliveries (
delivery_id BIGSERIAL PRIMARY KEY,
order_id BIGINT NOT NULL,
carrier_id BIGINT NOT NULL,
shipper_id BIGINT,
delivery_status delivery_status_enum NOT NULL,
tracking_num VARCHAR(50) NOT NULL UNIQUE,
ship_address VARCHAR(255) NOT NULL,
delivered_at TIMESTAMP,
shipped_at TIMESTAMP,
estimated_delivery_date DATE,
CONSTRAINT fk_delivery_order FOREIGN KEY (order_id) REFERENCES orders
(order_id),
CONSTRAINT fk_delivery_carrier FOREIGN KEY (carrier_id) REFERENCES carriers
(carrier_id),
CONSTRAINT fk_delivery_shipper FOREIGN KEY (shipper_id) REFERENCES shippers
(shipper_id) ON DELETE SET NULL
);
-- 17. Bảng payments
CREATE TABLE payments (
payment_id BIGSERIAL PRIMARY KEY,
order_id BIGINT NOT NULL,
payment_method payment_method_enum NOT NULL,
payment_date TIMESTAMP,amount DECIMAL(12,2) NOT NULL,
status payment_status_enum NOT NULL,
CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders
(order_id) ON DELETE CASCADE
);
-- 18. Bảng reviews
CREATE TABLE reviews (
review_id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL,
product_id BIGINT NOT NULL,
rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
comment TEXT,
review_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users (user_id),
CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES products
(product_id) ON DELETE CASCADE
);

