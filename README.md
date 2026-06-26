# E-Commerce Project

## Introduction

Sự phát triển mạnh mẽ của thương mại điện tử đặt ra yêu cầu cao về việc thiết kế một cơ sở dữ liệu có cấu trúc chặt chẽ, an toàn và tối ưu hóa hiệu năng truy vấn. Hệ thống lưu trữ thông tin cần xử lý tốt các luồng giao dịch đồng thời và đồng bộ thông tin thời gian thực giữa các thực thể người dùng, gian hàng, đơn hàng và đơn vị giao vận.

Dự án E-Commerce Project được xây dựng nhằm thiết kế và tối ưu hóa cơ sở dữ liệu vật lý trên hệ quản trị PostgreSQL, đạt chuẩn hóa 3NF để giảm thiểu tối đa sự dư thừa và triệt tiêu các dị thường dữ liệu. Hệ thống hỗ trợ đầy đủ các nghiệp vụ thực tế như quản lý tài khoản phân quyền động, quản lý kho sản phẩm độc lập của từng cửa hàng, quy trình giỏ hàng, đặt hàng thanh toán bảo toàn thuộc tính ACID và điều phối shipper giao vận thời gian thực.

Để kiểm chứng hiệu năng và tối ưu hóa ở tầng vật lý, nhóm đã áp dụng các chiến lược đánh chỉ mục B-Tree trên hệ thống khóa ngoại, chỉ mục phức hợp để tăng tốc bộ lọc kết hợp sắp xếp, chỉ mục GIN hỗ trợ tìm kiếm toàn văn và viết các trigger tự động cập nhật kho. Bên cạnh đó, nhóm đã xây dựng ứng dụng khách Client trực quan bằng ngôn ngữ Java Swing kết nối qua JDBC để mô phỏng thực tế quá trình vận hành của hệ thống, giúp mang lại cái nhìn trực quan và đánh giá chính xác hiệu năng của các câu lệnh SQL thực tế.
