# Hệ thống quản lý siêu thị nội bộ

Ứng dụng bài tập Java chạy local Windows: Java 21, Spring Boot 4.1.0, Spring MVC, Thymeleaf, Spring Security, Oracle và JDBC thuần. Không dùng Docker, ORM, JdbcTemplate hoặc frontend tách rời.

## Chức năng đã triển khai

- Đăng nhập/đăng xuất, đổi mật khẩu; BCrypt; tài khoản khóa không đăng nhập được.
- Hai vai trò ADMIN/STAFF. Phiên bị thu hồi ở yêu cầu kế tiếp khi tài khoản bị khóa, đổi quyền, đổi tên đăng nhập hoặc đổi mật khẩu.
- Catalog sản phẩm: tìm tên/barcode, lọc danh mục, phân trang 20 dòng; ADMIN thêm/sửa sản phẩm và danh mục.
- ADMIN quản lý NCC và nhân viên; khóa/mở, đổi quyền, đặt lại mật khẩu; bảo vệ ADMIN hoạt động cuối cùng bằng khóa bảng trong transaction.
- ADMIN/STAFF thêm/sửa thông tin khách hàng; chỉ xem điểm tích lũy.
- Giỏ POS riêng trong session; thêm, gộp sản phẩm trùng, sửa số lượng, xóa dòng.
- Checkout đọc lại giá/tồn, khóa sản phẩm theo thứ tự mã, trừ kho có điều kiện; hóa đơn và chi tiết dùng cùng connection/transaction.
- STAFF chỉ xem hóa đơn mình lập; ADMIN xem tất cả. Giá lịch sử lấy từ chi tiết hóa đơn.
- Nhập kho nhiều dòng, kiểm tra đúng NCC, cộng kho một lần; token form chống gửi lặp.
- Báo cáo doanh thu theo ngày, bán theo sản phẩm, tồn kho hiện tại.
- Giao diện tiếng Việt, validation, CSRF, Post/Redirect/Get và thông báo lỗi không lộ SQL.

**Trạng thái kiểm chứng:** bộ kiểm thử Java/MVC chạy không cần Oracle, dùng mock ở ranh giới JDBC/repository. Chưa chạy DDL hay kiểm thử tích hợp với Oracle thật vì máy chưa cài/cấu hình Oracle. Các kiểm thử này không thay thế kiểm thử khóa và concurrency trên Oracle.

## 1. Chuẩn bị Oracle local

1. Cài Oracle Database Free trực tiếp trên Windows cùng SQL Developer hoặc SQLcl. Dùng bộ cài phù hợp hệ điều hành của bạn. Không cần Docker.
2. Kiểm tra Listener đang chạy và PDB/service `FREEPDB1` mở. Cổng dự kiến là `1521`.
3. DBA kết nối **đúng PDB FREEPDB1**, kiểm tra user/tablespace đã tồn tại trước khi tạo. Xem `database/00_admin_setup.example.sql`.
4. Nếu chưa có: tạo tablespace `TS_SIEUTHI` (100M, tự tăng 10M, tối đa 500M), user `SIEUTHI`, temporary tablespace `TEMP`, quota trên `TS_SIEUTHI` và quyền theo `AGENTS.md`. Nhập mật khẩu bằng giao diện SQL Developer hoặc script riêng không commit.
5. Đường dẫn datafile dự kiến `C:\26AI\ORADATA\FREE\FREEPDB1\TS_SIEUTHI01.DBF` chỉ đúng nếu thư mục đó có trên máy Oracle và dịch vụ có quyền ghi. Không dùng đường dẫn này trong JDBC.
6. Tạo kết nối ứng dụng: user `sieuthi`, host `localhost`, port `1521`, **service name** `FREEPDB1`. Không dùng `TS_SIEUTHI` làm service.

Chạy `database/00_verify.sql` trong kết nối SIEUTHI. Kết quả cần là USER=SIEUTHI, PDB=FREEPDB1, tablespace=TS_SIEUTHI. Chưa tạo bảng thì danh sách bảng nghiệp vụ sẽ trống.

Không tự cấp thêm quyền khi lỗi, không xóa/tạo lại user hoặc dữ liệu để thử. DDL Oracle có implicit commit; nếu một lệnh tạo bảng lỗi thì kiểm tra đối tượng đã tạo trước khi xử lý tiếp.

## 2. Build và kiểm thử

