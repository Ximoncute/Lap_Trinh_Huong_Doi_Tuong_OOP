package moneymate.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {
    // Định dạng tiền tệ VND chuyên nghiệp
    public static String formatVND(double amount) {
        Locale vietnam = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(vietnam);
        return currencyFormatter.format(amount);
    }
}
