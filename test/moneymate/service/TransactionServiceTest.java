package moneymate.service;

import moneymate.exception.ValidationException;
import moneymate.model.Category;
import moneymate.model.Income;
import moneymate.model.Expense;
import moneymate.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionServiceTest {
    private TransactionService service;
    private List<Transaction> testData;
    private Category catSalary;
    private Category catFood;

    @BeforeEach
    public void setUp() throws ValidationException {
        service = new TransactionService();
        testData = new ArrayList<>();

        catSalary = new Category(1, "Lương", "INCOME", 0);
        catFood = new Category(2, "Ăn uống", "EXPENSE", 1000000); // Hạn mức 1 triệu

        // Thêm các khoản thu / chi mẫu
        testData.add(new Income(1, "Lương tháng 7", 5000000, "2026-07-01", catSalary, "Lương chính thức", "Công ty A"));
        testData.add(new Expense(2, "Ăn trưa", 150000, "2026-07-02", catFood, "Ăn phở", "Tiền mặt"));
        testData.add(new Expense(3, "Ăn tối nhà hàng", 900000, "2026-07-03", catFood, "Ăn liên hoan", "Thẻ tín dụng"));
    }

    @Test
    public void testGetTotalIncome() {
        double totalIncome = service.getTotalIncome(testData);
        assertEquals(5000000, totalIncome, 0.001);
    }

    @Test
    public void testGetTotalExpense() {
        double totalExpense = service.getTotalExpense(testData);
        assertEquals(1050000, totalExpense, 0.001);
    }

    @Test
    public void testGetNetBalance() {
        // Đa hình: Lương = +5.000.000, Ăn trưa = -150.000, Ăn tối = -900.000. Số dư = 3.950.000
        double balance = service.getNetBalance(testData);
        assertEquals(3950000, balance, 0.001);
    }

    @Test
    public void testGetExpenseByCategory() {
        Map<String, Double> byCat = service.getExpenseByCategory(testData);
        assertEquals(1, byCat.size());
        assertEquals(1050000, byCat.get("Ăn uống"), 0.001);
    }

    @Test
    public void testGetBudgetWarnings() {
        // Tổng chi Ăn uống = 1.050.000 > Hạn mức 1.000.000 -> Phải có 1 cảnh báo
        List<String> warnings = service.getBudgetWarnings(testData, null);
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("Ăn uống"));
    }
}