Mở PowerShell trong thư mục dự án:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Java 21 phải có trên PATH (hoặc cấu hình JAVA_HOME). Lần đầu Maven tải dependency cần Internet. Maven Wrapper Windows đã được sửa trường hợp thư mục cache không phải symbolic link. Nếu máy có Maven thì có thể dùng `mvn` thay `.\mvnw.cmd`.

Bộ kiểm thử không cần mật khẩu DB thật và không tự tạo database/schema. Các mật khẩu chuỗi trong test chỉ là dữ liệu kiểm thử mock, không phải tài khoản sử dụng được.

## 3. Tạo bảng và tài khoản ADMIN

Kết nối SQL Developer/SQLcl bằng SIEUTHI trong FREEPDB1, chạy thủ công theo thứ tự trên **schema mới**:

1. `database/01_schema.sql`: 9 bảng, PK, 10 FK và check constraints; 9 sequence.
2. `database/02_indexes.sql`: index FK và ngày hóa đơn.
3. Tạo hash BCrypt cho mật khẩu ADMIN bằng công cụ Java dưới đây.
4. `database/03_seed.sql`: hỏi hash BCrypt dạng ẩn, tạo user ứng dụng `admin`, một danh mục, NCC và sản phẩm mẫu có tồn kho 0.

Tạo hash trong terminal có console, không đưa mật khẩu lên dòng lệnh:

```powershell
.\mvnw.cmd -q compile dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
$runtimeClasspath = Get-Content target/classpath.txt -Raw
java -cp "target/classes;$runtimeClasspath" com.nhomkha.assignment_java.config.PasswordHashTool
```

Nhập mật khẩu tối thiểu 10 ký tự, tối đa 72 byte UTF-8; công cụ in ra hash, không in mật khẩu. Dán **hash** vào prompt của seed. SQL Developer chạy bằng Run Script (F5); `ACCEPT`, `UNDEFINE`, `WHENEVER` là lệnh script SQLcl/SQL*Plus, không gửi qua JDBC.

Seed chỉ chạy một lần. Không có mật khẩu ADMIN mặc định công khai. Mật khẩu đăng nhập ứng dụng khác với mật khẩu user Oracle SIEUTHI. Hãy nhập kho bằng ứng dụng trước khi bán sản phẩm mẫu.

## 4. Cấu hình kết nối và chạy

Ưu tiên dùng file `.env` tại thư mục gốc (cùng cấp `pom.xml`). File local đã được tạo với mật khẩu để trống; khi lấy project ở máy khác, sao chép `.env.example` thành `.env`. Điền mật khẩu Oracle của user SIEUTHI:

```properties
DB_URL=jdbc:oracle:thin:@//localhost:1521/FREEPDB1
DB_USERNAME=sieuthi
DB_PASSWORD=mat_khau_oracle_cua_ban
```

Giá trị mật khẩu trên chỉ minh họa, phải thay bằng mật khẩu thật trên máy. Đây là cú pháp Java `.properties`: không bao giá trị trong dấu nháy, không dùng `export`; dấu gạch chéo ngược phải viết thành `\\`. File `.env` đã được loại khỏi Git; chỉ `.env.example` được chia sẻ. Không đặt mật khẩu thật vào `src/main/resources/application.properties`.

Ứng dụng đã có `spring.config.import=optional:file:./.env[.properties]` để đọc file này. Chạy từ thư mục gốc dự án; trong IntelliJ đặt **Working directory** là thư mục chứa `pom.xml`. Sửa file xong cần khởi động lại ứng dụng. Các giá trị này được nạp vào cấu hình Spring, không tự tạo biến môi trường Windows.

Biến môi trường trong IDE/terminal vẫn dùng được và có độ ưu tiên cao hơn file. Nếu đã đặt `DB_PASSWORD` trong IDE/terminal, hãy bỏ nó khi muốn dùng mật khẩu trong `.env`.

Chạy từ PowerShell ở thư mục gốc:

```powershell
# Sau khi đã tạo target/classpath.txt ở bước trên:
$runtimeClasspath = Get-Content target/classpath.txt -Raw
java -cp "target/classes;$runtimeClasspath" com.nhomkha.assignment_java.config.DatabaseCheckTool

.\mvnw.cmd spring-boot:run
```

Hoặc sau khi package:

```powershell
java -jar target/assignment_java-0.0.1-SNAPSHOT.jar
```

