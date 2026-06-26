import net.datafaker.Faker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class ECommerceDataGen {

    // Hàm lấy ngẫu nhiên một phần tử từ List
    private static <T> T getRandom(List<T> list, Random random) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }

    // Hàm escape ký tự đặc biệt cho SQL
    private static String escape(String str) {
        if (str == null) {
            return "";
        }
        String cleanStr = removeAccent(str);
        return cleanStr.replace("'", "''");
    }

    // Hàm chuẩn hóa loại bỏ hoàn toàn dấu tiếng Việt
    private static String removeAccent(String s) {
        if (s == null) {
            return "";
        }
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(temp).replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        // Loại bỏ các ký tự đặc biệt có thể sinh ra từ Faker để câu lệnh SQL sạch sẽ hơn
        return result.replaceAll("[^\\p{ASCII}]", "");
    }

    public static void main(String[] args) {
        // TẠO TÊN FILE MỚI THEO TIMESTAMP (Không lo ghi đè)
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String outputFileName = "du_lieu_san_tmdt_" + timestamp + ".sql";

        try {
            File file = new File(outputFileName);
            PrintStream fileStream = new PrintStream(new FileOutputStream(file), true, "UTF-8");
            System.setOut(fileStream);

            System.err.println("-> Dang bat dau khoi tao va sinh du lieu...");
            System.err.println("-> File ket qua se duoc luu tai: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Loi cau hinh file dau ra: " + e.getMessage());
            return;
        }

        Faker faker = new Faker(new Locale("vi"));
        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // CAU HINH SO LUONG BAN GHI KHOI TAO
        int soNhaVanChuyen = 4;
        int soShipper = 50;
        int soDanhMuc = 15;
        int soThuongHieu = 16;
        int soCuaHang = 60;
        int soSanPham = 200;
        int soDonHang = 400;

        int userStartId = 1;
        int userEndId = 300;

        System.out.println("-- ====================================================================");
        System.out.println("-- SCRIPT GENERATED FOR HUST E-COMMERCE DATABASE SYSTEM");
        System.out.println("-- ====================================================================\n");

        // 1. CHEN BANG ROLES
        System.out.println("-- 1. INSERT DATA FOR ROLES");
        List<String> coreRoles = Arrays.asList("Customer", "Seller", "Admin");
        for (int i = 0; i < coreRoles.size(); i++) {
            System.out.printf("INSERT INTO roles (role_id, role_name) VALUES (%d, '%s') ON CONFLICT DO NOTHING;\n", 
                    (i + 1), coreRoles.get(i));
        }

        // 2. CHEN BANG USERS
        System.out.println("\n-- 2. INSERT DATA FOR USERS");
        List<Long> userIds = new ArrayList<>();
        String passwordHash = "$2b$12$eImiTxAk4vmMUL85vKH6He7mclMx4fB.XyA796Lfi9999G5vE.n2O"; // password123

        // Danh sach cac ten hay do ban tu dinh nghia (Khong dau)
        List<String> customFirstNames = Arrays.asList(
            "Truc", "Hung", "Thinh", "Yen", "Uyen", "Mai", "Hoang", "Hang", "Tung", "Son", 
            "Chi", "Ngoc", "Van", "May", "Lam", "Tri", "Phuc", "Dao", "Khoa", "Phan", 
            "Quang", "Dinh", "Thu", "Ngan", "Linh", "Hieu", "Phuong", "Minh", "Tuan", "Anh", 
            "Hong", "Duc", "Trang", "Khanh", "Quy", "Thao", "Vinh", "Toan", "Tai", "Nhat", 
            "Ha", "Dong", "Bac", "Oanh", "Dung", "Diep"
        );

        // Danh sach cac ho pho bien (Khong dau) de ghep voi ten cho tu nhien
        List<String> customLastNames = Arrays.asList(
            "Nguyen", "Tran", "Le", "Pham", "Hoang", "Phan", "Vu", "Dang", "Bui", "Do", 
            "Tong", "Ly", "Dao", "Mai", "Trinh", "Dinh", "Ho", "Ngo", "Vuong", "Phung", 
            "Cao", "Luu", "Quach", "Vo", "Truong", "Doan", "Luong", "Bach", "Ha"
        );

        for (int id = userStartId; id <= userEndId; id++) {
            userIds.add((long) id);

            // Boc ngau nhien Ten va Ho tu danh sach dep ban da cho
            String firstName = getRandom(customFirstNames, random);
            String lastName = getRandom(customLastNames, random);

            // Them ID vao sau username de dam bao 100% KHONG BAO GIO trung lap
            String username = "customer_" + id + "_" + faker.random().hex(3).toLowerCase();

            // Email khong dau, viet lien va chen ID de tranh trung khoa Unique trong Database
            String email = (firstName + "." + lastName).toLowerCase() + id + "@gmail.com";

            String dob = dateFormat.format(faker.date().birthday(18, 26));
            String createdAt = tsFormat.format(faker.date().past(365, java.util.concurrent.TimeUnit.DAYS));

            // Xuat lenh INSERT
            System.out.printf(
                "INSERT INTO users (user_id, username, email, password, first_name, last_name, phone, date_of_birth, created_at, deleted) " +
                "VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', FALSE);\n",
                id, username, email, passwordHash + faker.random().hex(10), firstName, lastName, 
                faker.phoneNumber().cellPhone(), dob, createdAt
            );
        }

        // 3. PHAN VAI TRO (USER_ROLES)
        System.out.println("\n-- 3. INSERT DATA FOR USER_ROLES");
        for (Long uid : userIds) {
            System.out.printf("INSERT INTO user_roles (user_id, role_id) VALUES (%d, 1);\n", uid);
            if (random.nextDouble() < 0.40) {
                System.out.printf("INSERT INTO user_roles (user_id, role_id) VALUES (%d, 2);\n", uid);
            }
        }

        // 4. CHEN BANG ADDRESSES
        System.out.println("\n-- 4. INSERT DATA FOR ADDRESSES");
        List<Long> addressIds = new ArrayList<>();
        long addrIdCounter = 1;
        String[] provinces = {
            "Ha Noi", "Ho Chi Minh", "Da Nang", "Hai Phong", "Can Tho", "An Giang", 
            "Da Lat", "Quang Nam", "Binh Thuan", "Ca Mau", "Bac Ninh", "Ha Giang", 
            "Binh Duong", "Ninh Binh", "Hue", "Nha Trang", "Vung Tau", "Quang Ninh", 
            "Thanh Hoa", "Phu Quoc", "Yen Bai", "Son La", "Lang Son", "Quang Binh", 
            "Ha Tinh", "Dak Lak"
        };

        for (Long uid : userIds) {
            int soDiaChi = random.nextInt(2) + 1;
            for (int a = 0; a < soDiaChi; a++) {
                addressIds.add(addrIdCounter);
                String city = provinces[random.nextInt(provinces.length)];
                String type = (a == 0) ? "default" : "active";
                System.out.printf(
                    "INSERT INTO addresses (address_id, user_id, street, region, city, country, postal_code, address_type, status) " +
                    "VALUES (%d, %d, '%s', '%s', '%s', 'Viet Nam', '%s', 'shipping', '%s');\n",
                    addrIdCounter++, uid, escape(faker.address().streetAddress()), 
                    escape(faker.address().country()), city, faker.address().zipCode(), type
                );
            }
        }

        // 5. CHEN BANG STORES
        System.out.println("\n-- 5. INSERT DATA FOR STORES");
        List<Long> storeIds = new ArrayList<>();
        Collections.shuffle(userIds);
        for (int i = 1; i <= soCuaHang; i++) {
            storeIds.add((long) i);
            Long chuShopId = userIds.get(i % userIds.size());

            // Đảm bảo tên cửa hàng là duy nhất bằng cách cộng thêm chỉ số i vào cuối
            String storeName = escape(faker.company().name() + " Official Store " + i);

            // Ép địa chỉ ngắn lại (Faker fullAddress đôi khi sinh chuỗi quá dài so với VARCHAR(255))
            String storeAddress = escape(faker.address().streetAddress() + ", " + faker.address().city());
            if (storeAddress.length() > 255) {
                storeAddress = storeAddress.substring(0, 250);
            }

            // Sinh số điện thoại hotline
            String hotline = "0" + faker.number().digits(9);

            System.out.printf(
                "INSERT INTO stores (store_id, user_id, store_address, store_name, description, hotline, status) " +
                "VALUES (%d, %d, '%s', '%s', 'Cua hang phan phoi chinh hang thuoc he thong thuong mai', '%s', 'active');\n",
                i, chuShopId, storeAddress, storeName, hotline
            );
        }

        // 6. CHEN CARRIERS
        System.out.println("\n-- 6. INSERT DATA FOR CARRIERS");
        List<Long> carrierIds = new ArrayList<>();
        String[] dviVC = {"Giao Hang Tiet Kiem", "Giao Hang Nhanh", "Viettel Post", "VNPost"};
        String[] emailVC = {"ghtk@carrier.vn", "ghn@carrier.vn", "viettel@carrier.vn", "vnpost@carrier.vn"};
        for (int i = 1; i <= soNhaVanChuyen; i++) {
            carrierIds.add((long) i);
            String phone = "0" + faker.number().digits(9);

            System.out.printf(
                "INSERT INTO carriers (carrier_id, carrier_name, carrier_phone, carrier_address, carrier_email, status) " +
                "VALUES (%d, '%s', '%s', '%s', '%s', 'active');\n",
                i, dviVC[i - 1], phone, escape(faker.address().fullAddress()), emailVC[i - 1]
            );
        }

        // 7. CHEN SHIPPERS
        System.out.println("\n-- 7. INSERT DATA FOR SHIPPERS");
        List<Long> shipperIds = new ArrayList<>();
        for (int i = 1; i <= soShipper; i++) {
            shipperIds.add((long) i);
            Long carrierId = getRandom(carrierIds, random);
            String bienSoXe = "29-" + faker.random().hex(1).toUpperCase() + (random.nextInt(90000) + 10000);
            
            System.out.printf(
                "INSERT INTO shippers (shipper_id, shipper_name, shipper_phone, carrier_id, status, vehicle_num) " +
                "VALUES (%d, '%s', '%s', %d, 'active', '%s');\n",
                i, escape(faker.name().fullName()), faker.phoneNumber().cellPhone(), carrierId, "active", bienSoXe
            );
        }

        // 8. CHEN CATEGORIES & BRANDS
        System.out.println("\n-- 8. INSERT DATA FOR CATEGORIES & BRANDS");
        List<Long> categoryIds = new ArrayList<>();
        String[] dmucNames = {
            "Dien thoai & May tinh", "Thoi trang", "Giay dep", "Sach & Thiet bi hoc tap", 
            "My pham", "Do gia dung", "The thao & Du lich", "Do choi & Me va be", 
            "O to & Xe may", "Dien gia dung", "Thiet bi dien tu", "Noi that & Ngoai that", 
            "Thuc pham & Do uong", "Van phong pham", "Dich vu & The thao"
        };
        for (int i = 1; i <= soDanhMuc; i++) {
            categoryIds.add((long) i);
            System.out.printf("INSERT INTO categories (category_id, category_name, category_image) VALUES (%d, '%s', 'cat_%d.png');\n", 
                    i, dmucNames[i - 1], i);
        }

        List<Long> brandIds = new ArrayList<>();
        String[] brandNames = {
            "Apple", "Samsung", "Sony", "Nike", "Adidas", "Xuka Book", "Uniqlo", 
            "Logitech", "Lopito", "Yody", "LG", "Candid", "Puma", "Dior", "Chanel", "YSL"
        };
        for (int i = 1; i <= soThuongHieu; i++) {
            brandIds.add((long) i);
            System.out.printf("INSERT INTO brands (brand_id, brand_name, brand_image) VALUES (%d, '%s', 'brand_%d.png');\n", 
                    i, brandNames[i - 1], i);
        }

        // 9. CHEN PRODUCTS
        System.out.println("\n-- 9. INSERT DATA FOR PRODUCTS");
        List<Long> productIds = new ArrayList<>();
        for (int i = 1; i <= soSanPham; i++) {
            productIds.add((long) i);
            Long storeId = getRandom(storeIds, random);
            Long catId = getRandom(categoryIds, random);
            Long brandId = getRandom(brandIds, random);
            double currentPrice = faker.number().numberBetween(50000, 15000000);
            double oldPrice = currentPrice * 1.15;

            System.out.printf(java.util.Locale.US,
                "INSERT INTO products (product_id, product_name, store_id, category_id, brand_id, price, old_price, quantity, discontinued, description, buy_turn, specification) " +
                "VALUES (%d, '%s', %d, %d, %d, %.2f, %.2f, %d, FALSE, '%s', %d, '%s');\n",
                i, 
                escape(faker.commerce().productName()), 
                storeId, 
                catId, 
                brandId, 
                currentPrice, 
                oldPrice, 
                faker.number().numberBetween(10, 500), 
                escape(faker.lorem().paragraph(2)), 
                faker.number().numberBetween(0, 100), 
                "Hang chinh hang phan phoi toan quoc day du phu kien di kem."
            );
        }

        // 10. CHEN IMAGES SAN PHAM
        System.out.println("\n-- 10. INSERT DATA FOR PRODUCT IMAGES");
        long imgIdCounter = 1;
        for (Long pid : productIds) {
            for (int j = 1; j <= 2; j++) {
                System.out.printf("INSERT INTO images (image_id, product_id, url) VALUES (%d, %d, 'https://cdn.hust-ecommerce.vn/products/p_%d_img_%d.jpg');\n",
                        imgIdCounter++, pid, pid, j);
            }
        }

        // 11. CHEN BANG CARTS & CART_ITEMS
        System.out.println("\n-- 11. INSERT DATA FOR CARTS & CART_ITEMS");
        long cartItemCounter = 1;
        for (int i = 0; i < userIds.size(); i++) {
            long currentCartId = i + 1;
            Long currentUserId = userIds.get(i);
            System.out.printf("INSERT INTO carts (cart_id, user_id) VALUES (%d, %d);\n", currentCartId, currentUserId);

            int sanPhamTrongGio = random.nextInt(3) + 1;
            for (int j = 0; j < sanPhamTrongGio; j++) {
                System.out.printf("INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity) VALUES (%d, %d, %d, %d);\n",
                        cartItemCounter++, currentCartId, getRandom(productIds, random), random.nextInt(3) + 1);
            }
        }

        // 12. CHEN BANG ORDERS, ORDER_DETAILS, PAYMENTS & DELIVERIES
        System.out.println("\n-- 12. INSERT DATA FOR TRANSACTIONAL ORDERS (COMPLEX BUSINESS FLOW)");
        long orderDetailCounter = 1;
        long paymentCounter = 1;
        long deliveryCounter = 1;

        String[] paymentMethods = {"COD", "BANKING", "MOMO", "VNPAY", "PAYPAL"};
        String[] orderStatuses = {"pending", "confirmed", "shipping", "delivered", "cancelled"};

        for (int i = 1; i <= soDonHang; i++) {
            Long orderUserId = getRandom(userIds, random);
            Long shipAddrId = getRandom(addressIds, random);
            String orderStatus = orderStatuses[random.nextInt(orderStatuses.length)];

            double freight = 15000 + (random.nextInt(5) * 5000);
            String orderDateStr = tsFormat.format(faker.date().past(30, java.util.concurrent.TimeUnit.DAYS));

            int itemsInOrder = random.nextInt(3) + 1;
            double orderSubtotal = 0;
            StringBuilder orderDetailsBuilder = new StringBuilder();

            for (int k = 0; k < itemsInOrder; k++) {
                Long orderedProductId = getRandom(productIds, random);
                int buyQty = random.nextInt(2) + 1;
                double itemPrice = 150000 + (random.nextInt(10) * 100000);
                double discount = random.nextInt(4) * 5.0;
                double totalLineAmount = (itemPrice * buyQty) * (1 - (discount / 100.0));
                orderSubtotal += totalLineAmount;

                // Sử dụng Locale.US để ép dấu chấm thập phân
                orderDetailsBuilder.append(String.format(java.util.Locale.US,
                        "INSERT INTO order_details (order_detail_id, order_id, product_id, quantity, price, discount, total_amount) " +
                        "VALUES (%d, %d, %d, %d, %.2f, %.2f, %.2f);\n",
                        orderDetailCounter++, i, orderedProductId, buyQty, itemPrice, discount, totalLineAmount));
            }

            double grandTotal = orderSubtotal + freight;

            // IN RA ORDERS
            System.out.printf(java.util.Locale.US,
                "INSERT INTO orders (order_id, user_id, order_date, required_date, shipped_date, ship_address_id, freight, status, total_amount) " +
                "VALUES (%d, %d, '%s', CURRENT_DATE + INTERVAL '3 days', %s, %d, %.2f, '%s', %.2f);\n",
                i, orderUserId, orderDateStr, orderStatus.equals("delivered") ? "CURRENT_DATE" : "NULL", 
                shipAddrId, freight, orderStatus, grandTotal
            );

            System.out.print(orderDetailsBuilder.toString());

            // IN RA PAYMENTS
            String pMethod = paymentMethods[random.nextInt(paymentMethods.length)];
            String pStatus = orderStatus.equals("delivered") ? "completed" : (orderStatus.equals("cancelled") ? "failed" : "pending");
            System.out.printf(java.util.Locale.US,
                "INSERT INTO payments (payment_id, order_id, payment_method, payment_date, amount, status) VALUES (%d, %d, '%s', %s, %.2f, '%s');\n",
                paymentCounter++, i, pMethod, pStatus.equals("completed") ? "CURRENT_TIMESTAMP" : "NULL", grandTotal, pStatus
            );

            // IN RA DELIVERIES
            if (!orderStatus.equals("pending") && !orderStatus.equals("cancelled")) {
                Long assignedCarrierId = getRandom(carrierIds, random);
                Long assignedShipperId = getRandom(shipperIds, random);
                String delStatus = orderStatus.equals("delivered") ? "delivered" : (orderStatus.equals("shipping") ? "shipping" : "picking");

                System.out.printf(java.util.Locale.US,
                    "INSERT INTO deliveries (delivery_id, order_id, carrier_id, shipper_id, delivery_status, tracking_num, ship_address, estimated_delivery_date) " +
                    "VALUES (%d, %d, %d, %d, '%s', 'TRACK%06d', '%s', CURRENT_DATE + INTERVAL '2 days');\n",
                    deliveryCounter++, i, assignedCarrierId, assignedShipperId, delStatus, 
                    i * 1000 + random.nextInt(999), escape(faker.address().streetAddress())
                );
            }

            // 13. CHEN DANH GIA (REVIEWS)
            if (orderStatus.equals("delivered") && random.nextBoolean()) {
                System.out.printf(java.util.Locale.US,
                    "INSERT INTO reviews (review_id, user_id, product_id, rating, comment, review_date) VALUES (%d, %d, %d, %d, '%s', '%s');\n",
                    i, orderUserId, getRandom(productIds, random), faker.number().numberBetween(4, 6),
                    "San pham dong goi can than, giao hang Gen Z sieu nhanh, chat luong tuyet voi so voi tam gia!", orderDateStr
                );
            }
        }

        System.out.println("\n-- ====================================================================");
        System.out.println("-- HOAN TAT SINH TAP DU LIEU DONG BO CHO SAN THUONG MAI DIEN TU !");
        System.out.println("-- ====================================================================");

        System.err.println("-> [THANH CONG] Du lieu sach (KHONG DAU) da duoc ghi vao file: '" + outputFileName + "'!");
    }
}