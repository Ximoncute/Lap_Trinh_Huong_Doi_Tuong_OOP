package moneymate.view;

import com.formdev.flatlaf.FlatClientProperties;
import moneymate.exception.DatabaseException;
import moneymate.exception.ValidationException;
import moneymate.model.Category;
import moneymate.service.CategoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoryDialog extends JDialog {
    private final CategoryService categoryService;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName;
    private JComboBox<String> cbType;
    private JTextField txtBudget;
    private JButton btnAdd, btnUpdate, btnDelete;
    private Category selectedCategory = null;
    private List<Category> currentCategories = null;
    private final Runnable onDataChanged; // Callback để báo về MainFrame load lại data

    public CategoryDialog(JFrame parent, CategoryService categoryService, Runnable onDataChanged) {
        super(parent, "Quản lý Danh mục", true);
        this.categoryService = categoryService;
        this.onDataChanged = onDataChanged;
        
        setSize(600, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        loadCategories();
    }

    private void initComponents() {
        // --- Form nhập liệu (Bên trái) ---
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        pnlForm.add(new JLabel("Tên danh mục:"), gbc);
        txtName = new JTextField();
        txtName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên danh mục");
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnlForm.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pnlForm.add(new JLabel("Loại:"), gbc);
        cbType = new JComboBox<>(new String[]{"INCOME", "EXPENSE"});
        cbType.addActionListener(e -> {
            boolean isExpense = "EXPENSE".equals(cbType.getSelectedItem());
            txtBudget.setEnabled(isExpense);
            if (!isExpense) txtBudget.setText("0");
        });
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnlForm.add(cbType, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        pnlForm.add(new JLabel("Hạn mức (VND):"), gbc);
        txtBudget = new JTextField("0");
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnlForm.add(txtBudget, gbc);

        // Nút bấm
        JPanel pnlButtons = new JPanel(new GridLayout(1, 3, 5, 5));
        btnAdd = new JButton("Thêm");
        btnUpdate = new JButton("Sửa");
        btnDelete = new JButton("Xóa");
        
        // CSS Style FlatLaf
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "background: #10B981; foreground: #ffffff");
        btnUpdate.putClientProperty(FlatClientProperties.STYLE, "background: #4F46E5; foreground: #ffffff");
        btnDelete.putClientProperty(FlatClientProperties.STYLE, "background: #EF4444; foreground: #ffffff");

        pnlButtons.add(btnAdd);
        pnlButtons.add(btnUpdate);
        pnlButtons.add(btnDelete);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        pnlForm.add(pnlButtons, gbc);

        add(pnlForm, BorderLayout.WEST);

        // --- Bảng danh sách (Bên phải) ---
        tableModel = new DefaultTableModel(new String[]{"STT", "Tên Danh Mục", "Loại", "Hạn mức"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromSelectedRow());
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách danh mục"));
        add(scrollPane, BorderLayout.CENTER);

        // Xử lý sự kiện nút bấm
        btnAdd.addActionListener(e -> saveCategory(true));
        btnUpdate.addActionListener(e -> saveCategory(false));
        btnDelete.addActionListener(e -> deleteCategory());
    }

    private void loadCategories() {
        try {
            tableModel.setRowCount(0);
            currentCategories = categoryService.getAllCategories();
            int stt = 1;
            for (Category c : currentCategories) {
                String budgetStr = String.format("%.0f", c.getBudgetLimit());
                tableModel.addRow(new Object[]{stt++, c.getName(), c.getType(), budgetStr});
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row != -1 && currentCategories != null && row < currentCategories.size()) {
            selectedCategory = currentCategories.get(row);
            txtName.setText(selectedCategory.getName());
            cbType.setSelectedItem(selectedCategory.getType());
            txtBudget.setText(String.format("%.0f", selectedCategory.getBudgetLimit()));
        }
    }

    private void saveCategory(boolean isNew) {
        try {
            String name = txtName.getText();
            String type = (String) cbType.getSelectedItem();
            double budget = Double.parseDouble(txtBudget.getText());

            if (isNew) {
                Category c = new Category(0, name, type, budget);
                categoryService.addCategory(c);
            } else {
                if (selectedCategory == null) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn một danh mục để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                selectedCategory.setName(name);
                selectedCategory.setType(type);
                selectedCategory.setBudgetLimit(budget);
                categoryService.updateCategory(selectedCategory);
            }
            loadCategories();
            clearForm();
            onDataChanged.run();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Hạn mức phải là chữ số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException | DatabaseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCategory() {
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một danh mục để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc muốn xóa danh mục '" + selectedCategory.getName() + "' không?\nTất cả giao dịch liên quan sẽ bị xóa!", 
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                categoryService.deleteCategory(selectedCategory.getId());
                loadCategories();
                clearForm();
                onDataChanged.run();
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtName.setText("");
        cbType.setSelectedIndex(0);
        txtBudget.setText("0");
        selectedCategory = null;
        table.clearSelection();
    }
}
