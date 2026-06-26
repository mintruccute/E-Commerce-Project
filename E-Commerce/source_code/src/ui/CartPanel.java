package ui;

import model.AddressItem;
import model.UserSession;
import utils.DatabaseManager;
import utils.UIStyleUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class CartPanel extends JPanel {

    public interface CheckoutSuccessListener {
        void onSuccess(Long orderId, BigDecimal total, String tracking, String carrier, String shipper, String paymentMethod);
    }

    private JPanel cartListPanel;
    private JLabel lblCartTotal;
    private JComboBox<AddressItem> comboAddresses;
    private JButton btnAddNewAddress;

    private Runnable onBackToStorefront;
    private CheckoutSuccessListener onSuccess;

    public CartPanel(Runnable onBackToStorefront, CheckoutSuccessListener onSuccess) {
        this.onBackToStorefront = onBackToStorefront;
        this.onSuccess = onSuccess;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        JLabel title = new JLabel("Giỏ Hàng", JLabel.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(238, 77, 45));
        headerPanel.add(title, BorderLayout.WEST);
        
        JButton btnClearCart = UIStyleUtils.createStyledButton("Xoá tất cả", Color.WHITE, Color.RED);
        btnClearCart.setBorder(BorderFactory.createLineBorder(Color.RED));
        btnClearCart.addActionListener(e -> clearCart());
        headerPanel.add(btnClearCart, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        
        JPanel tableHeader = new JPanel(new GridBagLayout());
        tableHeader.setBackground(Color.WHITE);
        tableHeader.setBorder(new EmptyBorder(15, 10, 15, 10));
        GridBagConstraints gh = new GridBagConstraints();
        gh.fill = GridBagConstraints.HORIZONTAL;
        gh.weightx = 1.0; gh.gridx = 0; tableHeader.add(new JLabel("Sản Phẩm"), gh);
        gh.weightx = 0.2; gh.gridx = 1; tableHeader.add(new JLabel("Đơn Giá", JLabel.CENTER), gh);
        gh.weightx = 0.2; gh.gridx = 2; tableHeader.add(new JLabel("Số Lượng", JLabel.CENTER), gh);
        gh.weightx = 0.2; gh.gridx = 3; tableHeader.add(new JLabel("Số Tiền", JLabel.CENTER), gh);
        gh.weightx = 0.1; gh.gridx = 4; tableHeader.add(new JLabel("Thao Tác", JLabel.CENTER), gh);
        centerWrapper.add(tableHeader, BorderLayout.NORTH);

        cartListPanel = new JPanel();
        cartListPanel.setLayout(new BoxLayout(cartListPanel, BoxLayout.Y_AXIS));
        cartListPanel.setBackground(new Color(245, 245, 245));
        JScrollPane scroll = new JScrollPane(cartListPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        scroll.getViewport().setBackground(new Color(245, 245, 245));
        centerWrapper.add(scroll, BorderLayout.CENTER);
        
        add(centerWrapper, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(20, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel addressPanel = new JPanel(new BorderLayout(10, 0));
        addressPanel.setOpaque(false);
        addressPanel.add(new JLabel("Địa chỉ nhận hàng:"), BorderLayout.WEST);
        comboAddresses = new JComboBox<>();
        btnAddNewAddress = UIStyleUtils.createStyledButton("+ Thêm địa chỉ mới", new Color(0, 123, 255), Color.WHITE);
        addressPanel.add(comboAddresses, BorderLayout.CENTER);
        addressPanel.add(btnAddNewAddress, BorderLayout.EAST);
        footer.add(addressPanel, BorderLayout.NORTH);

        JPanel checkoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        checkoutPanel.setOpaque(false);
        lblCartTotal = new JLabel("Tổng thanh toán (0 Sản phẩm): ₫0", JLabel.RIGHT);
        lblCartTotal.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JButton btnOrder = UIStyleUtils.createStyledButton("Mua Hàng", new Color(238, 77, 45), Color.WHITE);
        btnOrder.setFont(new Font("Arial", Font.BOLD, 16));
        btnOrder.setPreferredSize(new Dimension(150, 45));
        
        checkoutPanel.add(lblCartTotal);
        checkoutPanel.add(btnOrder);
        footer.add(checkoutPanel, BorderLayout.EAST);

        JButton btnBack = UIStyleUtils.createStyledButton("Quay lại Cửa hàng", Color.WHITE, Color.BLACK);
        btnBack.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        footer.add(btnBack, BorderLayout.WEST);

        add(footer, BorderLayout.SOUTH);

        btnBack.addActionListener(e -> {
            if (onBackToStorefront != null) onBackToStorefront.run();
        });
        btnAddNewAddress.addActionListener(e -> openNewAddressDialog());
        
        btnOrder.addActionListener(e -> {
            AddressItem selected = (AddressItem) comboAddresses.getSelectedItem();
            if (selected == null || selected.addressId == -1L) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hoặc thêm một địa chỉ để nhận hàng!", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String[] options = {"Thanh toán khi nhận hàng (COD)", "Chuyển khoản Ngân hàng", "Ví điện tử MoMo", "Thẻ Tín Dụng / Ghi nợ"};
            String paymentMethod = (String) JOptionPane.showInputDialog(
                    this,
                    "Vui lòng chọn phương thức thanh toán:\n",
                    "Bước Thanh Toán",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                processCheckout(selected.addressId, paymentMethod);
            }
        });
    }

    private void loadUserAddresses(Long selectAddressId) {
        comboAddresses.removeAllItems();
        boolean hasAddress = false;
        AddressItem toSelect = null;

        String sql = "SELECT address_id, street, city, country, status FROM addresses WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, UserSession.userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("address_id");
                    String street = rs.getString("street");
                    String city = rs.getString("city");
                    String country = rs.getString("country");
                    String status = rs.getString("status");

                    String fullText = street + ", " + city + ", " + country;
                    if ("default".equals(status)) {
                        fullText += " [Mặc định]";
                    }

                    AddressItem item = new AddressItem(id, fullText);
                    comboAddresses.addItem(item);
                    hasAddress = true;

                    if (selectAddressId != null && selectAddressId.equals(id)) {
                        toSelect = item;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!hasAddress) {
            comboAddresses.addItem(new AddressItem(-1L, "Bạn chưa có địa chỉ nhận hàng nào. Vui lòng thêm mới!"));
        } else if (toSelect != null) {
            comboAddresses.setSelectedItem(toSelect);
        }
    }

    private void openNewAddressDialog() {
        JPanel dialogPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        dialogPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField txtStreet = new JTextField();
        JTextField txtCity = new JTextField("Hanoi");
        JTextField txtCountry = new JTextField("Vietnam");

        dialogPanel.add(new JLabel("Số nhà, Tên đường (*):"));
        dialogPanel.add(txtStreet);
        dialogPanel.add(new JLabel("Thành phố (*):"));
        dialogPanel.add(txtCity);
        dialogPanel.add(new JLabel("Quốc gia (*):"));
        dialogPanel.add(txtCountry);

        int result = JOptionPane.showConfirmDialog(
                this, 
                dialogPanel, 
                "THÊM ĐỊA CHỈ NHẬN HÀNG MỚI", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String street = txtStreet.getText().trim();
            String city = txtCity.getText().trim();
            String country = txtCountry.getText().trim();

            if (street.isEmpty() || city.isEmpty() || country.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không được bỏ trống các thông tin bắt buộc!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String insertSql = "INSERT INTO addresses (user_id, street, city, country, address_type, status) " +
                               "VALUES (?, ?, ?, ?, 'shipping'::address_type_enum, 'active'::address_status_enum) RETURNING address_id";
            
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setLong(1, UserSession.userId);
                pstmt.setString(2, street);
                pstmt.setString(3, city);
                pstmt.setString(4, country);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Long newAddressId = rs.getLong("address_id");
                        JOptionPane.showMessageDialog(this, "Thêm địa chỉ giao hàng thành công!");
                        loadUserAddresses(newAddressId);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Không thể lưu địa chỉ: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadCartItems() {
        cartListPanel.removeAll();
        BigDecimal total = BigDecimal.ZERO;
        int totalQuantity = 0;

        String sql = "SELECT ci.cart_item_id, ci.product_id, p.product_name, p.price, ci.quantity, p.category_id FROM cart_items ci " +
                     "JOIN products p ON ci.product_id = p.product_id WHERE ci.cart_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, UserSession.cartId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long cartItemId = rs.getLong("cart_item_id");
                    Long productId = rs.getLong("product_id");
                    Long catId = rs.getLong("category_id");
                    String name = rs.getString("product_name");
                    BigDecimal price = rs.getBigDecimal("price");
                    int quantity = rs.getInt("quantity");
                    BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
                    total = total.add(subtotal);
                    totalQuantity += quantity;

                    JPanel row = new JPanel(new GridBagLayout());
                    row.setBackground(Color.WHITE);
                    row.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                            new EmptyBorder(15, 10, 15, 10)
                    ));
                    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.VERTICAL;
                    gbc.insets = new Insets(0, 5, 0, 5);

                    ImageIcon icon = getProductImage(productId, catId);
                    if (icon != null) {
                        Image scaledImg = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                        icon = new ImageIcon(scaledImg);
                    }
                    JLabel lblImg = new JLabel(icon != null ? icon : new ImageIcon());
                    lblImg.setPreferredSize(new Dimension(60, 60));
                    lblImg.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
                    gbc.gridx = 0; gbc.weightx = 0; row.add(lblImg, gbc);

                    JLabel lblName = new JLabel("<html><div style='width: 250px;'>" + name + "</div></html>");
                    lblName.setFont(new Font("Arial", Font.PLAIN, 14));
                    gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; row.add(lblName, gbc);

                    JLabel lblPrice = new JLabel(String.format("₫%,.0f", price), JLabel.CENTER);
                    lblPrice.setFont(new Font("Arial", Font.PLAIN, 14));
                    gbc.gridx = 2; gbc.weightx = 0.2; row.add(lblPrice, gbc);

                    JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                    qtyPanel.setOpaque(false);
                    JButton btnMinus = new JButton("-"); btnMinus.setPreferredSize(new Dimension(25, 25));
                    btnMinus.setBackground(Color.WHITE); btnMinus.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    btnMinus.setFocusPainted(false);
                    JTextField txtQty = new JTextField(String.valueOf(quantity), 2); 
                    txtQty.setHorizontalAlignment(JTextField.CENTER); txtQty.setPreferredSize(new Dimension(35, 25));
                    txtQty.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    txtQty.setEditable(false);
                    txtQty.setBackground(Color.WHITE);
                    JButton btnPlus = new JButton("+"); btnPlus.setPreferredSize(new Dimension(25, 25));
                    btnPlus.setBackground(Color.WHITE); btnPlus.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    btnPlus.setFocusPainted(false);
                    qtyPanel.add(btnMinus); qtyPanel.add(txtQty); qtyPanel.add(btnPlus);
                    
                    btnMinus.addActionListener(e -> updateCartQuantity(cartItemId, quantity - 1));
                    btnPlus.addActionListener(e -> updateCartQuantity(cartItemId, quantity + 1));
                    
                    gbc.gridx = 3; gbc.weightx = 0.2; row.add(qtyPanel, gbc);

                    JLabel lblTotal = new JLabel("<html><font color='#EE4D2D'>" + String.format("₫%,.0f", subtotal) + "</font></html>", JLabel.CENTER);
                    lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
                    gbc.gridx = 4; gbc.weightx = 0.2; row.add(lblTotal, gbc);

                    JButton btnDel = new JButton("Xoá");
                    btnDel.setForeground(Color.BLACK);
                    btnDel.setContentAreaFilled(false);
                    btnDel.setBorderPainted(false);
                    btnDel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    btnDel.setFocusPainted(false);
                    btnDel.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent evt) { btnDel.setForeground(Color.RED); }
                        public void mouseExited(java.awt.event.MouseEvent evt) { btnDel.setForeground(Color.BLACK); }
                    });
                    btnDel.addActionListener(e -> deleteCartItem(cartItemId));
                    gbc.gridx = 5; gbc.weightx = 0.1; row.add(btnDel, gbc);

                    cartListPanel.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        lblCartTotal.setText("<html>Tổng thanh toán (" + totalQuantity + " Sản phẩm): <font color='#EE4D2D' size='5'><b>₫" + String.format("%,.0f", total) + "</b></font></html>");
        cartListPanel.revalidate();
        cartListPanel.repaint();
        
        loadUserAddresses(null);
    }

    private void updateCartQuantity(Long cartItemId, int newQty) {
        if (newQty <= 0) {
            deleteCartItem(cartItemId);
            return;
        }
        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_item_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQty);
            pstmt.setLong(2, cartItemId);
            pstmt.executeUpdate();
            loadCartItems();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processCheckout(Long selectedAddressId, String paymentMethod) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String street = "", city = "", country = "";
                String findAddrSql = "SELECT street, city, country FROM addresses WHERE address_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(findAddrSql)) {
                    pstmt.setLong(1, selectedAddressId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            street = rs.getString("street");
                            city = rs.getString("city");
                            country = rs.getString("country");
                        }
                    }
                }
                String shippingAddressText = street + ", " + city + ", " + country;

                BigDecimal itemsTotal = BigDecimal.ZERO;
                String cartSql = "SELECT ci.product_id, p.price, ci.quantity FROM cart_items ci " +
                                 "JOIN products p ON ci.product_id = p.product_id WHERE ci.cart_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(cartSql)) {
                    pstmt.setLong(1, UserSession.cartId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            BigDecimal price = rs.getBigDecimal("price");
                            int qty = rs.getInt("quantity");
                            itemsTotal = itemsTotal.add(price.multiply(BigDecimal.valueOf(qty)));
                        }
                    }
                }

                if (itemsTotal.compareTo(BigDecimal.ZERO) == 0) {
                    JOptionPane.showMessageDialog(this, "Giỏ hàng rỗng, không thể hoàn tất!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    conn.rollback();
                    return;
                }

                BigDecimal freight = BigDecimal.valueOf(15000);
                BigDecimal orderTotal = itemsTotal.add(freight);

                Long orderId = null;
                String orderSql = "INSERT INTO orders (user_id, required_date, ship_address_id, freight, status, total_amount) " +
                                  "VALUES (?, CURRENT_DATE + 3, ?, ?, 'pending'::order_status_enum, ?) RETURNING order_id";
                try (PreparedStatement pstmt = conn.prepareStatement(orderSql)) {
                    pstmt.setLong(1, UserSession.userId);
                    pstmt.setLong(2, selectedAddressId);
                    pstmt.setBigDecimal(3, freight);
                    pstmt.setBigDecimal(4, orderTotal);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) orderId = rs.getLong("order_id");
                    }
                }

                String odSql = "INSERT INTO order_details (order_id, product_id, quantity, price, total_amount) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmtCart = conn.prepareStatement(cartSql);
                     PreparedStatement pstmtOd = conn.prepareStatement(odSql)) {
                    pstmtCart.setLong(1, UserSession.cartId);
                    try (ResultSet rs = pstmtCart.executeQuery()) {
                        while (rs.next()) {
                            Long pId = rs.getLong("product_id");
                            BigDecimal price = rs.getBigDecimal("price");
                            int qty = rs.getInt("quantity");

                            pstmtOd.setLong(1, orderId);
                            pstmtOd.setLong(2, pId);
                            pstmtOd.setInt(3, qty);
                            pstmtOd.setBigDecimal(4, price);
                            pstmtOd.setBigDecimal(5, price.multiply(BigDecimal.valueOf(qty)));
                            pstmtOd.addBatch();
                        }
                        pstmtOd.executeBatch();
                    }
                }

                Long carrierId = null;
                String carrierName = "Chưa gán";
                Long shipperId = null;
                String shipperName = "Đang phân phối tài xế...";

                String queryCarrier = "SELECT carrier_id, carrier_name FROM carriers WHERE status = 'active'::carrier_status_enum LIMIT 1";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(queryCarrier)) {
                    if (rs.next()) {
                        carrierId = rs.getLong("carrier_id");
                        carrierName = rs.getString("carrier_name");
                    }
                }

                if (carrierId != null) {
                    String queryShipper = "SELECT shipper_id, shipper_name FROM shippers WHERE carrier_id = ? AND status = 'active'::shipper_status_enum LIMIT 1";
                    try (PreparedStatement pstmt = conn.prepareStatement(queryShipper)) {
                        pstmt.setLong(1, carrierId);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                shipperId = rs.getLong("shipper_id");
                                shipperName = rs.getString("shipper_name");

                                String updateShip = "UPDATE shippers SET status = 'busy'::shipper_status_enum WHERE shipper_id = ?";
                                try (PreparedStatement pstmtUp = conn.prepareStatement(updateShip)) {
                                    pstmtUp.setLong(1, shipperId);
                                    pstmtUp.executeUpdate();
                                }
                            }
                        }
                    }
                }

                String trackingNum = "TRK-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String delivSql = "INSERT INTO deliveries (order_id, carrier_id, shipper_id, delivery_status, tracking_num, ship_address) " +
                                  "VALUES (?, ?, ?, 'shipping'::delivery_status_enum, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(delivSql)) {
                    pstmt.setLong(1, orderId);
                    pstmt.setLong(2, carrierId);
                    if (shipperId != null) {
                        pstmt.setLong(3, shipperId);
                    } else {
                        pstmt.setNull(3, Types.BIGINT);
                    }
                    pstmt.setString(4, trackingNum);
                    pstmt.setString(5, shippingAddressText);
                    pstmt.executeUpdate();
                }

                String clearSql = "DELETE FROM cart_items WHERE cart_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(clearSql)) {
                    pstmt.setLong(1, UserSession.cartId);
                    pstmt.executeUpdate();
                }

                conn.commit();
                
                if (onSuccess != null) {
                    onSuccess.onSuccess(orderId, orderTotal, trackingNum, carrierName, shipperName, paymentMethod);
                }

            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Đặt hàng thất bại: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showToast(String message) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        if (parent == null) return;
        JWindow toast = new JWindow(parent);
        toast.setBackground(new Color(0, 0, 0, 0));
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));
        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label);
        toast.add(panel);
        toast.pack();
        toast.setLocationRelativeTo(parent);
        toast.setLocation(toast.getX(), parent.getY() + parent.getHeight() - 100);
        toast.setVisible(true);

        new Timer(2500, e -> {
            toast.setVisible(false);
            toast.dispose();
        }).start();
    }

    private void deleteCartItem(Long cartItemId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xoá sản phẩm này khỏi giỏ hàng?", "Xác nhận xoá", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM cart_items WHERE cart_item_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, cartItemId);
                pstmt.executeUpdate();
                showToast("Đã xoá sản phẩm khỏi giỏ hàng!");
                loadCartItems();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void clearCart() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xoá TOÀN BỘ giỏ hàng?", "Xác nhận xoá", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM cart_items WHERE cart_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, UserSession.cartId);
                pstmt.executeUpdate();
                showToast("Đã làm rỗng giỏ hàng!");
                loadCartItems();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static java.util.Map<Integer, java.util.List<String>> categoryImagesCache = null;

    private void preloadImages() {
        if (categoryImagesCache != null) return;
        categoryImagesCache = new java.util.HashMap<>();
        String[] possiblePaths = {
            "C:\\Users\\Admin\\OneDrive\\Documents\\photo for database prj\\",
            "../photo for database prj/",
            "photo for database prj/",
            "src/photo for database prj/",
            "../../photo for database prj/"
        };
        String basePath = "";
        for (String p : possiblePaths) {
            if (new java.io.File(p).exists()) {
                basePath = p;
                if (!basePath.endsWith("/") && !basePath.endsWith("\\")) {
                    basePath += java.io.File.separator;
                }
                break;
            }
        }
        if (basePath.isEmpty()) {
            basePath = "photo for database prj/"; // Fallback
        }
        
        for (int i = 1; i <= 15; i++) {
            String folderName = getCategoryFolderName(i);
            if (folderName == null) continue;
            java.io.File folder = new java.io.File(basePath + folderName);
            java.util.List<String> images = new java.util.ArrayList<>();
            if (folder.exists() && folder.isDirectory()) {
                java.io.File[] files = folder.listFiles();
                if (files != null) {
                    for (java.io.File f : files) {
                        if (f.isFile() && (f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".png") || f.getName().toLowerCase().endsWith(".jpeg"))) {
                            images.add(f.getAbsolutePath());
                        }
                    }
                }
            }
            categoryImagesCache.put(i, images);
        }
    }

    private String getCategoryFolderName(int catId) {
        switch (catId) {
            case 1: return "Dien thoai & may tinh";
            case 2: return "thoi trang";
            case 3: return "giay dep";
            case 4: return "sách & thiết bị học tập";
            case 5: return "my pham";
            case 6: return "do gia dung";
            case 7: return "the thao va du lịch";
            case 8: return "do choi & me va be";
            case 9: return "oto & xe may";
            case 10: return "dien gia dung";
            case 11: return "thiet bi dien tu";
            case 12: return "noi that & ngoai that";
            case 13: return "thuc pham va do uong";
            case 14: return "van phong pham";
            case 15: return "dich vu & the thao";
            default: return null;
        }
    }

    private ImageIcon getProductImage(long productId, Long categoryId) {
        preloadImages();
        int catId = (categoryId != null) ? categoryId.intValue() : 0;
        java.util.List<String> images = categoryImagesCache.get(catId);
        
        String path = null;
        if (images != null && !images.isEmpty()) {
            int index = (int) (productId % images.size());
            path = images.get(index);
        }

        if (path == null) {
            return null;
        }

        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage();
            Image scaledImg = img.getScaledInstance(150, 110, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (Exception e) {
            return null;
        }
    }
}
