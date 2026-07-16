package moneymate.model;

import moneymate.exception.ValidationException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Lớp đại diện cho người dùng hệ thống (User), sử dụng số điện thoại (phone)
 * làm tên đăng nhập mặc định. Thể hiện nguyên lý đóng gói (Encapsulation).
 */
public class User {
    private int id;
    private String password; // Lưu trữ dưới dạng mã hóa SHA-256
    private String fullName;
    private String email;
    private String phone;
    private String accountNumber;
    private double incomeTarget;
    private double expenseLimit;

    public User(int id, String password, String fullName, String email, String phone, String accountNumber, double incomeTarget, double expenseLimit) throws ValidationException {
        setId(id);
        setPassword(password);
        setFullName(fullName);
        setEmail(email);
        setPhone(phone);
        setAccountNumber(accountNumber);
        setIncomeTarget(incomeTarget);
        setExpenseLimit(expenseLimit);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) throws ValidationException {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Mật khẩu không được trống.");
        }
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) throws ValidationException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Họ và tên không được trống.");
        }
        this.fullName = fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) throws ValidationException {
        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
            throw new ValidationException("Email đăng ký phải có đuôi @gmail.com (ví dụ: vidu@gmail.com).");
        }
        this.email = email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) throws ValidationException {
        if (phone == null || !phone.matches("^\\d{10,11}$")) {
            throw new ValidationException("Số điện thoại phải chứa từ 10 đến 11 ký số.");
        }
        this.phone = phone.trim();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) throws ValidationException {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new ValidationException("Số tài khoản không được trống.");
        }
        this.accountNumber = accountNumber.trim();
    }

    public double getIncomeTarget() {
        return incomeTarget;
    }

    public void setIncomeTarget(double incomeTarget) throws ValidationException {
        if (incomeTarget < 0) {
            throw new ValidationException("Mục tiêu thu nhập phải là số không âm.");
        }
        this.incomeTarget = incomeTarget;
    }

    public double getExpenseLimit() {
        return expenseLimit;
    }

    public void setExpenseLimit(double expenseLimit) throws ValidationException {
        if (expenseLimit < 0) {
            throw new ValidationException("Hạn mức chi tiêu phải là số không âm.");
        }
        this.expenseLimit = expenseLimit;
    }

    /**
     * Tiện ích băm mật khẩu SHA-256.
     */
    public static String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(rawPassword.getBytes());
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi mã hóa SHA-256", e);
        }
    }
}
