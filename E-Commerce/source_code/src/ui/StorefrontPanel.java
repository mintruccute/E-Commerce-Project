package ui;

import model.UserSession;
import utils.DatabaseManager;
import utils.UIStyleUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

public class StorefrontPanel extends JPanel {
    private JPanel categoryRowPanel;
    private JPanel productGridPanel;
    private Runnable onShowCart;
    private JTextField txtSearch;

    public StorefrontPanel(Runnable onShowCart) {
        this.onShowCart = onShowCart;
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout(15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 115, 0), width, height, new Color(238, 77, 45));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        header.setOpaque(false);

        JLabel logo = new JLabel("E-MALL VN");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logo.setForeground(Color.WHITE);
        header.add(logo, BorderLayout.WEST);

        JPanel searchPanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        searchPanelWrapper.setOpaque(false);
        JPanel searchPanel = new JPanel(new BorderLayout(0, 0));
        searchPanel.setOpaque(false);
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(400, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                new EmptyBorder(5, 15, 5, 15)
        ));
        JButton btnSearch = UIStyleUtils.createStyledButton("Tìm kiếm", new Color(255, 193, 7), Color.BLACK);
        btnSearch.setPreferredSize(new Dimension(130, 38));
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        searchPanelWrapper.add(searchPanel);
        header.add(searchPanelWrapper, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        JButton btnCart = UIStyleUtils.createStyledButton("Giỏ Hàng", Color.DARK_GRAY, Color.WHITE);
        btnCart.setPreferredSize(new Dimension(130, 38));
        JLabel lblUser = new JLabel("Chào, " + UserSession.fullName);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(Color.WHITE);
        rightPanel.add(btnCart);
        rightPanel.add(lblUser);
        header.add(rightPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        
        categoryRowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        categoryRowPanel.setBackground(Color.WHITE);

        JScrollPane categoryScroll = new JScrollPane(categoryRowPanel);
        categoryScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        categoryScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        categoryScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        categoryScroll.setPreferredSize(new Dimension(1000, 60));

        body.add(categoryScroll, BorderLayout.NORTH);

        productGridPanel = new JPanel(new GridLayout(0, 4, 15, 15));
        productGridPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JScrollPane scroll = new JScrollPane(productGridPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        categoryScroll.getHorizontalScrollBar().setUnitIncrement(20);
        body.add(scroll, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> loadProducts(txtSearch.getText().trim(), null));
        btnCart.addActionListener(e -> {
            if (onShowCart != null) onShowCart.run();
        });

        loadCategories();
    }

    private void loadCategories() {
        categoryRowPanel.removeAll();
        
        JButton btnAll = UIStyleUtils.createStyledButton("Tất cả sản phẩm", Color.WHITE, Color.BLACK);
        btnAll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            new EmptyBorder(8, 20, 8, 20)
        ));
        btnAll.addActionListener(e -> loadProducts(null, null));
        categoryRowPanel.add(btnAll);

        String sql = "SELECT category_id, category_name FROM categories";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Long catId = rs.getLong("category_id");
                String catName = rs.getString("category_name");
                JButton btnCat = UIStyleUtils.createStyledButton(catName, Color.WHITE, Color.BLACK);
                btnCat.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230)),
                    new EmptyBorder(8, 20, 8, 20)
                ));
                btnCat.addActionListener(e -> loadProducts(null, catId));
                categoryRowPanel.add(btnCat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        categoryRowPanel.revalidate();
        categoryRowPanel.repaint();
    }

    public void loadProducts(String searchKey, Long categoryId) {
        productGridPanel.removeAll();
        ArrayList<Object> params = new ArrayList<>();
        
        StringBuilder query = new StringBuilder(
                "SELECT product_id, product_name, price, old_price, buy_turn, category_id FROM products WHERE discontinued = FALSE"
        );

        if (searchKey != null && !searchKey.isEmpty()) {
            query.append(" AND LOWER(product_name) LIKE ?");
            params.add("%" + searchKey.toLowerCase() + "%");
        }
        if (categoryId != null) {
            query.append(" AND category_id = ?");
            params.add(categoryId);
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long prodId = rs.getLong("product_id");
                    String name = rs.getString("product_name");
                    BigDecimal price = rs.getBigDecimal("price");
                    BigDecimal oldPrice = rs.getBigDecimal("old_price");
                    int buyTurn = rs.getInt("buy_turn");
                    Long catId = rs.getLong("category_id");

                    JPanel card = new JPanel(new BorderLayout(5, 5));
                    card.setBackground(Color.WHITE);
                    javax.swing.border.Border defaultBorder = new UIStyleUtils.RoundedBorder(new Color(230, 230, 230), 15, 2);
                    javax.swing.border.Border hoverBorder = new UIStyleUtils.RoundedBorder(new Color(238, 77, 45), 15, 2);
                    card.setBorder(defaultBorder);
                    card.setBackground(Color.WHITE);
                    card.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent evt) {
                            card.setBorder(hoverBorder);
                        }
                        public void mouseExited(java.awt.event.MouseEvent evt) {
                            card.setBorder(defaultBorder);
                        }
                    });

                    JLabel lblImg = new JLabel();
                    ImageIcon productIcon = getProductImage(prodId, catId);
                    if (productIcon != null) {
                        lblImg.setIcon(productIcon);
                    } else {
                        lblImg.setText("Ảnh sản phẩm");
                    }
                    lblImg.setHorizontalAlignment(JLabel.CENTER);
                    lblImg.setPreferredSize(new Dimension(150, 110));
                    lblImg.setBackground(new Color(245, 245, 245));
                    lblImg.setOpaque(true);
                    card.add(lblImg, BorderLayout.NORTH);

                    JPanel detail = new JPanel(new GridLayout(4, 1));
                    detail.setBackground(Color.WHITE);

                    JLabel lblName = new JLabel("<html><b>" + name + "</b></html>");
                    lblName.setBorder(new EmptyBorder(2, 5, 2, 5));

                    String priceLabel = String.format("%,.0fđ", price);
                    if (oldPrice != null && oldPrice.compareTo(BigDecimal.ZERO) > 0) {
                        double disc = (oldPrice.subtract(price)).doubleValue() / oldPrice.doubleValue() * 100;
                        priceLabel += " <font color='red'>(-" + (int) Math.round(disc) + "%)</font>";
                    }
                    JLabel lblPrice = new JLabel("<html><font color='orange'>" + priceLabel + "</font></html>");
                    lblPrice.setBorder(new EmptyBorder(0, 5, 0, 5));

                    String oldPriceLabel = oldPrice != null ? String.format("%,.0fđ", oldPrice) : "";
                    JLabel lblOldPrice = new JLabel("<html><strike>" + oldPriceLabel + "</strike></html>");
                    lblOldPrice.setForeground(Color.GRAY);
                    lblOldPrice.setBorder(new EmptyBorder(0, 5, 0, 5));

                    JLabel lblSales = new JLabel("Lượt bán: " + buyTurn);
                    lblSales.setFont(new Font("Arial", Font.PLAIN, 10));
                    lblSales.setBorder(new EmptyBorder(0, 5, 2, 5));

                    detail.add(lblName);
                    detail.add(lblPrice);
                    detail.add(lblOldPrice);
                    detail.add(lblSales);

                    card.add(detail, BorderLayout.CENTER);

                    JButton btnAdd = UIStyleUtils.createStyledButton("Thêm vào giỏ", new Color(238, 77, 45), Color.WHITE);
                    btnAdd.addActionListener(e -> addToCart(prodId));
                    card.add(btnAdd, BorderLayout.SOUTH);

                    productGridPanel.add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        productGridPanel.revalidate();
        productGridPanel.repaint();
    }

    private void addToCart(Long productId) {
        String queryStock = "SELECT product_name, quantity FROM products WHERE product_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmtStock = conn.prepareStatement(queryStock)) {
            
            pstmtStock.setLong(1, productId);
            try (ResultSet rsStock = pstmtStock.executeQuery()) {
                if (rsStock.next()) {
                    String productName = rsStock.getString("product_name");
                    int availableStock = rsStock.getInt("quantity");

                    if (availableStock <= 0) {
                        JOptionPane.showMessageDialog(this, 
                                "Sản phẩm \"" + productName + "\" hiện đã HẾT HÀNG trong kho!", 
                                "Thông báo hết hàng", 
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    JPanel qtyPanel = new JPanel(new GridLayout(2, 1, 5, 5));
                    qtyPanel.add(new JLabel("<html>Nhập số lượng cho sản phẩm: <b>" + productName + "</b></html>"));
                    
                    JTextField txtQty = new JTextField("1", 3);
                    txtQty.setHorizontalAlignment(JTextField.CENTER);
                    txtQty.setFont(new Font("Arial", Font.BOLD, 14));
                    txtQty.setPreferredSize(new Dimension(50, 30));
                    txtQty.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    
                    JButton btnMinus = new JButton("-");
                    btnMinus.setPreferredSize(new Dimension(30, 30));
                    btnMinus.setBackground(Color.WHITE);
                    btnMinus.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    btnMinus.setFocusPainted(false);
                    
                    JButton btnPlus = new JButton("+");
                    btnPlus.setPreferredSize(new Dimension(30, 30));
                    btnPlus.setBackground(Color.WHITE);
                    btnPlus.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    btnPlus.setFocusPainted(false);
                    
                    btnMinus.addActionListener(e -> {
                        try {
                            int current = Integer.parseInt(txtQty.getText());
                            if (current > 1) txtQty.setText(String.valueOf(current - 1));
                        } catch(Exception ex){ txtQty.setText("1"); }
                    });
                    
                    btnPlus.addActionListener(e -> {
                        try {
                            int current = Integer.parseInt(txtQty.getText());
                            if (current < availableStock) txtQty.setText(String.valueOf(current + 1));
                        } catch(Exception ex){ txtQty.setText("1"); }
                    });
                    
                    JPanel spinnerWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    spinnerWrapper.add(btnMinus);
                    spinnerWrapper.add(txtQty);
                    spinnerWrapper.add(btnPlus);
                    
                    JPanel qtyOuter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                    qtyOuter.add(spinnerWrapper);
                    qtyOuter.add(new JLabel("<html><font color='gray'>(Kho: " + availableStock + ")</font></html>"));
                    qtyPanel.add(qtyOuter);

                    int selection = JOptionPane.showConfirmDialog(
                            this, 
                            qtyPanel, 
                            "CHỌN SỐ LƯỢNG SẢN PHẨM", 
                            JOptionPane.OK_CANCEL_OPTION, 
                            JOptionPane.PLAIN_MESSAGE
                    );

                    if (selection == JOptionPane.OK_OPTION) {
                        int quantityToAdd;
                        try { quantityToAdd = Integer.parseInt(txtQty.getText()); } 
                        catch (Exception ex) { quantityToAdd = 1; }

                        String checkCartSql = "SELECT cart_item_id, quantity FROM cart_items WHERE cart_id = ? AND product_id = ?";
                        try (PreparedStatement pstmtCheck = conn.prepareStatement(checkCartSql)) {
                            pstmtCheck.setLong(1, UserSession.cartId);
                            pstmtCheck.setLong(2, productId);
                            
                            try (ResultSet rsCart = pstmtCheck.executeQuery()) {
                                if (rsCart.next()) {
                                    String updateSql = "UPDATE cart_items SET quantity = quantity + ? WHERE cart_item_id = ?";
                                    try (PreparedStatement upPstmt = conn.prepareStatement(updateSql)) {
                                        upPstmt.setInt(1, quantityToAdd);
                                        upPstmt.setLong(2, rsCart.getLong("cart_item_id"));
                                        upPstmt.executeUpdate();
                                    }
                                } else {
                                    String insertSql = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?)";
                                    try (PreparedStatement insPstmt = conn.prepareStatement(insertSql)) {
                                        insPstmt.setLong(1, UserSession.cartId);
                                        insPstmt.setLong(2, productId);
                                        insPstmt.setInt(3, quantityToAdd);
                                        insPstmt.executeUpdate();
                                    }
                                }
                                showToast("Đã thêm thành công " + quantityToAdd + " sản phẩm vào Giỏ hàng!");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kiểm tra giỏ hàng: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
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
            case 7: return "the thao va du lich";
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
