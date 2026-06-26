-- 1. Yêu cầu nghiệp vụ : Khách hàng muốn tìm sản phẩm có tên “Enormous Marble Watch” 
-- (không phân biệt tìm kiếm chữ cái hoa thường)

-- EXPLAIN ANALYZE
SELECT * 
FROM products
WHERE to_tsvector('english',product_name) 
@@ plainto_tsquery('english','Enormous Marble Watch');

-- 2. Yêu cầu nghiệp vụ : Khách hàng muốn tìm các sản phẩm khuyến mại có giá gốc cao hơn giá hiện tại

-- EXPLAIN ANALYZE
SELECT * FROM products
WHERE old_price > price;

-- 3. Yêu cầu nghiệp vụ : Khách hàng muốn tìm top 20 sản phẩm “hot” nhất (có lượt bán nhiều nhất)

-- EXPLAIN ANALYZE
SELECT * FROM products
ORDER BY buy_turn DESC LIMIT 20;

-- 4. Yêu cầu nghiệp vụ : Tìm các lượt đánh giá 5 sao của sản phẩm “Enormous Marble Watch” có product_id = 29

-- EXPLAIN ANALYZE
SELECT * FROM reviews
WHERE product_id = 29 AND rating = 5;

-- 5. Yêu cầu nghiệp vụ : Tìm địa chỉ giao hàng mặc định của khách hàng có user_id = 5

-- EXPLAIN ANALYZE
SELECT * FROM addresses 
WHERE user_id = 5 AND status = 'default';

-- 6. Yêu cầu nghiệp vụ : Lấy danh sách tên sản phẩm và số lượng trong giỏ hàng của khách hàng có cart_id = 5.

-- EXPLAIN ANALYZE
SELECT p.product_name, ci.quantity FROM cart_items ci 
JOIN products p ON ci.product_id = p.product_id 
WHERE ci.cart_id = 5;

-- 7. Yêu cầu nghiệp vụ : Lấy toàn bộ lịch sử đơn hàng của khách hàng có user_id = 5, 
-- sắp xếp theo thời gian đặt hàng mới nhất.

-- EXPLAIN ANALYZE
SELECT * FROM orders
WHERE user_id = 5
ORDER BY created_at DESC;

-- 8. Yêu cầu nghiệp vụ : Lấy danh sách tên sản phẩm, 
-- số lượng và đơn giá của các mặt hàng trong đơn hàng có order_id = 5.

-- EXPLAIN ANALYZE
SELECT p.product_name, od.quantity, od.price
FROM order_details od
JOIN products p ON od.product_id = p.product_id
WHERE od.order_id = 5;

-- 9. Yêu cầu nghiệp vụ : Thống kê tổng số đơn hàng của từng khách hàng trong hệ thống.

-- EXPLAIN ANALYZE
SELECT user_id, COUNT(*) FROM orders
GROUP BY user_id;

-- 10. Yêu cầu nghiệp vụ : Lấy danh sách đơn hàng được tạo trong khoảng thời gian từ 2024-01-01 đến 2024-12-31.

-- EXPLAIN ANALYZE
SELECT * FROM orders
WHERE created_at BETWEEN '2024-01-01' AND '2024-12-31';

-- 11. Yêu cầu nghiệp vụ : Liệt kê đơn hàng bị hủy 

-- EXPLAIN ANALYZE
SELECT * FROM orders WHERE status = 'cancelled';

-- 12. Yêu cầu nghiệp vụ : Kiểm tra phương thức thanh toán của một đơn hàng

-- EXPLAIN ANALYZE
SELECT payment_method FROM payments 
WHERE order_id =3;

-- 13. Yêu cầu nghiệp vụ : Tổng tiền đã thanh toán thành công của 1 user

-- EXPLAIN ANALYZE 
SELECT SUM(total_amount) FROM orders 
WHERE user_id = 10 AND status = 'delivered';

-- 14. Yêu cầu nghiệp vụ : Các đơn hàng đag được shipper vận chuyển 

-- EXPLAIN ANALYZE 
SELECT * FROM deliveries 
WHERE delivery_status = 'shipping';

-- 15. Yêu cầu nghiệp vụ : Liệt kê danh sách shippers của một đơn vị vận chuyển (carrier)

-- EXPLAIN ANALYZE 
SELECT * FROM shippers 
WHERE carrier_id = 2;

-- 16. Yêu cầu nghiệp vụ : Lấy danh sách đơn hàng cần giao trong ngày

-- EXPLAIN ANALYZE 
SELECT * FROM deliveries 
WHERE estimated_delivery_date = CURRENT_DATE;

-- 17. Yêu cầu nghiệp vụ : Tìm shipper đang rảnh (status = 'active')

-- EXPLAIN ANALYZE 
SELECT * FROM shippers 
WHERE status = 'active';

-- 18. Yêu cầu nghiệp vụ : Tổng doanh thu hệ thống

-- EXPLAIN ANALYZE 
SELECT SUM(total_amount) FROM orders 
WHERE status = 'delivered';

