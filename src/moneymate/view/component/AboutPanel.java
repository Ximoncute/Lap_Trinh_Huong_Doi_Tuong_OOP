package moneymate.view.component;

import com.formdev.flatlaf.FlatClientProperties;
import moneymate.model.User;
import moneymate.util.CurrencyUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Giao diện giới thiệu phần mềm và Trợ lý gợi ý tài chính thông minh (AboutPanel).
 * Thiết kế giao diện sang trọng, cao cấp theo phong cách Modern SaaS.
 */
public class AboutPanel extends JPanel {
    private final User currentUser;
    private final Runnable refreshCallback;
    private JLabel lblAdviceTitle;
    private JTextArea txtAdviceBody;
    private JPanel pnlAdviceCard;

    private static final String[] GENERAL_ADVICES = {
            "Quy tắc 50/30/20: Hãy phân bổ 50% thu nhập cho nhu cầu thiết yếu, 30% cho sở thích cá nhân và 20% tích lũy đầu tư dài hạn.",
            "Tối ưu chi tiêu ăn uống: Thống kê cho thấy việc tự chuẩn bị bữa trưa có thể tiết kiệm tới 1.500.000 ₫ mỗi tháng.",
            "Hãy đặt hạn mức chi tiêu thấp hơn 10% so với tháng trước để rèn luyện thói quen chi tiêu thông thái.",
            "Quy tắc 24h: Trước khi mua một món đồ không thiết yếu, hãy đợi 24h. 80% trường hợp bạn sẽ nhận ra mình không thực sự cần nó.",
            "Đầu tư vào bản thân: Hãy trích 5% thu nhập hàng tháng để mua sách, học tập kỹ năng mới để gia tăng thu nhập chủ động."
    };

    public AboutPanel(User currentUser, Runnable refreshCallback) {
        this.currentUser = currentUser;
        this.refreshCallback = refreshCallback;

        setBackground(new Color(248, 250, 252)); // Slate 50
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        initComponents();
    }

    private void initComponents() {
        // --- 1. HEADER BANNER (Gradient Style) ---
        JPanel pnlHeader = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(79, 70, 229), getWidth(), getHeight(), new Color(99, 102, 241));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        pnlHeader.setPreferredSize(new Dimension(0, 140));
        pnlHeader.setLayout(new BorderLayout());
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel pnlHeaderText = new JPanel(new GridLayout(2, 1, 5, 0));
        pnlHeaderText.setOpaque(false);

