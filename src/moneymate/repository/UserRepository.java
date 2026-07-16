package moneymate.repository;

import moneymate.exception.DatabaseException;
import moneymate.exception.ValidationException;
import moneymate.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserRepository thực hiện các tác vụ lưu trữ và truy vấn thông tin người dùng trong SQLite.
 * Sử dụng Số điện thoại (phone) làm tên đăng nhập và loại bỏ hoàn toàn username.
 */
public class UserRepository {

    public User login(String phone, String rawPassword) throws DatabaseException {
        String sql = "SELECT * FROM [user] WHERE phone = ? AND password = ?";
        String hashedPassword = User.hashPassword(rawPassword);
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setString(2, hashedPassword);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("account_number"),
                            rs.getDouble("income_target"),
                            rs.getDouble("expense_limit")
                    );
                }
            }
        } catch (SQLException | ValidationException e) {
            throw new DatabaseException("Đăng nhập thất bại: " + e.getMessage(), e);
        }
        return null;
    }

    public void register(User user) throws DatabaseException {
        String sql = "INSERT INTO [user] (password, full_name, email, phone, account_number, income_target, expense_limit) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getPassword());
            pstmt.setString(2, user.getFullName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getAccountNumber());
            pstmt.setDouble(6, user.getIncomeTarget());
            pstmt.setDouble(7, user.getExpenseLimit());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Đăng ký tài khoản thất bại: " + e.getMessage(), e);
        }
    }

    public boolean phoneExists(String phone) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM [user] WHERE phone = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi kiểm tra số điện thoại tồn tại.", e);
        }
        return false;
    }

    public boolean accountNumberExists(String accountNumber) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM [user] WHERE account_number = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi kiểm tra số tài khoản tồn tại.", e);
        }
        return false;
    }

    public User getUserByAccountNumber(String accountNumber) throws DatabaseException {
        String sql = "SELECT * FROM [user] WHERE account_number = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("account_number"),
                            rs.getDouble("income_target"),
                            rs.getDouble("expense_limit")
                    );
                }
            }
        } catch (SQLException | ValidationException e) {
            throw new DatabaseException("Lỗi tìm kiếm tài khoản ngân hàng thụ hưởng.", e);
        }
        return null;
    }

    public void updateProfile(User user) throws DatabaseException {
        String sql = "UPDATE [user] SET full_name = ?, email = ?, phone = ?, account_number = ?, income_target = ?, expense_limit = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getAccountNumber());
            pstmt.setDouble(5, user.getIncomeTarget());
            pstmt.setDouble(6, user.getExpenseLimit());
            pstmt.setInt(7, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi cập nhật hồ sơ người dùng.", e);
        }
    }

    public void changePassword(int userId, String hashedNewPassword) throws DatabaseException {
        String sql = "UPDATE [user] SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashedNewPassword);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi thay đổi mật khẩu.", e);
        }
    }
}
