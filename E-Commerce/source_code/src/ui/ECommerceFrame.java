package ui;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ECommerceFrame extends JFrame {
    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    private StorefrontPanel storefrontPanel;
    private CartPanel cartPanel;

    public ECommerceFrame() {
        setTitle("Sàn Thương Mại Điện Tử Mô Phỏng");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        storefrontPanel = new StorefrontPanel(() -> {
            cartPanel.loadCartItems();
            cardLayout.show(mainContentPanel, "CART");
        });

        cartPanel = new CartPanel(
            () -> {
                storefrontPanel.loadProducts(null, null);
                cardLayout.show(mainContentPanel, "STOREFRONT");
            },
            (orderId, total, tracking, carrier, shipper, paymentMethod) -> {
                showSuccessDetails(orderId, total, tracking, carrier, shipper, paymentMethod);
            }
        );

        mainContentPanel.add(storefrontPanel, "STOREFRONT");
        mainContentPanel.add(cartPanel, "CART");

        add(mainContentPanel);
        cardLayout.show(mainContentPanel, "STOREFRONT");
    }

    private void showSuccessDetails(Long orderId, BigDecimal total, String tracking, String carrier, String shipper, String paymentMethod) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 245, 245));

        JPanel resultPanel = new JPanel(new GridBagLayout());
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
            new utils.UIStyleUtils.RoundedBorder(new Color(230, 230, 230), 20, 1),
            new javax.swing.border.EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 167, 69));
                int s = 50;
                int x = (getWidth() - s) / 2;
                int y = (getHeight() - s) / 2;
                g2.fillOval(x, y, s, s);
                g2.setColor(Color.WHITE);
                g2.setStroke(new java.awt.BasicStroke(4, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 15, y + 25, x + 23, y + 35);
                g2.drawLine(x + 23, y + 35, x + 38, y + 15);
            }
        };
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setOpaque(false);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        resultPanel.add(iconPanel, gbc);

        JLabel title = new JLabel("ĐẶT HÀNG THÀNH CÔNG!", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(40, 167, 69));
        gbc.gridy = 1; resultPanel.add(title, gbc);

        JSeparator sep = new JSeparator();
        gbc.gridy = 2; resultPanel.add(sep, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 3; resultPanel.add(new JLabel("<html><b>Mã Đơn Hàng:</b></html>"), gbc);
        gbc.gridx = 1; resultPanel.add(new JLabel(String.valueOf(orderId)), gbc);

        gbc.gridx = 0; gbc.gridy = 4; resultPanel.add(new JLabel("<html><b>Tổng Thanh Toán:</b></html>"), gbc);
        JLabel lblTotal = new JLabel(String.format("%,.0fđ", total));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(new Color(238, 77, 45));
        gbc.gridx = 1; resultPanel.add(lblTotal, gbc);

        gbc.gridx = 0; gbc.gridy = 5; resultPanel.add(new JLabel("<html><b>Phương Thức:</b></html>"), gbc);
        gbc.gridx = 1; resultPanel.add(new JLabel(paymentMethod), gbc);

        gbc.gridx = 0; gbc.gridy = 6; resultPanel.add(new JLabel("<html><b>Mã Vận Đơn:</b></html>"), gbc);
        gbc.gridx = 1; resultPanel.add(new JLabel(tracking), gbc);

        gbc.gridx = 0; gbc.gridy = 7; resultPanel.add(new JLabel("<html><b>Hãng Vận Chuyển:</b></html>"), gbc);
        gbc.gridx = 1; resultPanel.add(new JLabel(carrier), gbc);

        gbc.gridx = 0; gbc.gridy = 8; resultPanel.add(new JLabel("<html><b>Tài Xế Phụ Trách:</b></html>"), gbc);
        gbc.gridx = 1; resultPanel.add(new JLabel(shipper), gbc);

        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 2;
        resultPanel.add(new JSeparator(), gbc);

        JButton btnDone = utils.UIStyleUtils.createStyledButton("Tiếp tục mua sắm", new Color(238, 77, 45), Color.WHITE);
        btnDone.setPreferredSize(new Dimension(200, 45));
        btnDone.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnDone.addActionListener(e -> {
            storefrontPanel.loadProducts(null, null);
            cardLayout.show(mainContentPanel, "STOREFRONT");
        });

        gbc.gridy = 10; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        resultPanel.add(btnDone, gbc);

        wrapper.add(resultPanel);
        mainContentPanel.add(wrapper, "SUCCESS");
        cardLayout.show(mainContentPanel, "SUCCESS");
    }
}
