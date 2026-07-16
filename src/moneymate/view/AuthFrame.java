package moneymate.view;

import com.formdev.flatlaf.FlatClientProperties;
import moneymate.exception.DatabaseException;
import moneymate.exception.ValidationException;
import moneymate.model.User;
import moneymate.repository.UserRepository;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện hợp nhất Đăng Nhập & Đăng Ký (AuthFrame) thiết kế cao cấp chuẩn SaaS.
 * Đăng nhập mặc định bằng Số điện thoại (loại bỏ hoàn toàn trường Username).
 */
public class AuthFrame extends JFrame {
    private final UserRepository userRepository;
    private final CardLayout cardLayout;
    private final JPanel pnlMain;

    // Components Đăng nhập
    private JTextField txtLoginPhone;
    private JPasswordField txtLoginPass;

    // Components Đăng ký
    private JTextField txtRegPhone;
    private JTextField txtRegFullName;
    private JTextField txtRegEmail;
    private JPasswordField txtRegPass;
    private JPasswordField txtRegConfirmPass;

    public AuthFrame() {
        this.userRepository = new UserRepository();
        this.cardLayout = new CardLayout();
        this.pnlMain = new JPanel(cardLayout);

        setTitle("MoneyMate - Đăng nhập tài khoản MB Bank Style");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
        add(pnlMain);
    }

    private void initComponents() {
        pnlMain.add(createLoginPanel(), "Login");
        pnlMain.add(createRegisterPanel(), "Register");
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 45, 40, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;

        int row = 0;

        // Tên Logo
        JLabel lblLogo = new JLabel("MoneyMate", JLabel.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblLogo.setForeground(new Color(79, 70, 229)); // Indigo 600
        gbc.gridy = row++;
        panel.add(lblLogo, gbc);

        // Subtitle
        JLabel lblSub = new JLabel("Đăng nhập tài khoản cá nhân", JLabel.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(100, 116, 139)); // Slate 500
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 30, 0);
        panel.add(lblSub, gbc);

        gbc.insets = new Insets(8, 0, 4, 0);

        // Label Số điện thoại làm tên đăng nhập
        JLabel lblPhone = new JLabel("Số điện thoại đăng nhập:");
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = row++;
        panel.add(lblPhone, gbc);

        // TextField Số điện thoại
        txtLoginPhone = new JTextField();
        txtLoginPhone.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập số điện thoại của bạn");
        txtLoginPhone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 12, 0);
        panel.add(txtLoginPhone, gbc);

        // Label Password
        gbc.insets = new Insets(8, 0, 4, 0);
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = row++;
        panel.add(lblPass, gbc);

        // PasswordField
        txtLoginPass = new JPasswordField();
        txtLoginPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu");
        txtLoginPass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        txtLoginPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(txtLoginPass, gbc);

        // Nút Đăng nhập
        JButton btnLogin = new JButton("Đăng Nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.putClientProperty(FlatClientProperties.STYLE, "background: #4F46E5; foreground: #ffffff; arc: 8");
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(0, 40));
        btnLogin.addActionListener(e -> performLogin());
        gbc.gridy = row++;
        gbc.insets = new Insets(25, 0, 10, 0);
        panel.add(btnLogin, gbc);

        // Nút chuyển Đăng ký
        JButton btnGoRegister = new JButton("Chưa có tài khoản? Đăng ký ngay");
        btnGoRegister.setContentAreaFilled(false);
        btnGoRegister.setBorderPainted(false);
        btnGoRegister.setFocusPainted(false);
        btnGoRegister.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGoRegister.setForeground(new Color(79, 70, 229));
        btnGoRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGoRegister.addActionListener(e -> cardLayout.show(pnlMain, "Register"));
        gbc.gridy = row++;
        gbc.insets = new Insets(5, 0, 5, 0);
        panel.add(btnGoRegister, gbc);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 45, 25, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;

        int row = 0;

        // Tiêu đề
        JLabel lblTitle = new JLabel("Đăng Ký Tài Khoản", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(16, 185, 129)); // Emerald 500
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(lblTitle, gbc);

        gbc.insets = new Insets(4, 0, 2, 0);

        // Số điện thoại (dùng làm tên đăng nhập)
        JLabel lblRegPhone = new JLabel("Số điện thoại (SĐT đăng nhập):");
        lblRegPhone.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridy = row++;
        panel.add(lblRegPhone, gbc);

        txtRegPhone = new JTextField();
        txtRegPhone.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập số điện thoại (10 chữ số)");
        gbc.gridy = row++;
        panel.add(txtRegPhone, gbc);

