package moneymate.exception;

/**
 * Ngoại lệ xảy ra khi có lỗi tương tác với cơ sở dữ liệu.
 */
public class DatabaseException extends Exception {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