-- 19. Yêu cầu nghiệp vụ : Top 10 sản phẩm bán chạy nhất tháng

-- EXPLAIN ANALYZE 
SELECT product_id, SUM(quantity) FROM order_details 
GROUP BY product_id 
ORDER BY SUM(quantity) DESC LIMIT 10;

-- 20. Yêu cầu nghiệp vụ : Cửa hàng có doanh thu cao nhất

-- EXPLAIN ANALYZE 
SELECT store_id, SUM(o.total_amount) FROM orders o 
JOIN order_details od ON o.order_id = od.order_id 
JOIN products p ON od.product_id = p.product_id 
GROUP BY store_id 
ORDER BY SUM(o.total_amount) DESC LIMIT 1;

-- 21. Yêu cầu nghiệp vụ: Lọc các sản phẩm đang bán, còn hàng trong khoảng giá từ 500,000 đến 2,000,000 VND 
-- và sắp xếp từ sản phẩm mới đăng nhất đến cũ nhất.

-- explain analyze
select product_id, product_name, price, created_at
from products
where (discontinued = false) and (quantity > 5)
and (price between 500000.00 and 2000000.00)
order by created_at desc;

-- 22. Yêu cầu nghiệp vụ: Tính điểm đánh giá trung bình của từng cửa hàng dựa trên các lượt đánh giá sản phẩm của khách hàng, 
-- nhằm phục vụ việc theo dõi và hiển thị chất lượng dịch vụ của các cửa hàng trên sàn TMĐT.

-- explain analyze
select store_id, avg(rating) 
from reviews r join products p 
on r.product_id = p.product_id 
group by store_id;


-- 23. Yêu cầu nghiệp vụ: Lọc ra các gian hàng mới đăng ký hoạt động trong giai đoạn 
-- từ ngày 01/05/2026 đến hết ngày 31/05/2026 để gửi thư chào mừng của hệ thống.

-- explain analyze
select store_id, store_name, created_at
from stores
where created_at between '2026-05-01 00:00:00' and '2026-05-31 23:59:59';

-- 24. Yêu cầu nghiệp vụ: Thống kê xem tỉnh/thành phố nào đang có lượng đơn đặt hàng lớn nhất 
-- để sàn lên kế hoạch mở rộng chi nhánh kho bãi hoặc liên kết thêm đơn vị vận chuyển tại khu vực đó.

-- explain analyze
select a.city, count(o.order_id) as tong_so_don_hang
from orders o join addresses a 
on o.ship_address_id = a.address_id
group by a.city
order by tong_so_don_hang desc;

-- 25. Yêu cầu nghiệp vụ: Tính toán tỷ lệ phần trăm đơn hàng bị hủy trên tổng số lượng đơn hàng phát sinh 
-- của sàn để phòng ngừa rủi ro.

-- explain analyze 
select avg(case 
when status = 'cancelled' then 1.0 
else 0.0 end) * 100 as cancellation_rate_percent 
from orders;

-- 26. Yêu cầu nghiệp vụ: Lọc ra các gian hàng hoạt động xuất sắc có tổng doanh thu thực tế 
-- (đơn hàng đã giao thành công) đạt trên 50 triệu đồng để nâng cấp nhãn "Cửa hàng VIP".

-- explain analyze
select s.store_id, s.store_name, sum(od.total_amount) as doanh_thu
from stores s
join products p on s.store_id = p.store_id
join order_details od on p.product_id = od.product_id
join orders o on od.order_id = o.order_id
where o.status = 'delivered'
group by s.store_id, s.store_name
having sum(od.total_amount) > 50000000.00;

-- 27. Yêu cầu nghiệp vụ: Chuyển trạng thái deleted = true khi khách hàng yêu cầu đóng/xóa tài khoản 
-- để bảo toàn lịch sử dữ liệu của sàn.

-- explain analyze
update users set deleted = true 
where user_id = 1;

-- 28. Yêu cầu nghiệp vụ: Đếm số lượng đơn hàng giao trễ của từng đối tác 
-- để làm tiêu chí đánh giá chất lượng KPI giao nhận.

-- explain analyze
select c.carrier_name, 
count(d.delivery_id) as so_don_giao_tre
from carriers c join deliveries d 
on c.carrier_id = d.carrier_id
where d.delivery_status = 'delivered' 
and d.delivered_at > d.estimated_delivery_date
group by c.carrier_name;

-- 29. Yêu cầu nghiệp vụ: Thay đổi trạng thái hoạt động của cửa hàng sang banned 
-- khi phát hiện vi phạm quy chế hoạt động của sàn TMĐT.

-- explain analyze
update stores set status = 'banned' 
where store_id = 1;  

-- 30. Yêu cầu nghiệp vụ: Hiển thị 20 lượt nhận xét, đánh giá sản phẩm mới nhất của khách hàng lên trang chủ.

-- explain analyze 
select * from reviews 
order by review_date desc limit 20;


