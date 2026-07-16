package moneymate.util;

import moneymate.exception.ValidationException;
import moneymate.factory.TransactionFactory;
import moneymate.model.Category;
import moneymate.model.Transaction;
import moneymate.service.CategoryService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVUtil {

    private static final String CSV_HEADER = "id,type,title,amount,date,categoryName,description,extraInfo";

    // Nghiệp vụ: Xuất danh sách giao dịch ra file CSV
    public static void exportToCSV(List<Transaction> transactions, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.println(CSV_HEADER);
            for (Transaction t : transactions) {
                String extraInfo = t instanceof moneymate.model.Income ? 
                        ((moneymate.model.Income) t).getSource() : 
                        ((moneymate.model.Expense) t).getPaymentMethod();
                
                writer.println(String.format("%d,%s,%s,%.2f,%s,%s,%s,%s",
                        t.getId(),
                        t.getType(),
                        escapeSpecialCharacters(t.getTitle()),
                        t.getAmount(),
                        t.getDate(),
                        escapeSpecialCharacters(t.getCategory().getName()),
                        escapeSpecialCharacters(t.getDescription()),
                        escapeSpecialCharacters(extraInfo)
                ));
            }
        }
    }

    // Nghiệp vụ: Nhập danh sách giao dịch từ file CSV
    public static List<Transaction> importFromCSV(File file, CategoryService categoryService) 
            throws IOException, ValidationException, moneymate.exception.DatabaseException {
        List<Transaction> transactions = new ArrayList<>();
        List<Category> allCategories = categoryService.getAllCategories();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Đọc dòng tiêu đề (header)
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",", -1);
                if (data.length < 8) continue;

                String type = data[1].trim();
                String title = data[2].trim();
                double amount = Double.parseDouble(data[3].trim());
                String date = data[4].trim();
                String catName = data[5].trim();
                String desc = data[6].trim();
                String extraInfo = data[7].trim();

                // Tìm kiếm hoặc tạo mới danh mục phù hợp
                Category category = allCategories.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(catName))
                        .findFirst()
                        .orElse(null);

                if (category == null) {
                    category = new Category(0, catName, type, 0.0);
                    categoryService.addCategory(category);
                    allCategories.add(category); // lưu vào cache tạm thời để tránh insert trùng
                }

                Transaction transaction = TransactionFactory.createTransaction(
                        type, 0, title, amount, date, category, desc, extraInfo);
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    private static String escapeSpecialCharacters(String data) {
        if (data == null) return "";
        return data.replace(",", " "); // Loại bỏ dấu phẩy để tránh làm hỏng cấu trúc CSV đơn giản
    }
}
