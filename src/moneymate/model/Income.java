package moneymate.model;

import moneymate.exception.ValidationException;

public class Income extends Transaction {
    private String source; // Nguồn thu (ví dụ: Lương, Kinh doanh, Quà tặng)

    public Income() {}

    public Income(int id, String title, double amount, String date, Category category, String description, String source) throws ValidationException {
        super(id, title, amount, date, category, description);
        setSource(source);
    }

    public String getSource() { return source; }
    public void setSource(String source) throws ValidationException {
        if (source == null || source.trim().isEmpty()) {
            throw new ValidationException("Nguồn thu nhập không được để trống.");
        }
        this.source = source.trim();
    }

    @Override
    public double getNetValue() {
        return getAmount();
    }

    @Override
    public String getType() {
        return "INCOME";
    }
}
