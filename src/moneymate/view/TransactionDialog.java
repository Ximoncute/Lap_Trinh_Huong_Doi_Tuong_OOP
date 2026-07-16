package moneymate.view;

import com.formdev.flatlaf.FlatClientProperties;
import moneymate.exception.DatabaseException;
import moneymate.exception.ValidationException;
import moneymate.factory.TransactionFactory;
import moneymate.model.Category;
import moneymate.model.Income;
import moneymate.model.Expense;
import moneymate.model.Transaction;
import moneymate.service.CategoryService;
import moneymate.service.TransactionService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionDialog extends JDialog {
    private final CategoryService categoryService;
    private final TransactionService transactionService;
    private final Transaction existingTransaction; // Nếu khác null là chế độ sửa
    private final int userId;
    private final Runnable onDataChanged;

    private JTextField txtTitle;
    private JTextField txtAmount;
    private JTextField txtDate;
    private JComboBox<String> cbType;
    private JComboBox<Category> cbCategory;
    private JTextField txtDescription;
    private JLabel lblExtra;
    private JTextField txtExtraInfo;
    private JButton btnSave;

    private List<Category> allCategories;

    public TransactionDialog(JFrame parent, CategoryService categoryService, 
                             TransactionService transactionService, Transaction transaction, 
                             int userId, Runnable onDataChanged) {
        super(parent, transaction == null ? "Thêm giao dịch mới" : "Sửa giao dịch", true);
        this.categoryService = categoryService;
        this.transactionService = transactionService;
        this.existingTransaction = transaction;
        this.userId = userId;
        this.onDataChanged = onDataChanged;

        setSize(450, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        try {
            allCategories = categoryService.getAllCategories();
        } catch (DatabaseException e) {
            allCategories = java.util.Collections.emptyList();
        }

        initComponents();
        if (existingTransaction != null) {
            fillFormForEdit();
        }
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tiêu đề
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("Tiêu đề:"), gbc);
        txtTitle = new JTextField();
        txtTitle.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ví dụ: Lương tháng 7, Mua đồ ăn");
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(txtTitle, gbc);

        // Số tiền
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("Số tiền (VND):"), gbc);
        txtAmount = new JTextField();
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(txtAmount, gbc);

        // Ngày giao dịch (Tích hợp lịch chọn thay vì gõ tay)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(new JLabel("Ngày giao dịch:"), gbc);
        txtDate = new JTextField(LocalDate.now().toString());
        txtDate.setEditable(false);
        txtDate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        txtDate.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new moneymate.view.component.DatePickerDialog(TransactionDialog.this, txtDate).setVisible(true);
            }
        });
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(txtDate, gbc);

        // Loại giao dịch
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        panel.add(new JLabel("Loại:"), gbc);
        cbType = new JComboBox<>(new String[]{"EXPENSE", "INCOME"});
        cbType.addActionListener(e -> {
            updateCategoryList();
            updateExtraLabel();
        });
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(cbType, gbc);

        // Danh mục
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        panel.add(new JLabel("Danh mục:"), gbc);
        cbCategory = new JComboBox<>();
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(cbCategory, gbc);

        // Mô tả
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        panel.add(new JLabel("Mô tả:"), gbc);
        txtDescription = new JTextField();
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(txtDescription, gbc);

        // Thông tin phụ (Đa hình: Nguồn thu hoặc Phương thức thanh toán)
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
        lblExtra = new JLabel("Phương thức:");
        panel.add(lblExtra, gbc);
        txtExtraInfo = new JTextField();
        txtExtraInfo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ví dụ: Tiền mặt, Thẻ tín dụng...");
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(txtExtraInfo, gbc);

        // Nút Lưu
        btnSave = new JButton("Lưu Giao Dịch");
        btnSave.putClientProperty(FlatClientProperties.STYLE, "background: #4F46E5; foreground: #ffffff");
        btnSave.addActionListener(e -> saveTransaction());
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 8, 8, 8);
        panel.add(btnSave, gbc);

        add(panel);

        // Load dữ liệu ban đầu
        updateCategoryList();
        updateExtraLabel();
    }

    private void updateCategoryList() {
        String selectedType = (String) cbType.getSelectedItem();
        cbCategory.removeAllItems();
        List<Category> filtered = allCategories.stream()
                .filter(c -> c.getType().equalsIgnoreCase(selectedType))
                .collect(Collectors.toList());
        for (Category c : filtered) {
            cbCategory.addItem(c);
        }
    }

    private void updateExtraLabel() {
        String type = (String) cbType.getSelectedItem();
        if ("INCOME".equals(type)) {
            lblExtra.setText("Nguồn thu:");
            txtExtraInfo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ví dụ: Tiền lương, Công ty A...");
        } else {
            lblExtra.setText("Thanh toán:");
            txtExtraInfo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ví dụ: Tiền mặt, Ví Momo, Thẻ...");
        }
    }

    private void fillFormForEdit() {
        txtTitle.setText(existingTransaction.getTitle());
        txtAmount.setText(String.format("%.0f", existingTransaction.getAmount()));
        txtDate.setText(existingTransaction.getDate());
        cbType.setSelectedItem(existingTransaction.getType());
        
        // Chọn đúng category trong combobox
        for (int i = 0; i < cbCategory.getItemCount(); i++) {
            Category c = cbCategory.getItemAt(i);
            if (c.getId() == existingTransaction.getCategory().getId()) {
                cbCategory.setSelectedIndex(i);
                break;
            }
        }
        
        txtDescription.setText(existingTransaction.getDescription());
        if (existingTransaction instanceof Income) {
            txtExtraInfo.setText(((Income) existingTransaction).getSource());
        } else if (existingTransaction instanceof Expense) {
            txtExtraInfo.setText(((Expense) existingTransaction).getPaymentMethod());
        }
    }

    private void saveTransaction() {
        try {
            String title = txtTitle.getText();
            double amount = Double.parseDouble(txtAmount.getText());
            String date = txtDate.getText();
            String type = (String) cbType.getSelectedItem();
            Category category = (Category) cbCategory.getSelectedItem();
            String desc = txtDescription.getText();
            String extra = txtExtraInfo.getText();

            if (category == null) {
                throw new ValidationException("Vui lòng tạo danh mục phù hợp trước.");
            }

            if (existingTransaction == null) {
                // Tạo mới sử dụng Factory Pattern
                Transaction t = TransactionFactory.createTransaction(type, 0, title, amount, date, category, desc, extra);
                transactionService.addTransaction(t, userId);
            } else {
                // Sửa giao dịch đã có
                if (existingTransaction.getType().equalsIgnoreCase(type)) {
                    // Nếu loại giao dịch không thay đổi, cập nhật trực tiếp trên đối tượng cũ
                    existingTransaction.setTitle(title);
                    existingTransaction.setAmount(amount);
                    existingTransaction.setDate(date);
                    existingTransaction.setCategory(category);
                    existingTransaction.setDescription(desc);
                    if (existingTransaction instanceof Income) {
                        ((Income) existingTransaction).setSource(extra);
                    } else if (existingTransaction instanceof Expense) {
                        ((Expense) existingTransaction).setPaymentMethod(extra);
                    }
                    transactionService.updateTransaction(existingTransaction);
                } else {
                    // Nếu thay đổi loại giao dịch (ví dụ: chuyển từ Chi tiêu thành Thu nhập),
                    // chúng ta tạo đối tượng con mới thông qua Factory Pattern với cùng ID ban đầu
                    Transaction newT = TransactionFactory.createTransaction(type, existingTransaction.getId(), title, amount, date, category, desc, extra);
                    transactionService.updateTransaction(newT);
                }
            }

            onDataChanged.run();
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số tiền phải là một chữ số hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException | DatabaseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        }
    }
}