        // Họ tên
        JLabel lblRegName = new JLabel("Họ và tên:");
        lblRegName.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridy = row++;
        panel.add(lblRegName, gbc);

        txtRegFullName = new JTextField();
        txtRegFullName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập họ và tên đầy đủ");
        gbc.gridy = row++;
        panel.add(txtRegFullName, gbc);

        // Email
        JLabel lblRegEmail = new JLabel("Email:");
        lblRegEmail.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridy = row++;
        panel.add(lblRegEmail, gbc);

        txtRegEmail = new JTextField();
        txtRegEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ví dụ: hotro@moneymate.vn");
        gbc.gridy = row++;
        panel.add(txtRegEmail, gbc);

        // Mật khẩu
        JLabel lblRegPass = new JLabel("Mật khẩu:");
        lblRegPass.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridy = row++;
        panel.add(lblRegPass, gbc);

        txtRegPass = new JPasswordField();
        txtRegPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu mới");
        txtRegPass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        gbc.gridy = row++;
        panel.add(txtRegPass, gbc);

        // Xác nhận mật khẩu
        JLabel lblRegConfirm = new JLabel("Xác nhận mật khẩu:");
        lblRegConfirm.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridy = row++;
        panel.add(lblRegConfirm, gbc);

        txtRegConfirmPass = new JPasswordField();
        txtRegConfirmPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập lại mật khẩu");
        txtRegConfirmPass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        gbc.gridy = row++;
        panel.add(txtRegConfirmPass, gbc);

        // Nút đăng ký
        JButton btnRegister = new JButton("Đăng Ký");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.putClientProperty(FlatClientProperties.STYLE, "background: #10B981; foreground: #ffffff; arc: 8");
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setPreferredSize(new Dimension(0, 38));
        btnRegister.addActionListener(e -> performRegister());
        gbc.gridy = row++;
        gbc.insets = new Insets(20, 0, 5, 0);
        panel.add(btnRegister, gbc);

        // Nút chuyển Đăng nhập
        JButton btnGoLogin = new JButton("Đã có tài khoản? Đăng nhập tại đây");
        btnGoLogin.setContentAreaFilled(false);
        btnGoLogin.setBorderPainted(false);
        btnGoLogin.setFocusPainted(false);
        btnGoLogin.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnGoLogin.setForeground(new Color(16, 185, 129));
        btnGoLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGoLogin.addActionListener(e -> cardLayout.show(pnlMain, "Login"));
        gbc.gridy = row++;
        gbc.insets = new Insets(5, 0, 5, 0);
        panel.add(btnGoLogin, gbc);

