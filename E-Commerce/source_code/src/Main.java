import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.plaf.FontUIResource;
import ui.*;
import utils.*;
import java.util.Enumeration;

public class Main {
    public static void main(String[] args) {
        // Thiết lập giao diện tương thích hệ điều hành
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            FontUIResource font = new FontUIResource(new Font("Segoe UI", Font.PLAIN, 14));
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof FontUIResource) {
                    UIManager.put(key, font);
                }
            }
        } catch (Exception ignored) {}

        // Khởi tạo hộp thoại bảo mật kết nối Database
        JPanel dbPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        dbPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextField txtDbUser = new JTextField("postgres");
        JPasswordField txtDbPass = new JPasswordField(); // Che mật khẩu dạng ****
        
        dbPanel.add(new JLabel("Database Username:"));
        dbPanel.add(txtDbUser);
        dbPanel.add(new JLabel("Database Password:"));
        dbPanel.add(txtDbPass);

        int result = JOptionPane.showConfirmDialog(
                null, 
                dbPanel, 
                "KẾT NỐI CƠ SỞ DỮ LIỆU POSTGRESQL", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String dbUsername = txtDbUser.getText().trim();
            String dbPassword = new String(txtDbPass.getPassword()).trim();

            // CẢI TIẾN: Bắt lỗi chi tiết và hiển thị nguyên nhân thực tế lên Dialog
            try {
                DatabaseManager.testConnection(dbUsername, dbPassword);
                DatabaseManager.setCredentials(dbUsername, dbPassword);
                System.out.println("Kết nối Database PostgreSQL thành công!");
                
                // Mở cửa sổ Đăng ký / Đăng nhập
                SwingUtilities.invokeLater(() -> {
                    new AuthFrame().setVisible(true);
                });
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        null, 
                        "Không thể kết nối đến Database!\n\nChi tiết nguyên nhân lỗi:\n" + e.getMessage(), 
                        "Lỗi Kết Nối CSDL Thực Tế", 
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        } else {
            System.exit(0);
        }
    }
}
