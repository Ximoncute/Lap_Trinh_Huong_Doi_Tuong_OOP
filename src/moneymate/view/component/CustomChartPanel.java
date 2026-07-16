package moneymate.view.component;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

/**
 * Custom JPanel tự vẽ biểu đồ (Tròn kiểu Donut bóng đổ & Cột bo góc thông minh kèm Gradients)
 * thiết kế chuẩn UI hiện đại cực kỳ cao cấp và bắt mắt.
 * Đã tối ưu hóa cỡ chữ lớn, cột dày dặn, chống chồng chéo chữ thông minh.
 */
public class CustomChartPanel extends JPanel {
    private final boolean isPieChart;
    private Map<String, Double> pieData;
    private Map<String, double[]> barData; // Month -> [Income, Expense]

    private final Color[] PIE_COLORS = {
            new Color(99, 102, 241),   // Indigo (#6366F1)
            new Color(16, 185, 129),  // Emerald (#10B981)
            new Color(245, 158, 11),  // Amber (#F59E0B)
            new Color(244, 63, 94),   // Rose (#F43F5E)
            new Color(139, 92, 246),  // Violet (#8B5CF6)
            new Color(6, 182, 212),   // Cyan (#06B6D4)
            new Color(249, 115, 22)   // Orange (#F97316)
    };

    public CustomChartPanel(boolean isPieChart) {
        this.isPieChart = isPieChart;
        setBackground(Color.WHITE);
        setOpaque(true);
    }

    public void setPieData(Map<String, Double> pieData) {
        this.pieData = pieData;
        this.repaint();
    }

    public void setBarData(Map<String, double[]> barData) {
        this.barData = barData;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Bật khử răng cưa tối đa cho cả hình vẽ lẫn chữ viết sắc nét
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (isPieChart) {
            drawDonutChart(g2d);
        } else {
            drawBarChart(g2d);
        }
    }

    private void drawDonutChart(Graphics2D g2d) {
        // Tiêu đề chữ to đậm, Slate 900
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2d.setColor(new Color(15, 23, 42));
        g2d.drawString("Cơ cấu chi tiêu danh mục", 20, 35);

        if (pieData == null || pieData.isEmpty()) {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2d.setColor(new Color(148, 163, 184));
            g2d.drawString("Không có dữ liệu chi tiêu", getWidth() / 2 - 70, getHeight() / 2);
            return;
        }

        double total = pieData.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) return;

        // Phân chia không gian Donut và Legend
        int leftW = (int) (getWidth() * 0.48);
        int topOffset = 60;
        int size = Math.min(leftW, getHeight() - topOffset) - 40;
        int x = (leftW - size) / 2;
        int y = topOffset + (getHeight() - topOffset - size) / 2;

        // 1. Vẽ bóng đổ nhẹ 3D cho biểu đồ tròn
        g2d.setColor(new Color(0, 0, 0, 12));
        g2d.fillOval(x + 2, y + 3, size, size);

        int startAngle = 0;
        int colorIndex = 0;

        // 2. Vẽ các lát cắt Donut
        for (Map.Entry<String, Double> entry : pieData.entrySet()) {
            int arcAngle = (int) Math.round((entry.getValue() / total) * 360);
            if (colorIndex == pieData.size() - 1) {
                arcAngle = 360 - startAngle;
            }
            g2d.setColor(PIE_COLORS[colorIndex % PIE_COLORS.length]);
            g2d.fillArc(x, y, size, size, startAngle, arcAngle);
            startAngle += arcAngle;
            colorIndex++;
        }

