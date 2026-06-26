package utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/group6_project_e_commerce";
    private static String dbUsername;
    private static String dbPassword;

    public static void setCredentials(String username, String password) {
        dbUsername = username;
        dbPassword = password;
    }

    public static String getDbUsername() {
        return dbUsername;
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    public static Connection getConnection() throws SQLException {
        if (dbUsername == null || dbPassword == null) {
            throw new SQLException("Thông tin tài khoản Database chưa được thiết lập!");
        }
        return DriverManager.getConnection(URL, dbUsername, dbPassword);
    }

    // CẢI TIẾN: Trả về lỗi chi tiết từ Driver/PostgreSQL thay vì chỉ trả về true/false
    public static void testConnection(String username, String password) throws SQLException {
        // Nạp Driver ép buộc để tránh lỗi thiếu thư viện trên một số IDE
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy thư viện Driver PostgreSQL! Bạn đã thêm file .jar của PostgreSQL vào Libraries/Classpath của dự án chưa?");
        }
        
        try (Connection conn = DriverManager.getConnection(URL, username, password)) {
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Kết nối được thiết lập nhưng đã đóng hoặc không hợp lệ.");
            }
        }
    }
}
