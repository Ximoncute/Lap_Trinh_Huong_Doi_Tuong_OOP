package moneymate.view;

import com.formdev.flatlaf.FlatClientProperties;
import moneymate.exception.DatabaseException;
import moneymate.model.Category;
import moneymate.model.Transaction;
import moneymate.model.Income;
import moneymate.model.Expense;
import moneymate.service.CategoryService;
import moneymate.service.TransactionService;
import moneymate.util.CSVUtil;
import moneymate.util.CurrencyUtil;
import moneymate.view.component.CustomChartPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import moneymate.model.User;

public class MainFrame extends JFrame {
    private final CategoryService categoryService;
    private final TransactionService transactionService;
    private List<Transaction> allTransactions = new ArrayList<>();
    private List<Transaction> currentFilteredList = new ArrayList<>();
    private User currentUser;

    // Components
    private CardLayout cardLayout;
    private JPanel pnlContent;
    
    // Dashboard Components
    private JLabel lblBalanceVal, lblIncomeVal, lblExpenseVal;
    private CustomChartPanel pieChartPanel, barChartPanel;
    private JPanel pnlWarnings;
    private JLabel lblWarningText;
    private JTextField txtDashFrom, txtDashTo;
    private moneymate.view.component.ProfilePanel profilePanel;
    private moneymate.view.component.TransferPanel transferPanel;
    private moneymate.view.component.AboutPanel aboutPanel;

    // Transactions Components
    private JTable tblTransactions;
    private DefaultTableModel tblModel;
    private JTextField txtSearch;
    private JComboBox<String> cbFilterType;
    private JComboBox<String> cbFilterCategory;

    public MainFrame(User currentUser) {
        this.currentUser = currentUser;
        this.categoryService = new CategoryService();
        this.transactionService = new TransactionService();

        setTitle("MoneyMate - Quản lý Chi tiêu Tài chính");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);