Mở [http://localhost:8080](http://localhost:8080). Đăng nhập `admin` bằng mật khẩu ứng dụng đã tự tạo.

`DatabaseCheckTool` cũng đọc `.env` (biến môi trường ưu tiên hơn), chỉ kiểm tra version/schema/tên bảng, không thay đổi dữ liệu. Spring không chạy SQL khởi tạo (`spring.sql.init.mode=never`).

## 5. Thử luồng nghiệp vụ

1. ADMIN tạo danh mục và NCC → tạo sản phẩm (tồn mặc định 0).
2. Vào Nhập kho → lập phiếu, chọn đúng NCC và sản phẩm, nhập số lượng/giá.
3. Tạo STAFF, đăng xuất và đăng nhập STAFF.
4. Tìm sản phẩm → thêm vào giỏ → chỉnh số lượng → chọn khách thành viên hoặc khách vãng lai → ghi nhận tiền mặt/chuyển khoản → lập hóa đơn.
5. Kiểm tra hóa đơn, giá lưu tại giao dịch và tồn kho; thử số lượng vượt tồn.
6. STAFF không vào được trang quản trị hoặc hóa đơn của nhân viên khác.
7. Đăng nhập ADMIN → báo cáo theo khoảng ngày.

Chi tiết kiểm thử lỗi, quyền và đồng thời: [docs/TESTING.md](docs/TESTING.md).

## Thiết kế và giới hạn MVP

- Schema nằm trong `database/`; Java model dùng class POJO với thuộc tính private, constructor không tham số/đầy đủ và getter/setter, không annotation ORM. Riêng `Cart` giữ các phương thức kiểm soát giỏ hàng; `InvoiceDetail.getAmount()` và `StockImportDetail.getAmount()` tính thành tiền từ số lượng và đơn giá.
- Controller nhận form; Service điều phối nghiệp vụ; Repository chứa PreparedStatement và map ResultSet rõ ràng.
- `JdbcTransactions` dùng chung cho các nghiệp vụ nhiều câu lệnh: manual commit/rollback, bảo toàn lỗi gốc, abort connection nếu rollback thất bại.
- Tiền dùng NUMBER(...,2)/BigDecimal; nhập tối đa 2 chữ số thập phân, không âm. Không làm tròn ngầm; tổng là tổng các dòng.
- POS gộp sản phẩm trùng, tối đa 100 sản phẩm. Phiếu nhập tối đa 100 dòng; sản phẩm trùng giữ riêng đơn giá nhập, không đặt unique constraint mới.
- Tồn kho không sửa qua form sản phẩm. Không thêm trigger cập nhật kho vì Java đã thực hiện cập nhật.
- Hạn dùng ở sản phẩm, cho phép trống, không quản lý lô; chưa có quy tắc chặn bán theo hạn dùng.
- Tiền mặt/chuyển khoản chỉ là thông tin ghi nhận, không tích hợp thanh toán thật.
- Không tự cộng/trừ/sửa điểm, không xóa cứng lịch sử/nhân viên/khách hàng. Không có hủy/hoàn, VAT, khuyến mãi.
- Danh sách sản phẩm phân trang; các danh sách quản trị và chứng từ tải toàn bộ, phù hợp dữ liệu bài tập nhỏ.
- SQL báo cáo mẫu ở `database/05_reports.sql`. Chưa thêm procedure/function/trigger khi chưa có yêu cầu môn học cụ thể.


### Model và bảng nghiệp vụ

| Model | Bảng |
|---|---|
| Category | DANH_MUC |
| Product | SAN_PHAM |
| Supplier | NHA_CUNG_CAP |
| Employee | NHAN_VIEN |
| Customer | KHACH_HANG |
| Invoice | HOA_DON |
| InvoiceDetail | CT_HOA_DON |
| StockImport | PHIEU_NHAP |
| StockImportDetail | CT_PHIEU_NHAP |

Các model hóa đơn/phiếu nhập có mã PK/FK, cùng tên nhân viên, khách hàng/NCC hoặc sản phẩm từ JOIN để hiển thị. `Invoice.customerId` có thể null cho khách vãng lai. `StockImport.total` là tổng tính từ chi tiết phiếu nhập, không phải cột mới trong database. `unitPrice` là giá bán tại giao dịch; `purchasePrice` là giá nhập. `Cart` là giỏ session; `dto.ReportRow` chứa một dòng thống kê, không tương ứng bảng riêng. `DocumentRepository` và `DocumentController` vẫn nhóm các thao tác chứng từ, nhưng dùng các kiểu dữ liệu và trang riêng cho từng nghiệp vụ.