package moneymate.view.component;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Hộp thoại Chọn ngày dạng lịch (Calendar Date Picker) cực kỳ trực quan và tiện dụng.
 * Tự động đồng bộ với ô nhập văn bản, giúp người dùng click chọn ngày nhanh mà không cần gõ tay.
 */
public class DatePickerDialog extends JDialog {
    private final JTextField targetField;
    private LocalDate selectedDate;
    private JLabel lblMonthYear;
    private JPanel pnlDays;

    public DatePickerDialog(Window parent, JTextField targetField) {
        super(parent, "Chọn ngày", ModalityType.APPLICATION_MODAL);
        this.targetField = targetField;
        
        // Đọc ngày hiện tại từ text field để hiển thị lịch đúng tháng đó
        String currentText = targetField.getText().trim();
        try {
            this.selectedDate = LocalDate.parse(currentText);
        } catch (Exception e) {
            this.selectedDate = LocalDate.now();
        }

        setSize(320, 300);
        setLocationRelativeTo(targetField);
        setResizable(false);
        setLayout(new BorderLayout(5, 5));
        
        initComponents();
        renderCalendar();
    }

    private void initComponents() {
        // Thanh tiêu đề chọn Tháng/Năm
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlHeader.setBackground(new Color(248, 250, 252)); // Slate 50
        
        JButton btnPrev = new JButton("<");
        btnPrev.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPrev.setFocusPainted(false);
        btnPrev.addActionListener(e -> {
            selectedDate = selectedDate.minusMonths(1);
            renderCalendar();
        });

        JButton btnNext = new JButton(">");
        btnNext.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNext.setFocusPainted(false);
        btnNext.addActionListener(e -> {
            selectedDate = selectedDate.plusMonths(1);
            renderCalendar();
        });

        lblMonthYear = new JLabel("", JLabel.CENTER);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMonthYear.setForeground(new Color(15, 23, 42)); // Slate 900

        pnlHeader.add(btnPrev, BorderLayout.WEST);
        pnlHeader.add(lblMonthYear, BorderLayout.CENTER);
        pnlHeader.add(btnNext, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // Khung chứa lưới các ngày
        pnlDays = new JPanel(new GridLayout(0, 7, 3, 3));
        pnlDays.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlDays.setBackground(Color.WHITE);
        add(pnlDays, BorderLayout.CENTER);
    }

    private void renderCalendar() {
        pnlDays.removeAll();
        lblMonthYear.setText(String.format("Tháng %02d - %d", selectedDate.getMonthValue(), selectedDate.getYear()));

        // Vẽ hàng tiêu đề Thứ
        String[] headers = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (String h : headers) {
            JLabel lbl = new JLabel(h, JLabel.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(new Color(100, 116, 139)); // Slate 500
            pnlDays.add(lbl);
        }

        // Tính ngày trong tháng
        YearMonth yearMonth = YearMonth.of(selectedDate.getYear(), selectedDate.getMonthValue());
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeekVal = firstOfMonth.getDayOfWeek().getValue(); // 1 (Thứ 2) -> 7 (Chủ nhật)

        // Điền các ô trống trước ngày mùng 1
        for (int i = 1; i < dayOfWeekVal; i++) {
            pnlDays.add(new JLabel(""));
        }

        // Điền danh sách các ngày trong tháng
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();
        
        for (int day = 1; day <= daysInMonth; day++) {
            final int selectedDay = day;
            JButton btnDay = new JButton(String.valueOf(day));
            btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnDay.setFocusPainted(false);
            btnDay.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249), 1));
            
            LocalDate btnDate = firstOfMonth.withDayOfMonth(day);
            // Highlight ngày hiện tại hoặc ngày đang chọn
            if (btnDate.equals(today)) {
                btnDay.setBackground(new Color(239, 246, 255)); // Xanh dương nhạt
                btnDay.setForeground(new Color(37, 99, 235)); // Xanh dương đậm
                btnDay.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else {
                btnDay.setBackground(Color.WHITE);
                btnDay.setForeground(new Color(71, 85, 105)); // Slate 600
            }

            btnDay.addActionListener(e -> {
                LocalDate result = firstOfMonth.withDayOfMonth(selectedDay);
                targetField.setText(result.format(DateTimeFormatter.ISO_LOCAL_DATE));
                dispose();
            });
            pnlDays.add(btnDay);
        }

        pnlDays.revalidate();
        pnlDays.repaint();
    }
}
