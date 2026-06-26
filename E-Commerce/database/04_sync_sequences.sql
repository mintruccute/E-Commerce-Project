-- Đồng bộ lại bộ đếm tự động cho bảng carts
SELECT setval(pg_get_serial_sequence('carts', 'cart_id'), COALESCE(MAX(cart_id), 1)) FROM carts;

-- Đồng bộ lại bộ đếm tự động cho bảng cart_items
SELECT setval(pg_get_serial_sequence('cart_items', 'cart_item_id'), COALESCE(MAX(cart_item_id), 1)) FROM cart_items;

-- Đồng bộ lại bộ đếm tự động cho bảng users
SELECT setval(pg_get_serial_sequence('users', 'user_id'), COALESCE(MAX(user_id), 1)) FROM users;

-- Đồng bộ lại bộ đếm tự động cho các bảng khác (để phòng tránh lỗi tương tự khi đặt hàng)
SELECT setval(pg_get_serial_sequence('addresses', 'address_id'), COALESCE(MAX(address_id), 1)) FROM addresses;
SELECT setval(pg_get_serial_sequence('orders', 'order_id'), COALESCE(MAX(order_id), 1)) FROM orders;
SELECT setval(pg_get_serial_sequence('order_details', 'order_detail_id'), COALESCE(MAX(order_detail_id), 1)) FROM order_details;
SELECT setval(pg_get_serial_sequence('deliveries', 'delivery_id'), COALESCE(MAX(delivery_id), 1)) FROM deliveries;