        initLayout();
        refreshData();
    }

    private void initLayout() {
        setLayout(new BorderLayout());

        // 1. Sidebar (Bên trái)
        JPanel pnlSidebar = new JPanel(new GridLayout(8, 1, 10, 10));
        pnlSidebar.setBackground(new Color(30, 41, 59)); // Slate 800
        pnlSidebar.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel lblLogo = new JLabel("MoneyMate", JLabel.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLogo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                cardLayout.show(pnlContent, "About");
            }
        });
        pnlSidebar.add(lblLogo);

        JButton btnDash = createSidebarButton("Dashboard");
        JButton btnTrans = createSidebarButton("Giao Dịch");
        JButton btnCat = createSidebarButton("Danh Mục");
        JButton btnTransferSide = createSidebarButton("Chuyển Khoản");
        JButton btnProfile = createSidebarButton("Cá Nhân");

        btnDash.addActionListener(e -> cardLayout.show(pnlContent, "Dashboard"));
        btnTrans.addActionListener(e -> cardLayout.show(pnlContent, "Transactions"));
        btnCat.addActionListener(e -> new CategoryDialog(this, categoryService, this::refreshData).setVisible(true));
        btnTransferSide.addActionListener(e -> cardLayout.show(pnlContent, "Transfer"));
        btnProfile.addActionListener(e -> cardLayout.show(pnlContent, "Profile"));

        pnlSidebar.add(btnDash);
        pnlSidebar.add(btnTrans);
        pnlSidebar.add(btnCat);
        pnlSidebar.add(btnTransferSide);
        pnlSidebar.add(btnProfile);
        add(pnlSidebar, BorderLayout.WEST);

        // 2. Content Area (Bên phải - sử dụng CardLayout)
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(new Color(248, 250, 252)); // Slate 50

        // Khởi tạo màn hình Cá Nhân, Chuyển Khoản & Cố vấn tài chính
        profilePanel = new moneymate.view.component.ProfilePanel(this, currentUser, this::refreshData);
        transferPanel = new moneymate.view.component.TransferPanel(this, currentUser, transactionService, this::refreshData);
        aboutPanel = new moneymate.view.component.AboutPanel(currentUser, this::refreshData);

        // Khởi tạo các màn hình chính
        pnlContent.add(createDashboardPanel(), "Dashboard");
        pnlContent.add(createTransactionsPanel(), "Transactions");
        pnlContent.add(profilePanel, "Profile");
        pnlContent.add(transferPanel, "Transfer");
        pnlContent.add(aboutPanel, "About");

        add(pnlContent, BorderLayout.CENTER);
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #334155; pressedBackground: #475569");
        return btn;
    }

    // --- MÀN HÌNH DASHBOARD ---
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);

        // Header + Warnings banner (Sử dụng GridBagLayout 2 dòng độc lập để tránh đè chữ)
        JPanel pnlHeader = new JPanel(new GridBagLayout());
        pnlHeader.setOpaque(false);
        GridBagConstraints gbcH = new GridBagConstraints();
        gbcH.fill = GridBagConstraints.HORIZONTAL;
        gbcH.gridx = 0;

        // Dòng 1: Tiêu đề + Thêm nhanh
        JPanel pnlRow1 = new JPanel(new BorderLayout());
        pnlRow1.setOpaque(false);
        JLabel lblTitle = new JLabel("Tổng quan tài chính");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(15, 23, 42));
        pnlRow1.add(lblTitle, BorderLayout.WEST);

        JButton btnQuickAdd = new JButton("Thêm giao dịch nhanh");
        btnQuickAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnQuickAdd.putClientProperty(FlatClientProperties.STYLE, "background: #4F46E5; foreground: #ffffff; arc: 8");
        btnQuickAdd.addActionListener(e -> new TransactionDialog(this, categoryService, transactionService, null, currentUser.getId(), this::refreshData).setVisible(true));
        pnlRow1.add(btnQuickAdd, BorderLayout.EAST);

        gbcH.gridy = 0;
        gbcH.weightx = 1.0;
        gbcH.insets = new Insets(0, 0, 10, 0);
        pnlHeader.add(pnlRow1, gbcH);

        // Dòng 2: Bộ lọc ngày
        JPanel pnlRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlRow2.setOpaque(false);

        pnlRow2.add(new JLabel("Từ:"));
        txtDashFrom = new JTextField(9);
        txtDashFrom.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "YYYY-MM-DD");
        txtDashFrom.setEditable(false);
        txtDashFrom.setCursor(new Cursor(Cursor.HAND_CURSOR));
        txtDashFrom.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new moneymate.view.component.DatePickerDialog(MainFrame.this, txtDashFrom).setVisible(true);
            }
        });
        pnlRow2.add(txtDashFrom);

        pnlRow2.add(new JLabel("Đến:"));
        txtDashTo = new JTextField(9);
        txtDashTo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "YYYY-MM-DD");
        txtDashTo.setEditable(false);
        txtDashTo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        txtDashTo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new moneymate.view.component.DatePickerDialog(MainFrame.this, txtDashTo).setVisible(true);
            }
        });
        pnlRow2.add(txtDashTo);

        JButton btnFilter = new JButton("Lọc");
        btnFilter.putClientProperty(FlatClientProperties.STYLE, "background: #334155; foreground: #ffffff; arc: 8");
        btnFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFilter.addActionListener(e -> refreshData());
        pnlRow2.add(btnFilter);

        JButton btnReset = new JButton("Đặt lại");
        btnReset.putClientProperty(FlatClientProperties.STYLE, "background: #64748B; foreground: #ffffff; arc: 8");
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReset.addActionListener(e -> {
            txtDashFrom.setText("");
            txtDashTo.setText("");
            refreshData();
        });
        pnlRow2.add(btnReset);

        gbcH.gridy = 1;
        gbcH.insets = new Insets(0, 0, 10, 0);
        pnlHeader.add(pnlRow2, gbcH);

        // Banner Cảnh báo ngân sách
        pnlWarnings = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlWarnings.setBackground(new Color(254, 242, 242)); // Đỏ nhạt
        pnlWarnings.setBorder(BorderFactory.createLineBorder(new Color(252, 165, 165), 1, true));
        lblWarningText = new JLabel("");
        lblWarningText.setForeground(new Color(220, 38, 38));
        lblWarningText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlWarnings.add(lblWarningText);
        pnlWarnings.setVisible(false);
        gbcH.gridy = 2;
        gbcH.insets = new Insets(0, 0, 10, 0);
        pnlHeader.add(pnlWarnings, gbcH);

        panel.add(pnlHeader, BorderLayout.NORTH);

        // Grid chứa 3 Thẻ thống kê
        JPanel pnlCards = new JPanel(new GridLayout(1, 3, 20, 20));
        pnlCards.setOpaque(false);

        JPanel cardBal = createStatCard("SỐ DƯ HIỆN TẠI", lblBalanceVal = new JLabel("0 ₫"), new Color(79, 70, 229));
        JPanel cardInc = createStatCard("TỔNG THU NHẬP", lblIncomeVal = new JLabel("0 ₫"), new Color(16, 185, 129));
        JPanel cardExp = createStatCard("TỔNG CHI TIÊU", lblExpenseVal = new JLabel("0 ₫"), new Color(239, 68, 68));

        pnlCards.add(cardBal);
        pnlCards.add(cardInc);
        pnlCards.add(cardExp);

        // Biểu đồ
        JPanel pnlCharts = new JPanel(new GridLayout(1, 2, 20, 20));
        pnlCharts.setOpaque(false);

        pieChartPanel = new CustomChartPanel(true);
        pieChartPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; border: 1,1,1,1,#E2E8F0");
        
        barChartPanel = new CustomChartPanel(false);
        barChartPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; border: 1,1,1,1,#E2E8F0");

        pnlCharts.add(pieChartPanel);
        pnlCharts.add(barChartPanel);

        JPanel pnlCenter = new JPanel(new BorderLayout(15, 15));
        pnlCenter.setOpaque(false);
        pnlCenter.add(pnlCards, BorderLayout.NORTH);
        pnlCenter.add(pnlCharts, BorderLayout.CENTER);

        panel.add(pnlCenter, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(String title, JLabel valLabel, Color primaryColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(148, 163, 184)); // Slate 400
        card.add(lblTitle, BorderLayout.NORTH);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLabel.setForeground(primaryColor);
        card.add(valLabel, BorderLayout.CENTER);

        return card;
    }

    // --- MÀN HÌNH DANH SÁCH GIAO DỊCH ---
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);

        // Header điều khiển (Tìm kiếm, lọc)
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlFilters.setBackground(Color.WHITE);
        pnlFilters.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        pnlFilters.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        txtSearch = new JTextField(15);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo tiêu đề...");
        txtSearch.addCaretListener(e -> performFilter());
        pnlFilters.add(new JLabel("Tìm kiếm:"));
        pnlFilters.add(txtSearch);

        cbFilterType = new JComboBox<>(new String[]{"Tất cả", "INCOME", "EXPENSE"});
        cbFilterType.addActionListener(e -> performFilter());
        pnlFilters.add(new JLabel("Loại:"));
        pnlFilters.add(cbFilterType);

        cbFilterCategory = new JComboBox<>();
        cbFilterCategory.addActionListener(e -> performFilter());
        pnlFilters.add(new JLabel("Danh mục:"));
        pnlFilters.add(cbFilterCategory);

        JButton btnClearFilter = new JButton("Đặt lại");
        btnClearFilter.addActionListener(e -> {
            txtSearch.setText("");
            cbFilterType.setSelectedIndex(0);
            cbFilterCategory.setSelectedIndex(0);
        });
        pnlFilters.add(btnClearFilter);

        panel.add(pnlFilters, BorderLayout.NORTH);

        // Bảng dữ liệu (Hiển thị cột STT tự động tăng thay thế cho mã ID thô trong CSDL)
        tblModel = new DefaultTableModel(new String[]{"STT", "Ngày", "Tiêu đề", "Loại", "Danh mục", "Số tiền", "Mô tả", "Thông tin thêm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblTransactions = new JTable(tblModel);
        tblTransactions.setRowHeight(25);
        tblTransactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Sự kiện double-click để sửa nhanh giao dịch (Hỗ trợ sửa được cả Tổng thu nhập)
        tblTransactions.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditDialog();
                }
            }
        });
        
        // Màu sắc phân biệt Thu/Chi
        tblTransactions.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String type = (String) table.getValueAt(row, 3);
                
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(new Color(51, 65, 85));
                }

                // Cột Số tiền (cột 5)
                if (column == 5) {
                    double amount = (double) value;
                    setText(CurrencyUtil.formatVND(amount));
                    setFont(getFont().deriveFont(Font.BOLD));
                    if (!isSelected) {
                        c.setForeground("INCOME".equals(type) ? new Color(16, 185, 129) : new Color(239, 68, 68));
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblTransactions);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Nút chức năng bên dưới
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);

        JButton btnAdd = new JButton("Thêm giao dịch");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnImport = new JButton("Nhập CSV");
        JButton btnExport = new JButton("Xuất CSV");

        btnAdd.putClientProperty(FlatClientProperties.STYLE, "background: #4F46E5; foreground: #ffffff");
        btnEdit.putClientProperty(FlatClientProperties.STYLE, "background: #F59E0B; foreground: #ffffff");
        btnDelete.putClientProperty(FlatClientProperties.STYLE, "background: #EF4444; foreground: #ffffff");

        btnAdd.addActionListener(e -> new TransactionDialog(this, categoryService, transactionService, null, currentUser.getId(), this::refreshData).setVisible(true));
        btnEdit.addActionListener(e -> openEditDialog());
        btnDelete.addActionListener(e -> deleteTransaction());
        btnImport.addActionListener(e -> importCSV());
        btnExport.addActionListener(e -> exportCSV());

        pnlActions.add(btnImport);
        pnlActions.add(btnExport);
        pnlActions.add(btnDelete);
        pnlActions.add(btnEdit);
        pnlActions.add(btnAdd);

        panel.add(pnlActions, BorderLayout.SOUTH);
        return panel;
    }

    // --- NGHIỆP VỤ ĐIỀU KHIỂN & LÀM MỚI DỮ LIỆU ---
    public void refreshData() {
        try {
            allTransactions = transactionService.getAllTransactions(currentUser.getId());
            
            // Lọc danh sách giao dịch hiển thị trên Dashboard theo ngày nhập tùy chọn (Truy xuất ngày/tháng/năm)
            List<Transaction> filtered = allTransactions;
            String fromDate = txtDashFrom != null ? txtDashFrom.getText().trim() : "";
            String toDate = txtDashTo != null ? txtDashTo.getText().trim() : "";

            if (!fromDate.isEmpty()) {
                filtered = filtered.stream()
                        .filter(t -> t.getDate().compareTo(fromDate) >= 0)
                        .collect(Collectors.toList());
            }
            if (!toDate.isEmpty()) {
                filtered = filtered.stream()
                        .filter(t -> t.getDate().compareTo(toDate) <= 0)
                        .collect(Collectors.toList());
            }

            // 1. Cập nhật Dashboard với dữ liệu đã lọc
            double income = transactionService.getTotalIncome(filtered);
            double expense = transactionService.getTotalExpense(filtered);
            double balance = transactionService.getNetBalance(filtered);

            lblIncomeVal.setText(CurrencyUtil.formatVND(income));
            lblExpenseVal.setText(CurrencyUtil.formatVND(expense));
            lblBalanceVal.setText(CurrencyUtil.formatVND(balance));

            // Cập nhật biểu đồ tròn và cột động theo danh sách đã lọc
            pieChartPanel.setPieData(transactionService.getExpenseByCategory(filtered));
            barChartPanel.setBarData(transactionService.getMonthlyCashFlow(filtered));

            // Đồng bộ hoá số dư và tiến trình ngoài trang Cá Nhân kiểu ngân hàng
            if (profilePanel != null) {
                profilePanel.syncFinancialProgress(balance, income, expense);
            }

            // Đồng bộ số dư sang màn hình Chuyển khoản
            if (transferPanel != null) {
                transferPanel.updateSenderBalance(balance);
            }

            // Cảnh báo ngân sách
            List<String> warnings = transactionService.getBudgetWarnings(allTransactions, currentUser);
            if (!warnings.isEmpty()) {
                lblWarningText.setText(String.join(" | ", warnings));
                pnlWarnings.setVisible(true);
            } else {
                pnlWarnings.setVisible(false);
            }

            // 2. Cập nhật Combobox Lọc danh mục bên màn hình danh sách
            String selectedCat = (String) cbFilterCategory.getSelectedItem();
            cbFilterCategory.removeAllItems();
            cbFilterCategory.addItem("Tất cả");
            List<Category> categories = categoryService.getAllCategories();
            for (Category c : categories) {
                cbFilterCategory.addItem(c.getName());
            }
            if (selectedCat != null) {
                cbFilterCategory.setSelectedItem(selectedCat);
            }

            // 3. Đổ dữ liệu vào bảng
            performFilter();

        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Lỗi nạp dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performFilter() {
        String keyword = txtSearch.getText().toLowerCase().trim();
        String typeFilter = (String) cbFilterType.getSelectedItem();
        String categoryFilter = (String) cbFilterCategory.getSelectedItem();

        currentFilteredList = allTransactions.stream()
                .filter(t -> t.getTitle().toLowerCase().contains(keyword))
                .filter(t -> "Tất cả".equals(typeFilter) || typeFilter == null || t.getType().equalsIgnoreCase(typeFilter))
                .filter(t -> "Tất cả".equals(categoryFilter) || categoryFilter == null || t.getCategory().getName().equalsIgnoreCase(categoryFilter))
                .collect(Collectors.toList());

        tblModel.setRowCount(0);
        int stt = 1;
        for (Transaction t : currentFilteredList) {
            String extraInfo = t instanceof Income ? ((Income) t).getSource() : ((Expense) t).getPaymentMethod();
            tblModel.addRow(new Object[]{
                    stt++, // Hiển thị số thứ tự STT tăng dần liên tục từ 1
                    t.getDate(),
                    t.getTitle(),
                    t.getType(),
                    t.getCategory().getName(),
                    t.getAmount(),
                    t.getDescription() == null ? "" : t.getDescription(),
                    extraInfo
            });
        }
    }

    private void openEditDialog() {
        int row = tblTransactions.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giao dịch để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentFilteredList != null && row < currentFilteredList.size()) {
            Transaction t = currentFilteredList.get(row);
            new TransactionDialog(this, categoryService, transactionService, t, currentUser.getId(), this::refreshData).setVisible(true);
        }
    }

    private void deleteTransaction() {
        int row = tblTransactions.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giao dịch để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentFilteredList != null && row < currentFilteredList.size()) {
            Transaction t = currentFilteredList.get(row);
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Bạn có chắc muốn xóa giao dịch '" + t.getTitle() + "' không?", 
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    transactionService.deleteTransaction(t.getId());
                    refreshData();
                } catch (DatabaseException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void exportCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file CSV");
        fileChooser.setSelectedFile(new File("giao_dich.csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CSVUtil.exportToCSV(allTransactions, fileChooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Xuất file CSV thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất file: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file CSV cần nhập");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<Transaction> imported = CSVUtil.importFromCSV(fileChooser.getSelectedFile(), categoryService);
                for (Transaction t : imported) {
                    transactionService.addTransaction(t, currentUser.getId());
                }
                refreshData();
                JOptionPane.showMessageDialog(this, "Đã nhập thành công " + imported.size() + " giao dịch!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi nhập file: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
