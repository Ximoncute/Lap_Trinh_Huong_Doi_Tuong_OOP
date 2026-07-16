package moneymate.factory;

import moneymate.exception.ValidationException;
import moneymate.model.Category;
import moneymate.model.Expense;
import moneymate.model.Income;
import moneymate.model.Transaction;

/**
 * Factory Pattern để khởi tạo động đối tượng Income hoặc Expense
 * dựa trên loại giao dịch (INCOME hoặc EXPENSE).
 */
public class TransactionFactory {
    
    public static Transaction createTransaction(String type, int id, String title, double amount, 
                                                String date, Category category, String description, 
                                                String extraInfo) throws ValidationException {
        if ("INCOME".equalsIgnoreCase(type)) {
            return new Income(id, title, amount, date, category, description, extraInfo);
        } else if ("EXPENSE".equalsIgnoreCase(type)) {
            return new Expense(id, title, amount, date, category, description, extraInfo);
        } else {
            throw new ValidationException("Loại giao dịch không hợp lệ để khởi tạo: " + type);
        }
    }
}
