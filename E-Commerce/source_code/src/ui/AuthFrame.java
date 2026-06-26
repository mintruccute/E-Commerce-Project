package ui;
import model.*;
import utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class AuthFrame extends JFrame {
    private JTabbedPane tabbedPane;

    public AuthFrame() {
        setTitle("Hệ Thống Xác Thực - E-Commerce");
        setSize(500, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        initLoginTab();
        initRegisterTab();

        add(tabbedPane);
    }

    private void initLoginTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));

        JTextField txtUser = new JTextField(15);
        JPasswordField txtPass = new JPasswordField(15);
        
        JButton btnLogin = UIStyleUtils.createStyledButton("Đăng Nhập", new Color(238, 77, 45), Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1; panel.add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; panel.add(txtPass, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btnLogin, gbc);

        btnLogin.addActionListener(e -> {
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "SELECT u.user_id, u.first_name, u.last_name, r.role_name " +
                         "FROM users u " +
                         "JOIN user_roles ur ON u.user_id = ur.user_id " +
                         "JOIN roles r ON ur.role_id = r.role_id " +
                         "WHERE u.username = ? AND u.password = ? AND u.deleted = FALSE";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, username);
                pstmt.setString(2, password);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        UserSession.userId = rs.getLong("user_id");
                        UserSession.username = username;
                        UserSession.fullName = rs.getString("first_name") + " " + rs.getString("last_name");

                        getOrCreateCart(conn, UserSession.userId);

                        JOptionPane.showMessageDialog(this, 
                                "Đăng nhập thành công!\nChào mừng " + UserSession.fullName + " đến với Sàn TMĐT!",
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        
                        openECommerceWindow();
                        this.dispose(); 
                        
                    } else {
                        JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Thất bại", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        });

        tabbedPane.addTab("Đăng Nhập", panel);
    }

    private void getOrCreateCart(Connection conn, Long userId) throws SQLException {
        String selectCart = "SELECT cart_id FROM carts WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectCart)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserSession.cartId = rs.getLong("cart_id");
                } else {
                    String insertCart = "INSERT INTO carts (user_id) VALUES (?) RETURNING cart_id";
                    try (PreparedStatement psIns = conn.prepareStatement(insertCart)) {
                        psIns.setLong(1, userId);
                        try (ResultSet rsIns = psIns.executeQuery()) {
                            if (rsIns.next()) UserSession.cartId = rsIns.getLong("cart_id");
                        }
                    }
                }
            }
        }
    }

    private void initRegisterTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("ĐĂNG KÝ TÀI KHOẢN MỚI", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));

        JTextField txtUser = new JTextField(15);
        JTextField txtEmail = new JTextField(15);
        JPasswordField txtPass = new JPasswordField(15);
        JTextField txtFirstName = new JTextField(15);
        JTextField txtLastName = new JTextField(15);
        JTextField txtPhone = new JTextField(15);
        JTextField txtDOB = new JTextField("2000-01-01", 15);
        
        JButton btnRegister = UIStyleUtils.createStyledButton("Đăng Ký Ngay", new Color(0, 123, 255), Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Tên đăng nhập:"), gbc); gbc.gridx = 1; panel.add(txtUser, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Email:"), gbc); gbc.gridx = 1; panel.add(txtEmail, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Mật khẩu:"), gbc); gbc.gridx = 1; panel.add(txtPass, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Tên (First Name):"), gbc); gbc.gridx = 1; panel.add(txtFirstName, gbc);
        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Họ (Last Name):"), gbc); gbc.gridx = 1; panel.add(txtLastName, gbc);
        gbc.gridx = 0; gbc.gridy = 6; panel.add(new JLabel("Số điện thoại:"), gbc); gbc.gridx = 1; panel.add(txtPhone, gbc);
        gbc.gridx = 0; gbc.gridy = 7; panel.add(new JLabel("Ngày sinh (YYYY-MM-DD):"), gbc); gbc.gridx = 1; panel.add(txtDOB, gbc);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        panel.add(btnRegister, gbc);

        btnRegister.addActionListener(e -> {
            String username = txtUser.getText().trim();
            String email = txtEmail.getText().trim();
            String password = new String(txtPass.getPassword()).trim();
            String firstName = txtFirstName.getText().trim();
            String lastName = txtLastName.getText().trim();
            String phone = txtPhone.getText().trim();
            String dob = txtDOB.getText().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Connection conn = null;
            try {
                conn = DatabaseManager.getConnection();
                conn.setAutoCommit(false);

                long nextUserId = 1;
                String maxUserSql = "SELECT COALESCE(MAX(user_id), 0) + 1 FROM users";
                try (PreparedStatement pstmtMax = conn.prepareStatement(maxUserSql);
                     ResultSet rs = pstmtMax.executeQuery()) {
                    if (rs.next()) nextUserId = rs.getLong(1);
                }

                String insertUserSql = "INSERT INTO users (user_id, username, email, password, first_name, last_name, phone, date_of_birth, created_at, deleted) " +
                                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, FALSE)";
                try (PreparedStatement pstmtUser = conn.prepareStatement(insertUserSql)) {
                    pstmtUser.setLong(1, nextUserId);
                    pstmtUser.setString(2, username);
                    pstmtUser.setString(3, email);
                    pstmtUser.setString(4, password);
                    pstmtUser.setString(5, firstName);
                    pstmtUser.setString(6, lastName);
                    pstmtUser.setString(7, phone);
                    pstmtUser.setDate(8, Date.valueOf(dob));
                    pstmtUser.executeUpdate();
                }

                String insertRoleSql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, 1)";
                try (PreparedStatement pstmtRole = conn.prepareStatement(insertRoleSql)) {
                    pstmtRole.setLong(1, nextUserId);
                    pstmtRole.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "Đăng ký thành công tài khoản: " + username, "Thành công", JOptionPane.INFORMATION_MESSAGE);

                txtUser.setText(""); txtEmail.setText(""); txtPass.setText("");
                txtFirstName.setText(""); txtLastName.setText(""); txtPhone.setText("");
                tabbedPane.setSelectedIndex(0); 

            } catch (SQLException ex) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException rollbackEx) {}
                }
                JOptionPane.showMessageDialog(this, "Lỗi đăng ký: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Sai định dạng ngày sinh!", "Lỗi ngày sinh", JOptionPane.ERROR_MESSAGE);
            }
        });

        tabbedPane.addTab("Đăng Ký", panel);
    }

    private void openECommerceWindow() {
        SwingUtilities.invokeLater(() -> {
            new ECommerceFrame().setVisible(true);
        });
    }
}
