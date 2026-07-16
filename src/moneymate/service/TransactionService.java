package moneymate.service;

import moneymate.exception.DatabaseException;
import moneymate.model.Category;
import moneymate.model.Transaction;
import moneymate.model.User;
import moneymate.repository.TransactionRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lớp dịch vụ quản lý các nghiệp vụ tính toán tài chính liên quan tới Transaction.
 * Đã tích hợp user_id để tính toán chính xác số liệu cho người dùng hiện tại.
 */
public class TransactionService {
    private final TransactionRepository repository;

    public TransactionService() {
        this.repository = new TransactionRepository();
    }

    public List<Transaction> getAllTransactions(int userId) throws DatabaseException {
        return repository.getAllByUserId(userId);
    }

    public void addTransaction(Transaction transaction, int userId) throws DatabaseException {
        repository.addWithUserId(transaction, userId);
    }

    public void updateTransaction(Transaction transaction) throws DatabaseException {
        repository.update(transaction);
    }

    public void deleteTransaction(int id) throws DatabaseException {
        repository.delete(id);
    }

    // Nghiệp vụ: Tính tổng Thu nhập
    public double getTotalIncome(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Nghiệp vụ: Tính tổng Chi tiêu
    public double getTotalExpense(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Nghiệp vụ: Tính Số dư ròng
    public double getNetBalance(List<Transaction> transactions) {
        return transactions.stream()
                .mapToDouble(Transaction::getNetValue)
                .sum();
    }

    // Nghiệp vụ: Thống kê chi tiêu theo Danh mục
    public Map<String, Double> getExpenseByCategory(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    // Nghiệp vụ: Thống kê dòng tiền theo tháng động
    public Map<String, double[]> getMonthlyCashFlow(List<Transaction> transactions) {
        Map<String, double[]> cashFlow = new TreeMap<>();
        transactions.forEach(t -> {
            LocalDate date = LocalDate.parse(t.getDate());
            String yearMonth = String.format("%04d-%02d", date.getYear(), date.getMonthValue());
            cashFlow.putIfAbsent(yearMonth, new double[]{0.0, 0.0});
            double[] values = cashFlow.get(yearMonth);
            if ("INCOME".equalsIgnoreCase(t.getType())) {
                values[0] += t.getAmount();
            } else {
                values[1] += t.getAmount();
            }
        });
        return cashFlow;
    }

    // Nghiệp vụ: Kiểm tra cảnh báo vượt hạn mức chi tiêu trong tháng hiện tại
    public List<String> getBudgetWarnings(List<Transaction> transactions, User user) {
        List<String> warnings = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // 1. Kiểm tra tổng chi tiêu tháng hiện tại so với hạn mức trong User
        double totalMonthlyExpense = transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .filter(t -> {
                    LocalDate date = LocalDate.parse(t.getDate());
                    return date.getMonthValue() == currentMonth && date.getYear() == currentYear;
                })
                .mapToDouble(Transaction::getAmount)
                .sum();

        if (user != null && user.getExpenseLimit() > 0 && totalMonthlyExpense > user.getExpenseLimit()) {
            warnings.add(String.format("Cảnh báo: Tổng chi tiêu tháng này (%s) đã vượt hạn mức ví cá nhân (%s)!",
                    moneymate.util.CurrencyUtil.formatVND(totalMonthlyExpense),
                    moneymate.util.CurrencyUtil.formatVND(user.getExpenseLimit())));
        }

        // 2. Kiểm tra hạn mức của từng danh mục
        Map<Category, Double> monthlyExpenseMap = transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .filter(t -> {
                    LocalDate date = LocalDate.parse(t.getDate());
                    return date.getMonthValue() == currentMonth && date.getYear() == currentYear;
                })
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        monthlyExpenseMap.forEach((category, totalExpense) -> {
            if (category.getBudgetLimit() > 0 && totalExpense > category.getBudgetLimit()) {
                warnings.add(String.format("Cảnh báo danh mục: '%s' chi tiêu vượt định mức! (Đã dùng %s/%s)",
                        category.getName(), 
                        moneymate.util.CurrencyUtil.formatVND(totalExpense), 
                        moneymate.util.CurrencyUtil.formatVND(category.getBudgetLimit())));
            }
        });

        return warnings;
    }
}
