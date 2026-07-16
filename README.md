# Hướng Dẫn Cài Đặt & Chạy Ứng Dụng MoneyMate

Tài liệu này hướng dẫn chi tiết những gì bạn cần tải xuống và các bước thực hiện để chạy dự án từ con số 0.

---

## 1. Bạn cần TẢI NHỮNG GÌ về máy?

Để chạy dự án này, bạn **chỉ cần tải duy nhất 1 phần mềm** sau:

### 📥 Java Development Kit (JDK) 21
* **Lý do**: Đây là môi trường chạy mã nguồn Java của dự án.
* **Link tải chính thức**: [Tải JDK 21 từ Oracle](https://www.oracle.com/java/technologies/downloads/#java21) (Chọn phiên bản phù hợp với hệ điều hành của bạn, ví dụ: **Windows x64 Installer**).
* **Cài đặt**: Sau khi tải về, hãy chạy file cài đặt `.exe` và nhấn *Next* cho đến khi hoàn tất.

> **💡 Những thứ bạn KHÔNG CẦN tải/cài đặt:**
> * **KHÔNG cần cài Maven**: Dự án đã được tích hợp sẵn bộ chạy Maven trong thư mục `maven/`.
> * **KHÔNG cần cài hệ quản trị cơ sở dữ liệu** (như MySQL, SQL Server): Ứng dụng sử dụng cơ sở dữ liệu nhúng **SQLite**. File dữ liệu `money_mate.db` sẽ tự động được tạo ra ngay trong thư mục dự án khi bạn chạy phần mềm lần đầu tiên.

---

## 2. Cách chạy ứng dụng

Sau khi đã cài đặt JDK 21 ở bước trên, bạn có hai cách để khởi chạy dự án:

### Cách 1: Chạy nhanh bằng file Batch (Khuyên dùng trên Windows)
1. Truy cập vào thư mục dự án.
2. Tìm và kích đúp chuột vào tệp tin **`run.bat`**.
3. Ứng dụng sẽ tự động được biên dịch và khởi chạy giao diện lên màn hình.

### Cách 2: Chạy bằng dòng lệnh (Terminal / PowerShell)
1. Mở cửa sổ dòng lệnh (PowerShell hoặc Command Prompt) tại thư mục dự án.
2. Nhập lệnh dưới đây và nhấn Enter:
   ```bash
   .\maven\bin\mvn exec:java
   ```

---

## 3. Thư mục `target` là gì?

Thư mục **`target/`** xuất hiện trong dự án là thư mục chứa các kết quả sau khi biên dịch (build):
* **File bytecode (`.class`)**: Các file Java sau khi biên dịch sẽ nằm trong `target/classes/`.
* **File đóng gói (`.jar`)**: Khi bạn đóng gói dự án để mang sang máy khác chạy độc lập (bằng lệnh `.\maven\bin\mvn package`), file chạy `money-mate-1.0-SNAPSHOT.jar` sẽ được tạo ra tại đây.
* **Xóa an toàn**: Bạn có thể xóa thư mục `target/` bất cứ lúc nào (bằng lệnh `.\maven\bin\mvn clean`). Nó sẽ tự động được tạo lại khi bạn chạy hoặc biên dịch ứng dụng.

---

## 4. Công cụ xem Cơ sở dữ liệu (Tùy chọn)
Nếu bạn muốn mở xem trực tiếp các bảng dữ liệu của SQLite (tài khoản, giao dịch, danh mục), bạn có thể tải công cụ miễn phí sau:
* **DB Browser for SQLite**: [Tải về tại đây](https://sqlitebrowser.org/dl/)
* Mở phần mềm này lên, chọn *Open Database* và trỏ đến file `money_mate.db` trong thư mục dự án để xem dữ liệu dạng bảng.
