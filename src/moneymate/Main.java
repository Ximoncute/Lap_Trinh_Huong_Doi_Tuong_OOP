package moneymate;

import com.formdev.flatlaf.FlatLightLaf;
import moneymate.repository.DatabaseHelper;
import moneymate.view.AuthFrame;
import moneymate.view.MainFrame;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // 1. Khởi động giao diện hiện đại FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Không thể thiết lập FlatLaf: " + e.getMessage());
        }

        // 2. Kích hoạt DatabaseHelper để tự động kết nối và tạo bảng SQLite (nếu chưa có)
        try (Connection conn = DatabaseHelper.getConnection()) {
            if (conn != null) {
                System.out.println("[INFO] SQLite database connection established successfully.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Database connection error during startup: " + e.getMessage());
        }

        // 3. Khởi tạo và hiển thị AuthFrame (màn hình Đăng nhập / Đăng ký) trên UI Thread
        SwingUtilities.invokeLater(() -> {
            AuthFrame authFrame = new AuthFrame();
            authFrame.setVisible(true);
        });
    }
}