        // 3. Vẽ viền trắng mảnh phân tách các lát cắt cực sang trọng
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.5f));
        startAngle = 0;
        colorIndex = 0;
        for (Map.Entry<String, Double> entry : pieData.entrySet()) {
            int arcAngle = (int) Math.round((entry.getValue() / total) * 360);
            if (colorIndex == pieData.size() - 1) {
                arcAngle = 360 - startAngle;
            }
            g2d.drawArc(x, y, size, size, startAngle, arcAngle);
            startAngle += arcAngle;
            colorIndex++;
        }

        // 4. Vẽ lõi Donut che đè lên
        int holeSize = (int) (size * 0.66);
        int holeX = x + (size - holeSize) / 2;
        int holeY = y + (size - holeSize) / 2;
        g2d.setColor(Color.WHITE);
        g2d.fillOval(holeX, holeY, holeSize, holeSize);

        // Vẽ đường viền xám siêu mảnh bao quanh lõi Donut
        g2d.setColor(new Color(241, 245, 249)); // Slate 100
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawOval(holeX, holeY, holeSize, holeSize);

        // Ghi tổng chi tiêu chữ to rõ nét ở lõi
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2d.setColor(new Color(148, 163, 184)); // Slate 400
        int wTotalTxt = g2d.getFontMetrics().stringWidth("TỔNG CHI");
        g2d.drawString("TỔNG CHI", holeX + (holeSize - wTotalTxt) / 2, holeY + (holeSize / 2) - 8);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 17)); // Tăng font size số tiền ở lõi
        g2d.setColor(new Color(244, 63, 94)); // Rose 500
        String totalStr = formatValue(total);
        int wAmountTxt = g2d.getFontMetrics().stringWidth(totalStr);
        g2d.drawString(totalStr, holeX + (holeSize - wAmountTxt) / 2, holeY + (holeSize / 2) + 12);

        // Vẽ chú thích (Legend) thoáng, chữ to
        int legendX = leftW + 15;
        int legendY = topOffset + (getHeight() - topOffset - (pieData.size() * 28)) / 2;
        colorIndex = 0;

        for (Map.Entry<String, Double> entry : pieData.entrySet()) {
            int curY = legendY + (colorIndex * 28);
            
            // Vẽ badge tròn màu sắc
            g2d.setColor(PIE_COLORS[colorIndex % PIE_COLORS.length]);
            g2d.fillOval(legendX, curY + 3, 11, 11);

            // Nhãn chữ to rõ
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2d.setColor(new Color(71, 85, 105)); // Slate 600
            double percent = (entry.getValue() / total) * 100;
            String label = String.format("%s: %.1f%%", entry.getKey(), percent);
            g2d.drawString(label, legendX + 22, curY + 14);
            colorIndex++;
        }
    }

    private void drawBarChart(Graphics2D g2d) {
        // Tiêu đề
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2d.setColor(new Color(15, 23, 42)); // Slate 900
        g2d.drawString("Dòng tiền thu chi hàng tháng", 20, 35);

        if (barData == null || barData.isEmpty()) {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2d.setColor(new Color(148, 163, 184));
            g2d.drawString("Không có dữ liệu dòng tiền", getWidth() / 2 - 65, getHeight() / 2);
            return;
        }

        double maxVal = 0;
        for (double[] val : barData.values()) {
            maxVal = Math.max(maxVal, Math.max(val[0], val[1]));
        }
        if (maxVal == 0) maxVal = 1;

        // Căn chỉnh khoảng cách rộng rãi hơn nữa
        int leftPad = 50;
        int rightPad = 20;
        int topPad = 65;
        int bottomPad = 50; // Dành khoảng trống dưới cùng để vẽ Legend
        
        int w = getWidth() - leftPad - rightPad;
        int h = getHeight() - topPad - bottomPad;

        // 1. Vẽ các đường lưới nét đứt mờ chạy ngang
        g2d.setColor(new Color(241, 245, 249)); // Slate 100
        g2d.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{5f, 5f}, 0f));
        for (int k = 1; k <= 4; k++) {
            int gridY = topPad + h - (h * k / 4);
            g2d.drawLine(leftPad, gridY, leftPad + w, gridY);

            // Ghi giá trị Y ở bên lề trái
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2d.setColor(new Color(148, 163, 184));
            String yLabel = formatValue(maxVal * k / 4);
            g2d.drawString(yLabel, leftPad - 45, gridY + 4);
        }
        g2d.setStroke(new BasicStroke(1.2f)); // Reset nét vẽ

        // Vẽ trục tọa độ mảnh
        g2d.setColor(new Color(226, 232, 240));
        g2d.drawLine(leftPad, topPad, leftPad, topPad + h);
        g2d.drawLine(leftPad, topPad + h, leftPad + w, topPad + h);

        int numBars = barData.size();
        int groupW = w / numBars;
        
        // Cột to dày dặn, giới hạn chiều rộng tối đa là 65px để luôn cân đối
        int barW = Math.min((int) (groupW * 0.38), 65); 

        int i = 0;
        for (Map.Entry<String, double[]> entry : barData.entrySet()) {
            double income = entry.getValue()[0];
            double expense = entry.getValue()[1];

            // Giới hạn chiều cao tối đa của cột để luôn chừa ra khoảng trống an toàn
            int hIncome = (int) ((income / maxVal) * (h - 35));
            int hExpense = (int) ((expense / maxVal) * (h - 35));

            // Căn giữa hai cột thu và chi trong phạm vi nhóm của tháng đó
            int xStart = leftPad + (i * groupW) + (groupW - 2 * barW) / 2;

            // 1. Cột thu nhập (Xanh lá Gradient & Bo góc trên)
            if (income > 0) {
                int barX = xStart;
                int barY = topPad + h - hIncome;
                
                GradientPaint gpIncome = new GradientPaint(
                        barX, barY, new Color(52, 211, 153), // Mint (#34D399)
                        barX, barY + hIncome, new Color(16, 185, 129) // Emerald (#10B981)
                );
                g2d.setPaint(gpIncome);
                g2d.fill(new RoundRectangle2D.Double(barX, barY, barW - 2, hIncome, 6, 6));
                if (hIncome > 4) {
                    g2d.fillRect(barX, barY + hIncome - 4, barW - 2, 4); // Cạnh đáy phẳng
                }

                // GHI SỐ LIỆU THÔNG MINH TRÁNH CHỒNG CHÉO:
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String txt = formatValue(income);
                int txtW = g2d.getFontMetrics().stringWidth(txt);
                
                if (hIncome > 28) {
                    // Nếu cột cao, ghi chữ trắng nằm bên trong lòng cột
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(txt, barX + (barW - 2) / 2 - txtW / 2, barY + 14);
                } else {
                    // Nếu cột lùn, ghi chữ xanh lá đậm ngay phía trên cột
                    g2d.setColor(new Color(5, 150, 105));
                    g2d.drawString(txt, barX + (barW - 2) / 2 - txtW / 2, barY - 5);
                }
            }

            // 2. Cột chi tiêu (Đỏ Gradient & Bo góc trên)
            if (expense > 0) {
                int barX = xStart + barW;
                int barY = topPad + h - hExpense;

                GradientPaint gpExpense = new GradientPaint(
                        barX, barY, new Color(251, 113, 133), // Coral (#FB7185)
                        barX, barY + hExpense, new Color(244, 63, 94) // Rose (#F43F5E)
                );
                g2d.setPaint(gpExpense);
                g2d.fill(new RoundRectangle2D.Double(barX, barY, barW - 2, hExpense, 6, 6));
                if (hExpense > 4) {
                    g2d.fillRect(barX, barY + hExpense - 4, barW - 2, 4); // Cạnh đáy phẳng
                }

                // GHI SỐ LIỆU THÔNG MINH TRÁNH CHỒNG CHÉO:
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String txt = formatValue(expense);
                int txtW = g2d.getFontMetrics().stringWidth(txt);

                if (hExpense > 28) {
                    // Ghi bên trong lòng cột bằng chữ trắng
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(txt, barX + (barW - 2) / 2 - txtW / 2, barY + 14);
                } else {
                    // Ghi đè phía trên cột bằng chữ đỏ hồng
                    g2d.setColor(new Color(225, 29, 72));
                    g2d.drawString(txt, barX + (barW - 2) / 2 - txtW / 2, barY - 5);
                }
            }

            // Ghi nhãn tên tháng chữ to rõ nét định dạng Tháng MM/yy
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2d.setColor(new Color(148, 163, 184)); // Slate 400
            String formattedLabel = entry.getKey();
            if (formattedLabel.length() == 7 && formattedLabel.charAt(4) == '-') {
                String year = formattedLabel.substring(2, 4);
                String month = formattedLabel.substring(5, 7);
                formattedLabel = "Tháng " + month + "/" + year;
            }
            int lblW = g2d.getFontMetrics().stringWidth(formattedLabel);
            g2d.drawString(formattedLabel, xStart + barW - lblW / 2, topPad + h + 18);
            i++;
        }

        // Vẽ Legend ở dưới cùng nằm giữa biểu đồ
        int legendY = getHeight() - 15;
        int legendW = 180;
        int legendX = (getWidth() - legendW) / 2;
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        g2d.setColor(new Color(16, 185, 129));
        g2d.fillOval(legendX, legendY - 10, 10, 10);
        g2d.setColor(new Color(71, 85, 105)); // Slate 600
        g2d.drawString("Thu nhập", legendX + 16, legendY - 1);

        g2d.setColor(new Color(244, 63, 94));
        g2d.fillOval(legendX + 95, legendY - 10, 10, 10);
        g2d.setColor(new Color(71, 85, 105));
        g2d.drawString("Chi tiêu", legendX + 111, legendY - 1);
    }

    private String formatValue(double value) {
        if (value >= 1_000_000) {
            return String.format("%.1f Tr", value / 1_000_000.0).replace(".0", "");
        } else if (value >= 1_000) {
            return String.format("%.0f k", value / 1_000.0);
        } else {
            return String.format("%.0f", value);
        }
    }
}
