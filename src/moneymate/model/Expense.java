package moneymate.model;

import moneymate.exception.ValidationException;

public class Expense extends Transaction {
    private String paymentMethod; // Phương thức thanh toán (ví dụ: Tiền mặt, Thẻ tín dụng, Ví điện tử)

    public Expense() {}

    public Expense(int id, String title, double amount, String date, Category category, String description, String paymentMethod) throws ValidationException {
        super(id, title, amount, date, category, description);
        setPaymentMethod(paymentMethod);
    }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) throws ValidationException {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new ValidationException("Phương thức thanh toán không được để trống.");
        }
        this.paymentMethod = paymentMethod.trim();
    }

    @Override
    public double getNetValue() {
        return -getAmount();
    }

    @Override
    public String getType() {
        return "EXPENSE";
    }
}
