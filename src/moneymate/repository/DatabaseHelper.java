package moneymate.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tiện ích quản lý kết nối SQLite và tự khởi tạo cấu trúc bảng hoàn chỉnh.
 * Sử dụng Số điện thoại (phone) làm tên đăng nhập mặc định (loại bỏ trường username).
 */
public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:money_mate.db";

    static {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Kích hoạt ràng buộc khóa ngoại trong SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Tạo bảng user lưu trữ tài khoản cá nhân, SĐT (dùng làm tên đăng nhập), STK và mục tiêu tài chính
            stmt.execute("CREATE TABLE IF NOT EXISTS [user] (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "password TEXT NOT NULL," +
                    "full_name TEXT NOT NULL," +
                    "email TEXT NOT NULL," +
                    "phone TEXT NOT NULL UNIQUE," +
                    "account_number TEXT NOT NULL UNIQUE," +
                    "income_target REAL DEFAULT 20000000," +
                    "expense_limit REAL DEFAULT 10000000" +
                    ");");

            // 2. Tạo bảng category
            stmt.execute("CREATE TABLE IF NOT EXISTS category (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL UNIQUE," +
                    "type TEXT NOT NULL," +
                    "budget_limit REAL DEFAULT 0" +
                    ");");

            // 3. Tạo bảng transaction liên kết với user_id
            stmt.execute("CREATE TABLE IF NOT EXISTS [transaction] (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "title TEXT NOT NULL," +
                    "amount REAL NOT NULL," +
                    "date TEXT NOT NULL," +
                    "category_id INTEGER," +
                    "description TEXT," +
                    "type TEXT NOT NULL," +
                    "extra_info TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES [user](id) ON DELETE CASCADE," +
                    "FOREIGN KEY(category_id) REFERENCES category(id) ON DELETE CASCADE" +
                    ");");

            // 4. Chèn tài khoản admin mặc định nếu chưa có (Mật khẩu 'admin' hash SHA-256)
            // SĐT là '0987654321', STK là '0987654321'
            stmt.execute("INSERT OR IGNORE INTO [user] (id, password, full_name, email, phone, account_number, income_target, expense_limit) " +
                    "VALUES (1, '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Nguyễn Văn Anh', 'nguyenvananh@moneymate.vn', '0987654321', '0987654321', 20000000, 10000000);");

            // 5. Thêm dữ liệu danh mục mặc định
            stmt.execute("INSERT OR IGNORE INTO category (id, name, type, budget_limit) VALUES " +
                    "(1, 'Lương', 'INCOME', 0)," +
                    "(2, 'Kinh doanh', 'INCOME', 0)," +
                    "(3, 'Đầu tư', 'INCOME', 0)," +
                    "(4, 'Ăn uống', 'EXPENSE', 2000000)," +
                    "(5, 'Đi lại', 'EXPENSE', 500000)," +
                    "(6, 'Mua sắm', 'EXPENSE', 1500000)," +
                    "(7, 'Giải trí', 'EXPENSE', 1000000)," +
                    "(8, 'Chuyển khoản', 'EXPENSE', 0)," +
                    "(9, 'Nhận chuyển khoản', 'INCOME', 0);");

            // 6. Thêm dữ liệu giao dịch mẫu gán cho user_id = 1
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM [transaction]")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO [transaction] (id, user_id, title, amount, date, category_id, description, type, extra_info) VALUES " +
                            "(1, 1, 'Lương tháng 7', 15000000, '2026-07-01', 1, 'Lương nhận hàng tháng', 'INCOME', 'Công ty FPT')," +
                            "(2, 1, 'Ăn buffet tối', 600000, '2026-07-10', 4, 'Ăn liên hoan cùng gia đình', 'EXPENSE', 'Thẻ tín dụng')," +
                            "(3, 1, 'Đổ xăng xe', 120000, '2026-07-12', 5, 'Đổ xăng xe máy', 'EXPENSE', 'Tiền mặt')," +
                            "(4, 1, 'Mua sắm quần áo', 800000, '2026-07-13', 6, 'Mua áo sơ mi mới', 'EXPENSE', 'Ví Momo')," +
                            "(5, 1, 'Xem phim CGV', 180000, '2026-07-14', 7, 'Xem phim rạp cuối tuần', 'EXPENSE', 'Tiền mặt')," +
                            "(6, 1, 'Thu nhập kinh doanh phụ', 3500000, '2026-07-15', 2, 'Bán hàng online', 'INCOME', 'Cửa hàng Shopee');");
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Database initialization failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
