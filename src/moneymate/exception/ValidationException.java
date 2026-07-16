package moneymate.exception;

/**
 * Ngoại lệ xảy ra khi dữ liệu đầu vào không hợp lệ trong nghiệp vụ.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
