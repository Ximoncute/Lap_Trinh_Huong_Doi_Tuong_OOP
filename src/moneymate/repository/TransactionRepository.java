package moneymate.repository;

import moneymate.exception.DatabaseException;
import moneymate.exception.ValidationException;
import moneymate.factory.TransactionFactory;
import moneymate.model.Category;
import moneymate.model.Income;
import moneymate.model.Expense;
import moneymate.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Repository quản lý thao tác CRUD với bảng transaction trong CSDL SQLite.
 * Đã nâng cấp để phân tách dữ liệu giao dịch theo từng user_id riêng biệt.
 */
public class TransactionRepository implements IRepository<Transaction> {

    @Override
    public List<Transaction> getAll() throws DatabaseException {
        throw new UnsupportedOperationException("Vui lòng sử dụng getAllByUserId(int userId) để lọc theo người dùng.");
    }

    @Override
    public void add(Transaction entity) throws DatabaseException {
        throw new UnsupportedOperationException("Vui lòng sử dụng addWithUserId(Transaction entity, int userId) để liên kết người dùng.");
    }

    /**
     * Lấy toàn bộ danh sách giao dịch của một tài khoản cụ thể.
     */
    public List<Transaction> getAllByUserId(int userId) throws DatabaseException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, c.name AS cat_name, c.type AS cat_type, c.budget_limit AS cat_limit " +
                     "FROM [transaction] t " +
                     "JOIN category c ON t.category_id = c.id " +
                     "WHERE t.user_id = ? " +
                     "ORDER BY t.date DESC, t.id DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException | ValidationException e) {
            throw new DatabaseException("Lỗi khi tải danh sách giao dịch của người dùng ID: " + userId, e);
        }
        return list;
    }

    @Override
    public Transaction getById(int id) throws DatabaseException {
        String sql = "SELECT t.*, c.name AS cat_name, c.type AS cat_type, c.budget_limit AS cat_limit " +
                     "FROM [transaction] t " +
                     "JOIN category c ON t.category_id = c.id " +
                     "WHERE t.id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
        } catch (SQLException | ValidationException e) {
            throw new DatabaseException("Lỗi khi tải giao dịch theo ID: " + id, e);
        }
        return null;
    }

    /**
     * Thêm mới giao dịch liên kết với tài khoản người dùng đang đăng nhập.
     */
    public void addWithUserId(Transaction transaction, int userId) throws DatabaseException {
        String sql = "INSERT INTO [transaction] (user_id, title, amount, date, category_id, description, type, extra_info) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, transaction.getTitle());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getDate());
            pstmt.setInt(5, transaction.getCategory().getId());
            pstmt.setString(6, transaction.getDescription());
            pstmt.setString(7, transaction.getType());
            
            String extraInfo = "";
            if (transaction instanceof Income) {
                extraInfo = ((Income) transaction).getSource();
            } else if (transaction instanceof Expense) {
                extraInfo = ((Expense) transaction).getPaymentMethod();
            }
            pstmt.setString(8, extraInfo);
            
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi thêm mới giao dịch.", e);
        }
    }

    @Override
    public void update(Transaction transaction) throws DatabaseException {
        String sql = "UPDATE [transaction] SET title = ?, amount = ?, date = ?, category_id = ?, " +
                     "description = ?, type = ?, extra_info = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getTitle());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDate());
            pstmt.setInt(4, transaction.getCategory().getId());
            pstmt.setString(5, transaction.getDescription());
            pstmt.setString(6, transaction.getType());
            
            String extraInfo = "";
            if (transaction instanceof Income) {
                extraInfo = ((Income) transaction).getSource();
            } else if (transaction instanceof Expense) {
                extraInfo = ((Expense) transaction).getPaymentMethod();
            }
            pstmt.setString(7, extraInfo);
            pstmt.setInt(8, transaction.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi cập nhật giao dịch ID: " + transaction.getId(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseException {
        String sql = "DELETE FROM [transaction] WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi xóa giao dịch ID: " + id, e);
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException, ValidationException {
        Category category = new Category(
                rs.getInt("category_id"),
                rs.getString("cat_name"),
                rs.getString("cat_type"),
                rs.getDouble("cat_limit")
        );
        
        return TransactionFactory.createTransaction(
                rs.getString("type"),
                rs.getInt("id"),
                rs.getString("title"),
                rs.getDouble("amount"),
                rs.getString("date"),
                category,
                rs.getString("description"),
                rs.getString("extra_info")
        );
    }
}
