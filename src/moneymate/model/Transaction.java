package moneymate.model;

import moneymate.exception.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public abstract class Transaction {
    private int id;
    private String title;
    private double amount;
    private String date; // định dạng YYYY-MM-DD
    private Category category;
    private String description;

    public Transaction() {}

    public Transaction(int id, String title, double amount, String date, Category category, String description) throws ValidationException {
        this.id = id;
        setTitle(title);
        setAmount(amount);
        setDate(date);
        setCategory(category);
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Tiêu đề giao dịch không được để trống.");
        }
        this.title = title.trim();
    }

    public double getAmount() { return amount; }
    public void setAmount(double amount) throws ValidationException {
        if (amount <= 0) {
            throw new ValidationException("Số tiền giao dịch phải lớn hơn 0.");
        }
        this.amount = amount;
    }

    public String getDate() { return date; }
    public void setDate(String date) throws ValidationException {
        try {
            LocalDate.parse(date); // Kiểm tra định dạng ngày yyyy-MM-dd
            this.date = date;
        } catch (DateTimeParseException e) {
            throw new ValidationException("Ngày tháng không hợp lệ (Định dạng đúng: YYYY-MM-DD).");
        }
    }

    public Category getCategory() { return category; }
    public void setCategory(Category category) throws ValidationException {
        if (category == null) {
            throw new ValidationException("Danh mục không được để trống.");
        }
        this.category = category;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Phương thức đa hình trừu tượng để lấy giá trị thực tế (+ đối với thu, - đối với chi)
    public abstract double getNetValue();

    // Phương thức lấy loại giao dịch (INCOME hoặc EXPENSE)
    public abstract String getType();
}
