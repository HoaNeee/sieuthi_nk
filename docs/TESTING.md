# Kiểm thử

## Đã tự động hóa

Chạy `.\mvnw.cmd test`.

- `WebFlowTests`: trang nội bộ yêu cầu đăng nhập; STAFF bị chặn GET/POST quản trị; CSRF; login BCrypt; tài khoản khóa; validation; render Thymeleaf các trang chính; giỏ giữ nguyên khi lỗi và xóa sau thành công; HTML không lộ lỗi SQL.
- `TradingServiceTests`: đọc giá DB, khách vãng lai, khách không tồn tại, thiếu tồn, conditional update thất bại, insert chi tiết lỗi, nhập đúng/sai NCC, cấm STAFF nhập kho, giới hạn hóa đơn theo nhân viên.
- `JdbcTransactionsTests`: cùng connection, commit, rollback, reset autoCommit, giữ lỗi chính; rollback lỗi thì abort và không bật autoCommit.
- `EmployeeServiceTests`: khóa bảng trước thay đổi tài khoản, bảo vệ ADMIN cuối cùng, BCrypt khi tạo nhân viên, giới hạn byte mật khẩu.
- `CartTests`: gộp dòng, chặn số lượng không hợp lệ/tràn, snapshot độc lập và giỏ không dùng chung.
- `AssignmentJavaApplicationTests`: Spring context khởi tạo được.

Các kiểm thử sử dụng mock ở biên DB, **không xác nhận dialect Oracle, tính nguyên tử của Oracle hoặc deadlock trên dữ liệu thật**. Không dùng H2 để giả định tương đương Oracle.

## Kiểm thử Oracle thủ công sau khi cài đặt

Dùng dữ liệu bài tập riêng. Chạy 00_verify.sql và kiểm tra đúng schema trước. Không dùng SYS/SYSTEM cho ứng dụng.

| Tình huống | Thao tác | Kết quả cần đạt |
|---|---|---|
| Nhập kho | ADMIN nhập 10 đơn vị đúng NCC | Một phiếu và các chi tiết; tồn tăng đúng 10 |
| Sai NCC | Phiếu có 2 dòng, một sản phẩm thuộc NCC khác | Không tạo phiếu/chi tiết, mọi tồn giữ nguyên |
| Bán đủ | Giỏ 2 đơn vị, giá 12.35 | Tổng 24.70; tồn giảm 2; đúng nhân viên đăng nhập |
| Bán vượt | Mua 11 khi chỉ có 10 | Không có chứng từ dở dang, giỏ vẫn còn, tồn không âm |
| Khách vãng lai | Không chọn thành viên | HOA_DON.MA_KH là NULL |
| Khách thành viên | Chọn khách tồn tại | FK đúng khách, điểm giữ nguyên |
| Giá lịch sử | Bán hàng rồi ADMIN đổi giá sản phẩm | Hóa đơn cũ giữ DON_GIA cũ và tổng cũ |
| Thay giá khi đang mở giỏ | ADMIN đổi giá; STAFF thanh toán sau | Dùng giá DB ở lúc checkout |
| Hai lượt bán đồng thời | Hai trình duyệt/phiên, mỗi giỏ mua 4 khi tồn là 5 | Chỉ một lượt thành công; lượt còn lại báo thiếu; tồn cuối 1 |
| Hai sản phẩm ngược thứ tự | Hai giỏ chứa cùng 2 SP nhưng thêm theo thứ tự khác | Khóa theo MA_SP; không deadlock do thứ tự giỏ |
| Gửi lặp checkout | Bấm hai lần / hai POST cùng session | Chỉ một hóa đơn cho giỏ đó; lần sau giỏ trống |
| Gửi lặp nhập | Hai POST cùng token phiếu | Chỉ một phiếu nhập, kho chỉ tăng một lần |
| STAFF đoán ID | Mở hóa đơn nhân viên khác | 404, không lộ chi tiết |
| STAFF giả nhân viên | Thêm MA_NV/employeeId vào POST | Không thay nhân viên lập hóa đơn |
| Sửa tồn/điểm qua POST | Thêm stock/points vào form sản phẩm/khách | Repository không ghi những cột này |
| SQL injection | Tìm tên chứa dấu nháy, phần SQL như ' OR 1=1 -- | Là giá trị bind, không thay cấu trúc câu SQL |
| Khóa tài khoản | ADMIN khóa STAFF đang đăng nhập | Request tiếp theo hết phiên; không login lại được |
| ADMIN cuối cùng | Thử khóa/hạ quyền ADMIN cuối | Bị từ chối, dữ liệu không đổi |
| Hai ADMIN hạ quyền đồng thời | Hai phiên cùng hạ quyền hai ADMIN còn lại | Một ADMIN hoạt động vẫn được giữ |
| Mất kết nối | Dừng Oracle trong môi trường thử | Thông báo thân thiện, không lộ SQL/credential |

Đối soát sau thử nghiệm:

```sql
SELECT * FROM SAN_PHAM WHERE SO_LUONG_TON < 0;
SELECT MA_HD FROM HOA_DON h WHERE NOT EXISTS
  (SELECT 1 FROM CT_HOA_DON c WHERE c.MA_HD=h.MA_HD);
SELECT MA_PN FROM PHIEU_NHAP p WHERE NOT EXISTS
  (SELECT 1 FROM CT_PHIEU_NHAP c WHERE c.MA_PN=p.MA_PN);
```

Cả ba truy vấn cần không có dòng bất thường; chạy thêm truy vấn đối soát tổng tiền ở 05_reports.sql. Lỗi insert chi tiết/rollback hiện được mô phỏng tự động bằng Mockito; nếu cần kiểm thử lỗi DB thật, thực hiện trong schema thử nghiệm được chuẩn bị riêng, không sửa constraint/trigger trên dữ liệu đang sử dụng.

## Điều chưa kiểm chứng

Oracle chưa được cài/cấu hình khi triển khai lần đầu. DDL, seed, JDBC driver kết nối Oracle, cấp phát sequence, khóa đồng thời và bản cài Windows phải được chạy xác nhận sau khi môi trường sẵn sàng. Không đánh dấu các ca Oracle là đã đạt chỉ vì unit test thành công.