        JLabel lblTitle = new JLabel("MoneyMate Premium");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);
        pnlHeaderText.add(lblTitle);

        JLabel lblSubtitle = new JLabel("Hệ thống quản lý tài chính cá nhân & Trợ lý cố vấn tài chính thông minh");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(224, 231, 255));
        pnlHeaderText.add(lblSubtitle);

        pnlHeader.add(pnlHeaderText, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        // --- 2. CENTER CONTENT (Split into 2 columns: Features vs AI Assistant) ---
        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weighty = 1.0;

        // Cột 1: Giới thiệu tính năng
        JPanel pnlFeatures = new JPanel(new GridBagLayout());
        pnlFeatures.setBackground(Color.WHITE);
        pnlFeatures.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbcF = new GridBagConstraints();
        gbcF.fill = GridBagConstraints.HORIZONTAL;
        gbcF.insets = new Insets(8, 0, 8, 0);
        gbcF.weightx = 1.0;
        gbcF.gridx = 0;

        int rowF = 0;

        JLabel lblFeatTitle = new JLabel("TÍNH NĂNG NỔI BẬT");
        lblFeatTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFeatTitle.setForeground(new Color(30, 41, 59));
        gbcF.gridy = rowF++;
        gbcF.insets = new Insets(0, 0, 15, 0);
        pnlFeatures.add(lblFeatTitle, gbcF);

        gbcF.insets = new Insets(6, 0, 6, 0);
        
        gbcF.gridy = rowF++;
        pnlFeatures.add(createFeatureItem("» Thống kê & Biểu đồ trực quan", "Xem phân tích thu chi theo các mốc ngày/tháng/năm trực quan, khoa học."), gbcF);
        
        gbcF.gridy = rowF++;
        pnlFeatures.add(createFeatureItem("» Thẻ thanh toán số sang trọng", "Tích hợp thẻ ảo định danh Platinum hiển thị số tài khoản và thông tin thời gian thực."), gbcF);
        
        gbcF.gridy = rowF++;
        pnlFeatures.add(createFeatureItem("» Chuyển khoản nội bộ tức thì", "Chuyển tiền nhanh chóng giữa các tài khoản người dùng, tự động tra cứu người hưởng thụ."), gbcF);
        
        gbcF.gridy = rowF++;
        pnlFeatures.add(createFeatureItem("» Cảnh báo hạn mức thông minh", "Tự động phát hiện và nhắc nhở khi chi tiêu tháng vượt ngưỡng ngân sách cho phép."), gbcF);

        gbc.gridx = 0; gbc.weightx = 0.5;
        pnlCenter.add(pnlFeatures, gbc);

        // Cột 2: Trợ lý cố vấn tài chính AI
        JPanel pnlAiAssistant = new JPanel(new BorderLayout(15, 15));
        pnlAiAssistant.setBackground(Color.WHITE);
        pnlAiAssistant.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JPanel pnlAiTitleRow = new JPanel(new GridLayout(2, 1, 3, 0));
        pnlAiTitleRow.setOpaque(false);
        JLabel lblAiTitle = new JLabel("TRỢ LÝ TÀI CHÍNH THÔNG MINH");
        lblAiTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAiTitle.setForeground(new Color(79, 70, 229)); // Indigo 600
        pnlAiTitleRow.add(lblAiTitle);

        JLabel lblAiSubtitle = new JLabel("Phân tích dữ liệu thực tế và đề xuất chiến lược tối ưu hóa");
        lblAiSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAiSubtitle.setForeground(new Color(100, 116, 139));
        pnlAiTitleRow.add(lblAiSubtitle);
        pnlAiAssistant.add(pnlAiTitleRow, BorderLayout.NORTH);

        // Thẻ Lời khuyên tài chính
        pnlAdviceCard = new JPanel(new BorderLayout(10, 10));
        pnlAdviceCard.setBackground(new Color(243, 244, 246)); // Gray 100
        pnlAdviceCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        pnlAdviceCard.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        lblAdviceTitle = new JLabel("Gợi ý từ Trợ lý:");
        lblAdviceTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAdviceTitle.setForeground(new Color(55, 65, 81));
        pnlAdviceCard.add(lblAdviceTitle, BorderLayout.NORTH);

        txtAdviceBody = new JTextArea("Hãy bấm nút bên dưới để Trợ lý tiến hành phân tích số liệu tài khoản và đưa ra cố vấn chuyên nghiệp.");
        txtAdviceBody.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtAdviceBody.setForeground(new Color(75, 85, 99));
        txtAdviceBody.setLineWrap(true);
        txtAdviceBody.setWrapStyleWord(true);
        txtAdviceBody.setEditable(false);
        txtAdviceBody.setOpaque(false);
        pnlAdviceCard.add(txtAdviceBody, BorderLayout.CENTER);

        pnlAiAssistant.add(pnlAdviceCard, BorderLayout.CENTER);

        // Nút Kích hoạt phân tích
        JButton btnAnalyze = new JButton("Nhận Cố Vấn Tài Chính");
        btnAnalyze.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAnalyze.putClientProperty(FlatClientProperties.STYLE, "background: #4F46E5; foreground: #ffffff; arc: 8");
        btnAnalyze.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAnalyze.setPreferredSize(new Dimension(0, 40));
        btnAnalyze.addActionListener(e -> generateAiAdvice());
        pnlAiAssistant.add(btnAnalyze, BorderLayout.SOUTH);

        gbc.gridx = 1; gbc.weightx = 0.5;
        pnlCenter.add(pnlAiAssistant, gbc);

        add(pnlCenter, BorderLayout.CENTER);

        // --- 3. FOOTER INFO ---
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setOpaque(false);
        JLabel lblCopy = new JLabel("© 2026 MoneyMate Project. Sản phẩm thuộc học phần Lập trình hướng đối tượng (OOP).", JLabel.CENTER);
        lblCopy.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCopy.setForeground(new Color(148, 163, 184));
        pnlFooter.add(lblCopy, BorderLayout.CENTER);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JPanel createFeatureItem(String title, String desc) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(15, 23, 42));
        gbc.gridy = 0;
        panel.add(lblTitle, gbc);

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(new Color(100, 116, 139));
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 0, 0, 0);
        panel.add(lblDesc, gbc);

        return panel;
    }

    private void generateAiAdvice() {
        // Đổi màu nền của thẻ gợi ý sang màu xanh lục dịu mắt khi phân tích thành công
        pnlAdviceCard.setBackground(new Color(236, 253, 245)); // Emerald 50
        lblAdviceTitle.setText("» Lời khuyên tối ưu ngân sách:");
        lblAdviceTitle.setForeground(new Color(5, 150, 105)); // Emerald 600

        double limit = currentUser.getExpenseLimit();
        double target = currentUser.getIncomeTarget();

        // Xây dựng gợi ý chuyên biệt dựa vào số liệu người dùng
        if (limit > 15000000) {
            txtAdviceBody.setText("Phân tích: Hạn mức chi tiêu tháng của bạn đang khá cao (" + CurrencyUtil.formatVND(limit) + ").\n\n" +
                    "Đề xuất: Nên hạ hạn mức chi tiêu xuống khoảng 20% và đầu tư phần tiền nhàn rỗi vào các quỹ mở hoặc tiết kiệm tích lũy để tối ưu dòng tiền.");
        } else if (target < 10000000) {
            txtAdviceBody.setText("Phân tích: Mục tiêu thu nhập của bạn hiện tại tương đối an toàn (" + CurrencyUtil.formatVND(target) + ").\n\n" +
                    "Đề xuất: Thử nâng chỉ số thu nhập mục tiêu của bản thân lên thêm 15% và tìm kiếm các cơ hội kinh doanh phụ hoặc đầu tư thụ động để bứt phá.");
        } else {
            // Lấy ngẫu nhiên một trong số các lời khuyên chung
            String advice = GENERAL_ADVICES[new Random().nextInt(GENERAL_ADVICES.length)];
            txtAdviceBody.setText(advice);
        }
    }
}
