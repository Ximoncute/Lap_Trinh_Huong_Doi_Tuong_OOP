package moneymate.model;

import moneymate.exception.ValidationException;

public class Category {
    private int id;
    private String name;
    private String type; // "INCOME" hoặc "EXPENSE"
    private double budgetLimit; // Hạn mức ngân sách (chỉ áp dụng cho EXPENSE)

    public Category() {}

    public Category(int id, String name, String type, double budgetLimit) throws ValidationException {
        this.id = id;
        setName(name);
        setType(type);
        setBudgetLimit(budgetLimit);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Tên danh mục không được để trống.");
        }
        this.name = name.trim();
    }

    public String getType() { return type; }
    public void setType(String type) throws ValidationException {
        if (type == null || (!type.equals("INCOME") && !type.equals("EXPENSE"))) {
            throw new ValidationException("Loại danh mục phải là INCOME hoặc EXPENSE.");
        }
        this.type = type;
    }

    public double getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(double budgetLimit) throws ValidationException {
        if (budgetLimit < 0) {
            throw new ValidationException("Hạn mức ngân sách không được nhỏ hơn 0.");
        }
        this.budgetLimit = budgetLimit;
    }

    @Override
    public String toString() { return name; }
}
