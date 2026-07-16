package moneymate.view.component;

import com.formdev.flatlaf.FlatClientProperties;
import moneymate.exception.DatabaseException;
import moneymate.exception.ValidationException;
import moneymate.factory.TransactionFactory;
import moneymate.model.Category;
import moneymate.model.User;
import moneymate.model.Transaction;
import moneymate.repository.UserRepository;
import moneymate.service.TransactionService;
import moneymate.util.CurrencyUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Giao diện chuyển khoản liên tài khoản (TransferPanel) thời gian thực.
 * Tích hợp tìm kiếm tài khoản thụ hưởng và xuất biên lai giao dịch thành công kiểu MB Bank.
 */
public class TransferPanel extends JPanel {
    private final JFrame parentFrame;
    private final User currentUser;
    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final Runnable onTransferSuccess;

    private JTextField txtRecipientSTK;
    private JLabel lblRecipientNameVal;
    private JTextField txtAmount;
    private JTextField txtMessage;
    private JButton btnCheck;
    private JButton btnTransfer;
    private JLabel lblSenderBalance;

    private User recipientUser = null;
    private double currentSenderBalance = 0.0;

    public TransferPanel(JFrame parentFrame, User currentUser, TransactionService transactionService, Runnable onTransferSuccess) {
        this.parentFrame = parentFrame;
        this.currentUser = currentUser;
        this.transactionService = transactionService;
        this.userRepository = new UserRepository();
        this.onTransferSuccess = onTransferSuccess;

        setBackground(new Color(248, 250, 252)); // Slate 50
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        int row = 0;

        // Tiêu đề
        JLabel lblTitle = new JLabel("CHUYỂN KHOẢN TRONG HỆ THỐNG MONNEYMATE", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(79, 70, 229)); // Indigo 600
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 25, 0);
        add(lblTitle, gbc);

        gbc.insets = new Insets(8, 0, 8, 0);

        // --- Panel thông tin người gửi ---
        JPanel pnlSender = new JPanel(new GridBagLayout());
        pnlSender.setBackground(new Color(30, 41, 59)); // Slate 800 sang trọng
        pnlSender.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        pnlSender.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        GridBagConstraints gbcSender = new GridBagConstraints();
        gbcSender.fill = GridBagConstraints.HORIZONTAL;
        gbcSender.gridx = 0;

        JLabel lblSendCard = new JLabel("TÀI KHOẢN NGUỒN (SENDER)");
        lblSendCard.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSendCard.setForeground(new Color(148, 163, 184)); // Slate 400
        gbcSender.gridy = 0;
        pnlSender.add(lblSendCard, gbcSender);

        JLabel lblSenderAcc = new JLabel(currentUser.getFullName() + " | STK: " + currentUser.getAccountNumber());
        lblSenderAcc.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSenderAcc.setForeground(Color.WHITE);
        gbcSender.gridy = 1;
        gbcSender.insets = new Insets(5, 0, 5, 0);
        pnlSender.add(lblSenderAcc, gbcSender);