        return panel;
    }

    private void performLogin() {
        String phone = txtLoginPhone.getText().trim();
        String password = new String(txtLoginPass.getPassword());

        if (phone.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ số điện thoại và mật khẩu.", "Lỗi xác thực", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userRepository.login(phone, password);
            if (user != null) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    MainFrame mainFrame = new MainFrame(user);
                    mainFrame.setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this, "Số điện thoại hoặc mật khẩu không chính xác.", "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performRegister() {
        String phone = txtRegPhone.getText().trim();
        String fullName = txtRegFullName.getText().trim();
        String email = txtRegEmail.getText().trim();
        String password = new String(txtRegPass.getPassword());
        String confirm = new String(txtRegConfirmPass.getPassword());

        if (phone.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tất cả các trường.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!phone.matches("^\\d{10,11}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải chứa 10-11 chữ số.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
            JOptionPane.showMessageDialog(this, "Email đăng ký phải có đuôi @gmail.com (ví dụ: hotro@gmail.com).", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Quyết định số tài khoản (STK) mặc định là số điện thoại
        String accountNumber = phone;

        try {
            if (userRepository.phoneExists(phone)) {
                JOptionPane.showMessageDialog(this, "Số điện thoại này đã được đăng ký tài khoản khác.", "Lỗi đăng ký", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Sinh mã OTP kích hoạt 6 chữ số
            String otp = String.format("%06d", new java.util.Random().nextInt(1000000));

            // Hiển thị hòm thư Gmail giả lập
            showSimulatedEmailPopup(email, otp);

            // Yêu cầu người dùng nhập OTP kích hoạt
            String userInputOtp = JOptionPane.showInputDialog(this,
                    "Mã OTP kích hoạt đã gửi về hòm thư: " + email + "\nVui lòng nhập mã gồm 6 số để xác thực tài khoản:",
                    "Xác thực Email kích hoạt tài khoản",
                    JOptionPane.QUESTION_MESSAGE);

            if (userInputOtp == null) {
                JOptionPane.showMessageDialog(this, "Quá trình xác thực bị hủy. Đăng ký tài khoản thất bại.", "Hủy đăng ký", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!userInputOtp.trim().equals(otp)) {
                JOptionPane.showMessageDialog(this, "Mã OTP xác thực không chính xác! Đăng ký tài khoản thất bại.", "Lỗi xác thực", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Tạo đối tượng User mới
            User newUser = new User(
                    0,
                    User.hashPassword(password),
                    fullName,
                    email,
                    phone,
                    accountNumber,
                    20000000.0, // Hạn mức thu nhập mặc định
                    10000000.0  // Hạn mức chi tiêu mặc định
            );

            userRepository.register(newUser);
            JOptionPane.showMessageDialog(this, "Xác thực email thành công!\nĐăng ký tài khoản MoneyMate thành công!", "Đăng ký thành công", JOptionPane.INFORMATION_MESSAGE);

            // Xoá form đăng ký
            txtRegPhone.setText("");
            txtRegFullName.setText("");
            txtRegEmail.setText("");
            txtRegPass.setText("");
            txtRegConfirmPass.setText("");

            // Quay về login
            cardLayout.show(pnlMain, "Login");

        } catch (DatabaseException | ValidationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSimulatedEmailPopup(String emailAddress, String otpCode) {
        JDialog mailDialog = new JDialog(this, "Hộp thư Gmail giả lập - Nhận thư mới", true);
        mailDialog.setSize(550, 365);
        mailDialog.setLocationRelativeTo(this);
        mailDialog.setResizable(false);

        JPanel pnlMail = new JPanel(new BorderLayout(10, 10));
        pnlMail.setBackground(Color.WHITE);
        pnlMail.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Thanh tiêu đề email giả lập
        JPanel pnlHeader = new JPanel(new GridBagLayout());
        pnlHeader.setBackground(new Color(241, 245, 249)); // Slate 100
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel lblFrom = new JLabel("<html><b>Từ:</b> MoneyMate Security &lt;security@moneymate.vn&gt;</html>");
        lblFrom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 0;
        pnlHeader.add(lblFrom, gbc);

        JLabel lblTo = new JLabel("<html><b>Đến:</b> " + emailAddress + "</html>");
        lblTo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 1;
        pnlHeader.add(lblTo, gbc);

        JLabel lblSubject = new JLabel("<html><b>Tiêu đề:</b> [MoneyMate] Mã OTP xác nhận kích hoạt tài khoản mới</html>");
        lblSubject.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSubject.setForeground(new Color(30, 41, 59));
        gbc.gridy = 2;
        pnlHeader.add(lblSubject, gbc);

        pnlMail.add(pnlHeader, BorderLayout.NORTH);

        // Nội dung Email
        JEditorPane txtBody = new JEditorPane();
        txtBody.setContentType("text/html");
        txtBody.setEditable(false);
        txtBody.setText("<html><body style='font-family: Segoe UI, sans-serif; font-size: 13px; color: #1E293B; line-height: 1.5;'>" +
                "Xin chào,<br><br>" +
                "Hệ thống bảo mật MoneyMate đã nhận được yêu cầu đăng ký tài khoản của bạn.<br>" +
                "Mã OTP xác thực kích hoạt tài khoản của bạn là:<br><br>" +
                "<div style='font-size: 24px; font-weight: bold; color: #4F46E5; background-color: #EEF2F6; padding: 12px 25px; border-radius: 6px; display: inline-block; letter-spacing: 2px;'>" + otpCode + "</div><br><br>" +
                "Vui lòng sao chép mã này và nhập vào ô xác thực trên ứng dụng để hoàn tất đăng ký tài khoản.<br>" +
                "Mã xác thực có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.<br><br>" +
                "Trân trọng,<br>" +
                "<b>Ban quản trị MoneyMate</b>" +
                "</body></html>");
        txtBody.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
        pnlMail.add(new JScrollPane(txtBody), BorderLayout.CENTER);

        // Nút bấm
        JButton btnClose = new JButton("Đã xem - Nhập mã xác thực");
        btnClose.putClientProperty(FlatClientProperties.STYLE, "background: #4F46E5; foreground: #ffffff; arc: 8");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(0, 36));
        btnClose.addActionListener(e -> mailDialog.dispose());
        pnlMail.add(btnClose, BorderLayout.SOUTH);

        mailDialog.add(pnlMail);
        mailDialog.setVisible(true);
    }
}
