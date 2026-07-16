package moneymate.repository;

import moneymate.exception.DatabaseException;
import moneymate.exception.ValidationException;
import moneymate.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository implements IRepository<Category> {

    @Override
    public List<Category> getAll() throws DatabaseException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM category ORDER BY name ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException | ValidationException e) {
            throw new DatabaseException("Lỗi khi tải danh sách danh mục.", e);
        }
        return categories;
    }

    @Override
    public Category getById(int id) throws DatabaseException {
        String sql = "SELECT * FROM category WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException | ValidationException e) {
            throw new DatabaseException("Lỗi khi tìm danh mục theo ID: " + id, e);
        }
        return null;
    }

    @Override
    public void add(Category category) throws DatabaseException {
        String sql = "INSERT INTO category (name, type, budget_limit) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getType());
            pstmt.setDouble(3, category.getBudgetLimit());
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi thêm danh mục. Tên danh mục có thể đã tồn tại.", e);
        }
    }

    @Override
    public void update(Category category) throws DatabaseException {
        String sql = "UPDATE category SET name = ?, type = ?, budget_limit = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getType());
            pstmt.setDouble(3, category.getBudgetLimit());
            pstmt.setInt(4, category.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi cập nhật danh mục: " + category.getName(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseException {
        String sql = "DELETE FROM category WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi xóa danh mục. Hãy kiểm tra các giao dịch đang liên kết.", e);
        }
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException, ValidationException {
        return new Category(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("type"),
                rs.getDouble("budget_limit")
        );
    }
}
