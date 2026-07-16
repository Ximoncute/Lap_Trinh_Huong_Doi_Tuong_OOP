package moneymate.view.component;

import com.formdev.flatlaf.FlatClientProperties;
import moneymate.exception.DatabaseException;
import moneymate.exception.ValidationException;
import moneymate.model.User;
import moneymate.repository.UserRepository;
import moneymate.util.CurrencyUtil;
import moneymate.view.AuthFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện cá nhân dạng App Ngân Hàng (ProfilePanel) đẳng cấp.
 * Sử dụng Số điện thoại làm mã định danh đăng nhập và loại bỏ hoàn toàn username.
 */
public class ProfilePanel extends JPanel {
    private final JFrame parentFrame;
    private final UserRepository userRepository;
    private final Runnable onProfileUpdated;
    private User currentUser;

    private DebitCardPanel debitCard;
    private JProgressBar barIncome;
    private JProgressBar barExpense;
    private JLabel lblIncomePercent;
    private JLabel lblExpensePercent;

    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAccountNumber;
    private JTextField txtIncomeTarget;
    private JTextField txtExpenseLimit;

    private JPasswordField txtCurrentPass;
    private JPasswordField txtNewPass;

    public ProfilePanel(JFrame parentFrame, User currentUser, Runnable onProfileUpdated) {
        this.parentFrame = parentFrame;
        this.currentUser = currentUser;
        this.userRepository = new UserRepository();
        this.onProfileUpdated = onProfileUpdated;

        setBackground(new Color(248, 250, 252)); // Slate 50
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadUserData();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        // --- CỘT TRÁI (Thẻ ngân hàng ảo + Thanh tiến trình tài chính) ---
        JPanel pnlLeft = new JPanel(new GridBagLayout());
        pnlLeft.setOpaque(false);
        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.fill = GridBagConstraints.HORIZONTAL;
        gbcLeft.insets = new Insets(10, 5, 10, 5);
        gbcLeft.gridx = 0;

        // 1. Thẻ ngân hàng Platinum ảo
        debitCard = new DebitCardPanel();
        gbcLeft.gridy = 0;
        pnlLeft.add(debitCard, gbcLeft);

        // 2. Panel tiến độ tài chính
        JPanel pnlProgress = new JPanel(new GridBagLayout());
        pnlProgress.setBackground(Color.WHITE);
        pnlProgress.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbcProg = new GridBagConstraints();
        gbcProg.fill = GridBagConstraints.HORIZONTAL;
        gbcProg.insets = new Insets(5, 0, 5, 0);
        gbcProg.gridx = 0;

        JLabel lblProgTitle = new JLabel("Chỉ số tài chính tháng");
        lblProgTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblProgTitle.setForeground(new Color(15, 23, 42));
        gbcProg.gridy = 0;
        pnlProgress.add(lblProgTitle, gbcProg);

        // Mục tiêu thu nhập
        JLabel lblIncTarget = new JLabel("Mục tiêu thu nhập tháng:");
        lblIncTarget.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblIncTarget.setForeground(new Color(71, 85, 105));
        gbcProg.gridy = 1;
        gbcProg.insets = new Insets(10, 0, 2, 0);
        pnlProgress.add(lblIncTarget, gbcProg);

        barIncome = new JProgressBar(0, 100);
        barIncome.setForeground(new Color(16, 185, 129)); // Emerald 500
        gbcProg.gridy = 2;
        gbcProg.insets = new Insets(0, 0, 2, 0);
        pnlProgress.add(barIncome, gbcProg);

        lblIncomePercent = new JLabel("Đã đạt: 0%");
        lblIncomePercent.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblIncomePercent.setForeground(new Color(16, 185, 129));
        gbcProg.gridy = 3;
        pnlProgress.add(lblIncomePercent, gbcProg);

        // Hạn mức chi tiêu
        JLabel lblExpLimit = new JLabel("Hạn mức chi tiêu tháng:");
        lblExpLimit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblExpLimit.setForeground(new Color(71, 85, 105));
        gbcProg.gridy = 4;
        gbcProg.insets = new Insets(15, 0, 2, 0);
        pnlProgress.add(lblExpLimit, gbcProg);

        barExpense = new JProgressBar(0, 100);
        barExpense.setForeground(new Color(239, 68, 68)); // Red 500
        gbcProg.gridy = 5;
        gbcProg.insets = new Insets(0, 0, 2, 0);
        pnlProgress.add(barExpense, gbcProg);

        lblExpensePercent = new JLabel("Đã chi: 0%");
        lblExpensePercent.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblExpensePercent.setForeground(new Color(239, 68, 68));
        gbcProg.gridy = 6;
        pnlProgress.add(lblExpensePercent, gbcProg);

        gbcLeft.gridy = 1;
        gbcLeft.insets = new Insets(15, 5, 10, 5);
        pnlLeft.add(pnlProgress, gbcLeft);

        // Đưa Cột trái vào Grid chính
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        add(pnlLeft, gbc);

        // --- CỘT PHẢI (Thông tin tài khoản & Đổi mật khẩu) ---
        JPanel pnlRight = new JPanel(new GridBagLayout());
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.fill = GridBagConstraints.HORIZONTAL;
        gbcRight.insets = new Insets(8, 8, 8, 8);

        int rowRight = 0;

        JLabel lblFormTitle = new JLabel("QUẢN LÝ TÀI KHOẢN MB BANK STYLE");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFormTitle.setForeground(new Color(79, 70, 229)); // Indigo 600
        gbcRight.gridx = 0; gbcRight.gridy = rowRight++; gbcRight.gridwidth = 2;
        gbcRight.insets = new Insets(5, 8, 15, 8);
        pnlRight.add(lblFormTitle, gbcRight);

        gbcRight.gridwidth = 1;
        gbcRight.insets = new Insets(6, 8, 6, 8);

        // SĐT làm mã đăng nhập
        gbcRight.gridx = 0; gbcRight.gridy = rowRight; gbcRight.weightx = 0.3;
        pnlRight.add(new JLabel("Số điện thoại (Đăng nhập):"), gbcRight);
        txtPhone = new JTextField();
        txtPhone.setEditable(false);
        txtPhone.putClientProperty(FlatClientProperties.STYLE, "background: #F1F5F9; foreground: #64748B");
        gbcRight.gridx = 1; gbcRight.weightx = 0.7;
        pnlRight.add(txtPhone, gbcRight);
        rowRight++;

        // Số tài khoản ngân hàng
        gbcRight.gridx = 0; gbcRight.gridy = rowRight; gbcRight.weightx = 0.3;
        pnlRight.add(new JLabel("Số tài khoản (STK):"), gbcRight);
        txtAccountNumber = new JTextField();
        txtAccountNumber.setEditable(false);
        txtAccountNumber.putClientProperty(FlatClientProperties.STYLE, "background: #F1F5F9; foreground: #64748B");
        gbcRight.gridx = 1; gbcRight.weightx = 0.7;
        pnlRight.add(txtAccountNumber, gbcRight);
        rowRight++;

        // Họ tên
        gbcRight.gridx = 0; gbcRight.gridy = rowRight; gbcRight.weightx = 0.3;
        pnlRight.add(new JLabel("Họ và tên:"), gbcRight);
        txtFullName = new JTextField();
        gbcRight.gridx = 1; gbcRight.weightx = 0.7;
        pnlRight.add(txtFullName, gbcRight);
        rowRight++;

        // Email
        gbcRight.gridx = 0; gbcRight.gridy = rowRight; gbcRight.weightx = 0.3;
        pnlRight.add(new JLabel("Email liên hệ:"), gbcRight);
        txtEmail = new JTextField();
        gbcRight.gridx = 1; gbcRight.weightx = 0.7;
        pnlRight.add(txtEmail, gbcRight);
        rowRight++;

        // Mục tiêu thu nhập
        gbcRight.gridx = 0; gbcRight.gridy = rowRight; gbcRight.weightx = 0.3;
        pnlRight.add(new JLabel("Mục tiêu thu nhập:"), gbcRight);
        txtIncomeTarget = new JTextField();
        gbcRight.gridx = 1; gbcRight.weightx = 0.7;
        pnlRight.add(txtIncomeTarget, gbcRight);
        rowRight++;

        // Hạn mức chi tiêu
        gbcRight.gridx = 0; gbcRight.gridy = rowRight; gbcRight.weightx = 0.3;
        pnlRight.add(new JLabel("Hạn mức chi tiêu:"), gbcRight);
        txtExpenseLimit = new JTextField();
        gbcRight.gridx = 1; gbcRight.weightx = 0.7;
        pnlRight.add(txtExpenseLimit, gbcRight);
        rowRight++;

        // --- Đổi mật khẩu ---
        JSeparator sep = new JSeparator();
        gbcRight.gridx = 0; gbcRight.gridy = rowRight++; gbcRight.gridwidth = 2;
        gbcRight.insets = new Insets(15, 8, 10, 8);
        pnlRight.add(sep, gbcRight);

        gbcRight.gridwidth = 1;
        gbcRight.insets = new Insets(6, 8, 6, 8);

        JLabel lblPassTitle = new JLabel("Đổi mật khẩu (Bỏ trống nếu không đổi):");
        lblPassTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassTitle.setForeground(new Color(15, 23, 42));
        gbcRight.gridx = 0; gbcRight.gridy = rowRight++; gbcRight.gridwidth = 2;
        pnlRight.add(lblPassTitle, gbcRight);

        gbcRight.gridwidth = 1;

        // Mật khẩu hiện tại
        gbcRight.gridx = 0; gbcRight.gridy = rowRight;
        pnlRight.add(new JLabel("Mật khẩu cũ:"), gbcRight);
        txtCurrentPass = new JPasswordField();
        txtCurrentPass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        gbcRight.gridx = 1;
        pnlRight.add(txtCurrentPass, gbcRight);
        rowRight++;

        // Mật khẩu mới
        gbcRight.gridx = 0; gbcRight.gridy = rowRight;
        pnlRight.add(new JLabel("Mật khẩu mới:"), gbcRight);
        txtNewPass = new JPasswordField();
        txtNewPass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        gbcRight.gridx = 1;
        pnlRight.add(txtNewPass, gbcRight);
        rowRight++;

        // Nút Lưu và Đăng xuất xếp hàng ngang
        JPanel pnlActionRow = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlActionRow.setOpaque(false);

        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.putClientProperty(FlatClientProperties.STYLE, "background: #4F46E5; foreground: #ffffff; arc: 8");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveProfile());
        pnlActionRow.add(btnSave);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.putClientProperty(FlatClientProperties.STYLE, "background: #EF4444; foreground: #ffffff; arc: 8");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> performLogout());
        pnlActionRow.add(btnLogout);

        gbcRight.gridx = 0; gbcRight.gridy = rowRight; gbcRight.gridwidth = 2;
        gbcRight.insets = new Insets(20, 8, 5, 8);
        pnlRight.add(pnlActionRow, gbcRight);

        // Đưa Cột phải vào Grid chính
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.6;
        add(pnlRight, gbc);
    }

    private void loadUserData() {
        txtPhone.setText(currentUser.getPhone());
        txtAccountNumber.setText(currentUser.getAccountNumber());
        txtFullName.setText(currentUser.getFullName());
        txtEmail.setText(currentUser.getEmail());
        txtIncomeTarget.setText(String.format("%.0f", currentUser.getIncomeTarget()));
        txtExpenseLimit.setText(String.format("%.0f", currentUser.getExpenseLimit()));

        debitCard.updateCard(currentUser.getFullName(), "0 ₫", currentUser.getAccountNumber());
    }

    /**
     * Đồng bộ hóa số dư hiện tại và cập nhật lại thanh tiến trình ngoài màn hình cá nhân.
     */
    public void syncFinancialProgress(double netBalance, double totalIncome, double totalExpense) {
        debitCard.updateCard(currentUser.getFullName(), CurrencyUtil.formatVND(netBalance), currentUser.getAccountNumber());

        // 1. Tiến độ thu nhập
        double incTarget = currentUser.getIncomeTarget();
        if (incTarget > 0) {
            int incPercent = (int) Math.min((totalIncome / incTarget) * 100, 100);
            barIncome.setValue(incPercent);
            lblIncomePercent.setText(String.format("Đã đạt: %d%% (%s / %s)", 
                    incPercent, CurrencyUtil.formatVND(totalIncome), CurrencyUtil.formatVND(incTarget)));
        } else {
            barIncome.setValue(0);
            lblIncomePercent.setText("Hạn mức chưa thiết lập");
        }

        // 2. Tiến độ chi tiêu
        double expLimit = currentUser.getExpenseLimit();
        if (expLimit > 0) {
            int expPercent = (int) Math.min((totalExpense / expLimit) * 100, 100);
            barExpense.setValue(expPercent);
            lblExpensePercent.setText(String.format("Đã chi: %d%% (%s / %s)", 
                    expPercent, CurrencyUtil.formatVND(totalExpense), CurrencyUtil.formatVND(expLimit)));
            if (totalExpense > expLimit) {
                barExpense.setForeground(new Color(185, 28, 28)); // Đỏ thẫm
            } else {
                barExpense.setForeground(new Color(239, 68, 68)); // Đỏ thường
            }
        } else {
            barExpense.setValue(0);
            lblExpensePercent.setText("Hạn mức chưa thiết lập");
        }
    }

    private void saveProfile() {
        try {
            String name = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            double target = Double.parseDouble(txtIncomeTarget.getText().trim());
            double limit = Double.parseDouble(txtExpenseLimit.getText().trim());

            currentUser.setFullName(name);
            currentUser.setEmail(email);
            currentUser.setIncomeTarget(target);
            currentUser.setExpenseLimit(limit);

            // Đổi mật khẩu nếu có nhập
            String curPass = new String(txtCurrentPass.getPassword());
            String newPass = new String(txtNewPass.getPassword());
            if (!curPass.isEmpty() || !newPass.isEmpty()) {
                if (curPass.isEmpty() || newPass.isEmpty()) {
                    throw new ValidationException("Vui lòng nhập cả mật khẩu cũ và mới.");
                }
                String hashedCur = User.hashPassword(curPass);
                if (!hashedCur.equals(currentUser.getPassword())) {
                    throw new ValidationException("Mật khẩu cũ không chính xác.");
                }
                String hashedNew = User.hashPassword(newPass);
                userRepository.changePassword(currentUser.getId(), hashedNew);
                currentUser.setPassword(hashedNew);
                txtCurrentPass.setText("");
                txtNewPass.setText("");
            }

            userRepository.updateProfile(currentUser);
            JOptionPane.showMessageDialog(this, "Cập nhật hồ sơ tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            onProfileUpdated.run();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Hạn mức tài chính mục tiêu phải là số hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException | DatabaseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc muốn đăng xuất tài khoản hiện tại không?", 
                "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            parentFrame.dispose();
            SwingUtilities.invokeLater(() -> {
                AuthFrame auth = new AuthFrame();
                auth.setVisible(true);
            });
        }
    }

    // --- Panel con vẽ Thẻ ngân hàng ảo ---
    public static class DebitCardPanel extends JPanel {
        private String cardHolder = "";
        private String balanceStr = "0 ₫";
        private String accountNumber = "";

        public DebitCardPanel() {
            setPreferredSize(new Dimension(320, 185));
            setMinimumSize(new Dimension(320, 185));
            setOpaque(false);
        }

        public void updateCard(String holder, String balance, String accNum) {
            this.cardHolder = holder.toUpperCase();
            this.balanceStr = balance;
            this.accountNumber = accNum;
            repaint();
        }

        private String formatCardNum(String acc) {
            if (acc == null || acc.trim().isEmpty()) return "8888 8888";
            if (acc.length() <= 4) return "9704 " + acc;
            return acc.substring(0, 4) + " " + acc.substring(4);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Vẽ thẻ ngân hàng bo góc
            GradientPaint gp = new GradientPaint(0, 0, new Color(30, 41, 59), getWidth(), getHeight(), new Color(15, 23, 42)); // Slate 800 -> Slate 900
            g2d.setPaint(gp);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

            // Nét lượn sóng phản chiếu thẩm mỹ
            g2d.setColor(new Color(255, 255, 255, 8));
            g2d.fillOval(-50, -40, 180, 180);
            g2d.fillOval(getWidth() - 140, getHeight() - 110, 240, 240);

            // Nhãn thẻ
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2d.setColor(new Color(241, 245, 249, 180));
            g2d.drawString("MoneyMate Digital Card", 20, 28);

            // Chip vàng thẻ
            g2d.setColor(new Color(245, 158, 11)); // Amber 500
            g2d.fillRoundRect(20, 42, 38, 25, 4, 4);

            // Số thẻ ngân hàng ảo kiểu MB Bank
            g2d.setFont(new Font("Courier New", Font.BOLD, 14));
            g2d.setColor(new Color(226, 232, 240, 230));
            g2d.drawString("9704 2202 " + formatCardNum(accountNumber), 20, 85);

            // Số tài khoản ngân hàng
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2d.setColor(new Color(148, 163, 184)); // Slate 400
            g2d.drawString("STK: " + accountNumber, 20, 105);

            // Số dư
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2d.setColor(Color.WHITE);
            g2d.drawString(balanceStr, 20, 132);

            // Tên chủ thẻ
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2d.setColor(new Color(241, 245, 249));
            g2d.drawString(cardHolder, 20, 162);

            // Biểu tượng thẻ Mastercard cách điệu góc phải dưới
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.fillOval(getWidth() - 55, getHeight() - 38, 26, 26);
            g2d.fillOval(getWidth() - 41, getHeight() - 38, 26, 26);
        }
    }
}