        lblSenderBalance = new JLabel("Số dư khả dụng: 0 ₫");
        lblSenderBalance.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSenderBalance.setForeground(new Color(16, 185, 129)); // Emerald 500
        gbcSender.gridy = 2;
        pnlSender.add(lblSenderBalance, gbcSender);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 20, 0);
        add(pnlSender, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);

        // --- Form nhập liệu chuyển khoản ---
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.fill = GridBagConstraints.HORIZONTAL;
        gbcForm.insets = new Insets(8, 8, 8, 8);
        int formRow = 0;

        // Số tài khoản nhận
        gbcForm.gridx = 0; gbcForm.gridy = formRow; gbcForm.weightx = 0.3;
        pnlForm.add(new JLabel("Số tài khoản nhận (STK):"), gbcForm);

        JPanel pnlSTKRow = new JPanel(new BorderLayout(8, 0));
        pnlSTKRow.setOpaque(false);
        txtRecipientSTK = new JTextField();
        txtRecipientSTK.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập số tài khoản nhận");
        pnlSTKRow.add(txtRecipientSTK, BorderLayout.CENTER);

        btnCheck = new JButton("Kiểm tra");
        btnCheck.putClientProperty(FlatClientProperties.STYLE, "background: #334155; foreground: #ffffff; arc: 6");
        btnCheck.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCheck.addActionListener(e -> checkRecipient());
        pnlSTKRow.add(btnCheck, BorderLayout.EAST);

        gbcForm.gridx = 1; gbcForm.weightx = 0.7;
        pnlForm.add(pnlSTKRow, gbcForm);
        formRow++;

        // Tên chủ tài khoản nhận (Hiển thị tự động sau khi kiểm tra)
        gbcForm.gridx = 0; gbcForm.gridy = formRow; gbcForm.weightx = 0.3;
        pnlForm.add(new JLabel("Người hưởng thụ:"), gbcForm);

        lblRecipientNameVal = new JLabel("Chưa xác thực tài khoản");
        lblRecipientNameVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRecipientNameVal.setForeground(new Color(148, 163, 184)); // Slate 400
        gbcForm.gridx = 1; gbcForm.weightx = 0.7;
        pnlForm.add(lblRecipientNameVal, gbcForm);
        formRow++;

        // Số tiền chuyển
        gbcForm.gridx = 0; gbcForm.gridy = formRow; gbcForm.weightx = 0.3;
        pnlForm.add(new JLabel("Số tiền chuyển (VND):"), gbcForm);

        txtAmount = new JTextField();
        txtAmount.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ví dụ: 100000");
        gbcForm.gridx = 1; gbcForm.weightx = 0.7;
        pnlForm.add(txtAmount, gbcForm);
        formRow++;

        // Lời nhắn chuyển tiền
        gbcForm.gridx = 0; gbcForm.gridy = formRow; gbcForm.weightx = 0.3;
        pnlForm.add(new JLabel("Nội dung chuyển khoản:"), gbcForm);

        txtMessage = new JTextField();
        txtMessage.setText(currentUser.getFullName().toUpperCase() + " chuyen khoan");
        gbcForm.gridx = 1; gbcForm.weightx = 0.7;
        pnlForm.add(txtMessage, gbcForm);
        formRow++;

        // Nút Xác nhận chuyển tiền
        btnTransfer = new JButton("Xác nhận chuyển khoản");
        btnTransfer.putClientProperty(FlatClientProperties.STYLE, "background: #4F46E5; foreground: #ffffff; arc: 8");
        btnTransfer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTransfer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTransfer.setPreferredSize(new Dimension(0, 42));
        btnTransfer.addActionListener(e -> performTransfer());

        gbcForm.gridx = 0; gbcForm.gridy = formRow; gbcForm.gridwidth = 2;
        gbcForm.insets = new Insets(15, 8, 5, 8);
        pnlForm.add(btnTransfer, gbcForm);

        gbc.gridy = row++;
        add(pnlForm, gbc);
    }

    /**
     * Đồng bộ số dư của người gửi khi nạp lại Dashboard.
     */
    public void updateSenderBalance(double balance) {
        this.currentSenderBalance = balance;
        lblSenderBalance.setText("Số dư khả dụng: " + CurrencyUtil.formatVND(balance));
    }

    private void checkRecipient() {
        String stk = txtRecipientSTK.getText().trim();
        if (stk.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số tài khoản nhận.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (stk.equals(currentUser.getAccountNumber())) {
            JOptionPane.showMessageDialog(this, "Không thể tự chuyển tiền cho chính số tài khoản của bạn.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            recipientUser = userRepository.getUserByAccountNumber(stk);
            if (recipientUser != null) {
                lblRecipientNameVal.setText(recipientUser.getFullName().toUpperCase());
                lblRecipientNameVal.setForeground(new Color(16, 185, 129)); // Xanh Emerald
            } else {
                recipientUser = null;
                lblRecipientNameVal.setText("Tài khoản thụ hưởng không tồn tại!");
                lblRecipientNameVal.setForeground(new Color(239, 68, 68)); // Đỏ
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performTransfer() {
        if (recipientUser == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập và kiểm tra tài khoản thụ hưởng trước.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String amountStr = txtAmount.getText().trim();
        String message = txtMessage.getText().trim();

        if (amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền chuyển.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền chuyển phải là số dương lớn hơn 0.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (amount > currentSenderBalance) {
            JOptionPane.showMessageDialog(this, "Số dư tài khoản không đủ để thực hiện giao dịch chuyển khoản này.", "Giao dịch thất bại", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (message.isEmpty()) {
            message = currentUser.getFullName() + " chuyen tien";
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận chuyển khoản:\n" +
                "- Đến tài khoản: " + recipientUser.getFullName().toUpperCase() + " (" + recipientUser.getAccountNumber() + ")\n" +
                "- Số tiền: " + CurrencyUtil.formatVND(amount) + "\n" +
                "- Nội dung: " + message,
                "Xác thực chuyển tiền", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Tạo giao dịch chi tiêu cho Người gửi (ID category = 8 'Chuyển khoản')
                Category catTransferExpense = new Category(8, "Chuyển khoản", "EXPENSE", 0);
                Transaction transSend = TransactionFactory.createTransaction(
                        "EXPENSE",
                        0,
                        "Chuyển tiền đến " + recipientUser.getFullName(),
                        amount,
                        LocalDate.now().toString(),
                        catTransferExpense,
                        message,
                        "STK Nhận: " + recipientUser.getAccountNumber()
                );
                transactionService.addTransaction(transSend, currentUser.getId());

                // Tạo giao dịch thu nhập cho Người nhận (ID category = 9 'Nhận chuyển khoản')
                Category catTransferIncome = new Category(9, "Nhận chuyển khoản", "INCOME", 0);
                Transaction transReceive = TransactionFactory.createTransaction(
                        "INCOME",
                        0,
                        "Nhận tiền từ " + currentUser.getFullName(),
                        amount,
                        LocalDate.now().toString(),
                        catTransferIncome,
                        message,
                        "STK Gửi: " + currentUser.getAccountNumber()
                );
                transactionService.addTransaction(transReceive, recipientUser.getId());

                // Reset form
                txtRecipientSTK.setText("");
                txtAmount.setText("");
                txtMessage.setText(currentUser.getFullName().toUpperCase() + " chuyen khoan");
                lblRecipientNameVal.setText("Chưa xác thực tài khoản");
                lblRecipientNameVal.setForeground(new Color(148, 163, 184));
                User tempRecipient = recipientUser;
                recipientUser = null;

                // Đồng bộ lại Dashboard dữ liệu ngay lập tức
                onTransferSuccess.run();

                // Hiển thị Biên lai giao dịch thành công (MB Bank style)
                showReceiptDialog(tempRecipient, amount, message);

            } catch (DatabaseException | ValidationException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showReceiptDialog(User recipient, double amount, String message) {
        JDialog dialog = new JDialog(parentFrame, "Biên lai giao dịch điện tử", true);
        dialog.setSize(400, 520);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;

        int row = 0;

        // Icon tròn dấu kiểm xanh lá
        JLabel lblCheck = new JLabel("✓", JLabel.CENTER);
        lblCheck.setFont(new Font("Segoe UI", Font.BOLD, 52));
        lblCheck.setForeground(new Color(16, 185, 129)); // Emerald 500
        gbc.gridy = row++;
        content.add(lblCheck, gbc);

        JLabel lblStatus = new JLabel("GIAO DỊCH THÀNH CÔNG", JLabel.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblStatus.setForeground(new Color(16, 185, 129));
        gbc.gridy = row++;
        content.add(lblStatus, gbc);

        JLabel lblReceiptAmt = new JLabel(CurrencyUtil.formatVND(amount), JLabel.CENTER);
        lblReceiptAmt.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblReceiptAmt.setForeground(new Color(30, 41, 59));
        gbc.gridy = row++;
        gbc.insets = new Insets(5, 0, 15, 0);
        content.add(lblReceiptAmt, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);
        JSeparator sep = new JSeparator();
        gbc.gridy = row++;
        content.add(sep, gbc);

        // Chi tiết
        content.add(createReceiptDetailRow("Tài khoản nguồn gửi:", currentUser.getFullName().toUpperCase() + " (" + currentUser.getAccountNumber() + ")"), gbc);
        gbc.gridy = row++;
        content.add(createReceiptDetailRow("Tài khoản hưởng thụ:", recipient.getFullName().toUpperCase() + " (" + recipient.getAccountNumber() + ")"), gbc);
        gbc.gridy = row++;
        
        // Tạo mã giao dịch random FT
        Random rand = new Random();
        String ftCode = "FT26197" + (100000 + rand.nextInt(900000));
        content.add(createReceiptDetailRow("Mã giao dịch (Ref):", ftCode), gbc);
        gbc.gridy = row++;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        content.add(createReceiptDetailRow("Thời gian giao dịch:", LocalDate.now().toString() + " " + java.time.LocalTime.now().toString().substring(0, 8)), gbc);
        gbc.gridy = row++;

        content.add(createReceiptDetailRow("Nội dung chuyển:", message), gbc);
        gbc.gridy = row++;

        content.add(createReceiptDetailRow("Phí giao dịch:", "0 ₫ (Miễn phí chuyển khoản)"), gbc);
        gbc.gridy = row++;

        JSeparator sep2 = new JSeparator();
        content.add(sep2, gbc);
        gbc.gridy = row++;

        JButton btnClose = new JButton("Hoàn Tất");
        btnClose.putClientProperty(FlatClientProperties.STYLE, "background: #10B981; foreground: #ffffff; arc: 8");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(0, 38));
        btnClose.addActionListener(e -> dialog.dispose());
        
        gbc.insets = new Insets(15, 0, 5, 0);
        content.add(btnClose, gbc);
        gbc.gridy = row++;

        dialog.add(content);
        dialog.setVisible(true);
    }

    private JPanel createReceiptDetailRow(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lblL = new JLabel(label);
        lblL.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblL.setForeground(new Color(100, 116, 139)); // Slate 500
        
        JLabel lblR = new JLabel(value);
        lblR.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblR.setForeground(new Color(30, 41, 59)); // Slate 800

        panel.add(lblL, BorderLayout.WEST);
        panel.add(lblR, BorderLayout.EAST);
        return panel;
    }
}
