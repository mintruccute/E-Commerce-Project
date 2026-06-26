-- ====================================================================
-- PHẦN 1: FUNCTIONS & TRIGGERS (HÀM & BỘ KÍCH HOẠT)
-- ====================================================================

-- 1. Hàm kiểm tra và cập nhật số lượng tồn kho + lượt mua của sản phẩm 
CREATE OR REPLACE FUNCTION update_product_stock_func()
RETURNS TRIGGER AS $$
BEGIN
    -- Kiểm tra số lượng tồn kho có đủ để đáp ứng đơn hàng hay không
    IF (SELECT quantity FROM products WHERE product_id = NEW.product_id) < NEW.quantity THEN
        RAISE EXCEPTION 'So luong san pham trong kho khong du de dap ung don hang!';
    END IF;

    -- Thực hiện trừ kho và cộng dồn lượt mua (buy_turn) của sản phẩm
    UPDATE products 
    SET quantity = quantity - NEW.quantity,
        buy_turn = buy_turn + NEW.quantity
    WHERE product_id = NEW.product_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo Trigger tự động trừ kho khi có bản ghi mới trong order_details
DROP TRIGGER IF EXISTS trigger_update_stock ON order_details;
CREATE TRIGGER trigger_update_stock
AFTER INSERT ON order_details
FOR EACH ROW
EXECUTE FUNCTION update_product_stock_func();


-- 2. Hàm tự động tính tổng tiền của đơn hàng (bao gồm giá trị hàng hóa + phí ship) 
CREATE OR REPLACE FUNCTION calculate_order_total(target_order_id BIGINT)
RETURNS VOID AS $$
DECLARE
    v_freight DECIMAL(12,2);
    v_items_total DECIMAL(12,2);
BEGIN
    -- Lấy phí vận chuyển của đơn hàng
    SELECT freight INTO v_freight FROM orders WHERE order_id = target_order_id;
    
    -- Tính tổng số tiền của tất cả các mặt hàng thuộc đơn hàng trong chi tiết đơn hàng
    SELECT COALESCE(SUM(total_amount), 0) INTO v_items_total 
    FROM order_details 
    WHERE order_id = target_order_id;
    
    -- Cập nhật giá trị tổng tiền thực tế và mốc thời gian cập nhật cho bảng orders
    UPDATE orders 
    SET total_amount = v_items_total + v_freight,
        updated_at = CURRENT_TIMESTAMP
    WHERE order_id = target_order_id;
END;
$$ LANGUAGE plpgsql;


-- ====================================================================
-- PHẦN 2: DATABASE VIEWS (KHUNG NHÌN DỮ LIỆU)
-- ====================================================================

-- 1. View báo cáo hiệu suất kinh doanh và doanh thu thực tế của từng cửa hàng 
CREATE OR REPLACE VIEW view_store_performance AS
SELECT 
    s.store_id,
    s.store_name,
    u.username AS owner_name,
    s.status AS store_status,
    COUNT(DISTINCT p.product_id) AS total_products,
    COUNT(DISTINCT o.order_id) AS completed_orders_count,
    COALESCE(SUM(CASE WHEN o.status = 'delivered' THEN od.total_amount ELSE 0 END), 0) AS total_revenue
FROM stores s
JOIN users u ON s.user_id = u.user_id
LEFT JOIN products p ON s.store_id = p.store_id
LEFT JOIN order_details od ON p.product_id = od.product_id
LEFT JOIN orders o ON od.order_id = o.order_id AND o.status = 'delivered'
GROUP BY s.store_id, s.store_name, u.username, s.status;

-- Lệnh gọi thử nghiệm View:
-- SELECT * FROM view_store_performance ORDER BY total_revenue DESC;


-- 2. View theo dõi chi tiết trạng thái vận đơn và thông tin shipper theo thời gian thực 
CREATE OR REPLACE VIEW view_delivery_tracking AS
SELECT 
    o.order_id,
    o.user_id AS customer_id,
    d.tracking_num,
    c.carrier_name,
    s.shipper_name,
    s.shipper_phone,
    d.delivery_status,
    d.ship_address,
    d.estimated_delivery_date,
    d.shipped_at,
    d.delivered_at
FROM orders o
JOIN deliveries d ON o.order_id = d.order_id
JOIN carriers c ON d.carrier_id = c.carrier_id
LEFT JOIN shippers s ON d.shipper_id = s.shipper_id;

-- Lệnh gọi thử nghiệm View theo đơn hàng cụ thể (Ví dụ ID: 1):
-- SELECT * FROM view_delivery_tracking WHERE order_id = 1;


-- ====================================================================
-- PHẦN 3: DATABASE INDEXES (CHỈ MỤC TỐI ƯU HÓA TRUY VẤN) 
-- ====================================================================

-- Các chỉ mục B-Tree trên các trường khóa ngoại để tối ưu hóa liên kết bảng (JOIN) 
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_brand ON products(brand_id);
CREATE INDEX IF NOT EXISTS idx_products_store ON products(store_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_shipper_status ON deliveries(shipper_id, delivery_status);
CREATE INDEX IF NOT EXISTS idx_reviews_product_id ON reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_stores_created_at ON stores(created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_review_date ON reviews(review_date);

-- Chỉ mục phức hợp hỗ trợ lọc trạng thái sản phẩm và sắp xếp theo ngày tạo mới nhất 
CREATE INDEX IF NOT EXISTS idx_products_status_created ON products(discontinued, quantity, created_at DESC);

-- Chỉ mục phức hợp hỗ trợ tìm đơn hàng theo người dùng và sắp xếp theo ngày đặt mới nhất 
CREATE INDEX IF NOT EXISTS idx_orders_user_date ON orders(user_id, order_date DESC);

-- Chỉ mục duy nhất (Unique Index) để tối ưu tra cứu nhanh theo mã vận đơn 
CREATE UNIQUE INDEX IF NOT EXISTS idx_deliveries_tracking_num ON deliveries(tracking_num);

-- Chỉ mục GIN hỗ trợ tìm kiếm toàn văn (Full-Text Search) không dấu/có dấu trên tên sản phẩm 
CREATE INDEX IF NOT EXISTS idx_products_name_fts ON products USING gin(to_tsvector('english', product_name));


