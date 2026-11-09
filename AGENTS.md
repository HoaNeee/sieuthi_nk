# AGENTS.md — Hướng dẫn dành cho Codex

> Dự án bài tập lớn: **Hệ thống quản lý siêu thị nội bộ**.  
> Tài liệu này là chỉ dẫn triển khai và quy ước kiến trúc cho Codex. Hãy đọc trước khi thay đổi mã nguồn. **Quyết định thiết kế đã chốt:** giữ ERD 9 bảng đơn giản, POS nội bộ, hai vai trò `ADMIN`/`STAFF`, JDBC thuần, **chạy local trên Windows, không sử dụng Docker**.

## 1. Mục tiêu và phạm vi

Xây dựng ứng dụng **quản lý siêu thị dành cho nhân viên**, phục vụ một bài tập lớn Java kết hợp kiến thức Oracle Database. Giao diện được render trên server bằng **Spring MVC + Thymeleaf**. Nhân viên đã đăng nhập có thể xem/tìm kiếm sản phẩm, chọn sản phẩm vào giỏ tạm và lập hóa đơn theo mô hình **POS (Point of Sale)**.

### Phạm vi chức năng dự kiến

- Đăng nhập/đăng xuất nhân viên, phân quyền theo vai trò.
- Quản lý danh mục và sản phẩm; xem danh sách, tìm kiếm, lọc sản phẩm.
- Quản lý nhà cung cấp và nghiệp vụ nhập kho (phiếu nhập + chi tiết phiếu nhập).
- Bán hàng tại quầy: chọn sản phẩm từ danh sách, chỉnh số lượng, lập hóa đơn, trừ tồn kho.
- Quản lý hồ sơ khách hàng thành viên; hiển thị điểm tích lũy. **Chưa tự tính/cộng/trừ điểm** cho đến khi có quy tắc chính thức.
- Xem lịch sử hóa đơn/phiếu nhập và các báo cáo thống kê cơ bản bằng SQL Oracle.

### Không thuộc phạm vi hiện tại

- Không xây dựng website thương mại điện tử công khai; khách hàng **không** tự đặt hàng online.
- Không cần tích hợp máy quét mã vạch hoặc phần cứng POS thực tế. `BARCODE` phục vụ tra cứu và khả năng mở rộng sau này.
- Không tích hợp cổng thanh toán thật, giao hàng hoặc vận chuyển.
- Không tạo bảng `GIO_HANG`/`DON_HANG` chỉ để mô phỏng giỏ POS hiện tại.
- Không tự thêm microservices, message broker, SPA hoặc các tầng phức tạp không có yêu cầu.
- **Không sử dụng Docker, Docker Compose hay tạo Dockerfile**; dự án chỉ chạy local để phục vụ học tập, không cần triển khai server/cloud trong phạm vi hiện tại.

## 2. Công nghệ và ràng buộc bắt buộc

| Thành phần | Lựa chọn |
|---|---|
| Ngôn ngữ | Java (theo version dự án thực tế) |
| Backend web | Spring Boot + Spring MVC |
| Giao diện | Thymeleaf, HTML, CSS, JavaScript đơn giản |
| Database | Oracle Database |
| Truy cập dữ liệu | **JDBC thuần:** `DataSource`, `Connection`, `PreparedStatement`, `ResultSet`, `CallableStatement` khi cần |
| Transaction nghiệp vụ | **JDBC thủ công:** `setAutoCommit(false)`, `commit()`, `rollback()` |
| Connection pool | Spring-managed `DataSource` (ví dụ HikariCP do Spring Boot cấu hình) |
| Validation | Jakarta Bean Validation / kiểm tra nghiệp vụ trong Service |
| Xác thực/phân quyền | Ưu tiên Spring Security nếu phù hợp project; bắt buộc kiểm tra quyền ở server |

**KHÔNG dùng:** JPA, Hibernate, Spring Data JPA, `JpaRepository`, `EntityManager`, các ORM khác, `JdbcTemplate`, `NamedParameterJdbcTemplate`, MyBatis. Không chuyển ứng dụng sang kiến trúc REST API + frontend tách rời. **Không Docker/Compose/triển khai cloud** khi chưa có yêu cầu mới.

Các class model là POJO Java thông thường, **không** gắn `@Entity`, `@Table`, `@Column`.

> Spring được phép quản lý Dependency Injection, `DataSource`, routing MVC, validation, security và render view. "JDBC thuần" áp dụng cho phần thao tác dữ liệu, không yêu cầu bỏ Spring Boot hoặc tự viết connection pool.

### 2.1. Môi trường phát triển local và cấu hình Oracle dự kiến

> **Trạng thái: dự kiến, chưa xác nhận đã chạy thành công.** Đây là cấu hình Oracle người thực hiện đã cung cấp và nhiều khả năng sẽ giữ nguyên. Codex phải dùng nó làm **giả định mặc định**, nhưng kiểm tra thực tế trước khi kết nối hoặc chạy SQL; **không tự tạo lại user/tablespace**.

| Mục | Giá trị dự kiến / quy tắc |
|---|---|
| Oracle service name (PDB) | `FREEPDB1` |
| Tài khoản/schema ứng dụng | `sieuthi` (Oracle mặc định lưu tên định danh không đặt dấu nháy thành `SIEUTHI`) |
| Default tablespace của schema | `TS_SIEUTHI` |
| Temporary tablespace | `TEMP` |
| Datafile (môi trường Oracle trên Windows hiện dự kiến) | `C:\26AI\ORADATA\FREE\FREEPDB1\TS_SIEUTHI01.DBF` |
| Kích thước dữ liệu | Ban đầu `100M`, `AUTOEXTEND ON NEXT 10M MAXSIZE 500M` |
| Quota của user | `UNLIMITED ON TS_SIEUTHI` |
| Quyền dự kiến | `CONNECT`, `RESOURCE`, `CREATE VIEW`, `CREATE TRIGGER`, `CREATE PROCEDURE`, `CREATE SEQUENCE` |
| Host/port JDBC | Mặc định **chỉ để phát triển local**: `localhost:1521`; phải xác nhận theo Oracle đang chạy |
| JDBC URL theo service | `jdbc:oracle:thin:@//localhost:1521/FREEPDB1` |

**Phân biệt các khái niệm:** `FREEPDB1` là **service/PDB** dùng trong JDBC URL; `SIEUTHI` là **user/schema** chứa 9 bảng; `TS_SIEUTHI` là **tablespace** lưu dữ liệu. **Không** dùng `TS_SIEUTHI` thay cho service name, và **không** đưa đường dẫn `.DBF` vào cấu hình JDBC.

- Script quản trị tạo tablespace/user cần được **người thực hiện chạy thủ công bằng tài khoản có quyền** trong **đúng PDB `FREEPDB1`**, trước khi tạo bảng nghiệp vụ. Việc đặt datafile ở đường dẫn Windows chỉ phù hợp khi Oracle server có ổ đĩa/thư mục đó và đủ quyền ghi; cấu hình hiện tại dành cho **Oracle cài và chạy local trên Windows**, không phải container. Chỉ thay đổi đường dẫn nếu người thực hiện thay đổi cách cài đặt Oracle sau này.
- Đường dẫn datafile, tên PDB và dung lượng là **cấu hình môi trường**, không phải tên bảng/cột trong Java; không hardcode đường dẫn Windows vào source Java hoặc `application.yml`.
- Không dùng `SYS`/`SYSTEM` làm tài khoản chạy ứng dụng Spring. Tạo bảng và chạy CRUD trong schema `SIEUTHI`; kết nối quản trị (nếu cần) tách riêng.
- Script SQL đã cung cấp gồm `COLUMN FILE_NAME FORMAT A60` và `EXIT` — đây là các lệnh **SQL*Plus/SQLcl**, không phải SQL DDL; trong SQL Developer cần dùng chế độ chạy script thích hợp. Truy vấn `DBA_DATA_FILES` thường đòi quyền tra cứu quản trị, không dùng từ tài khoản ứng dụng nếu không được cấp quyền.
- `CONNECT`/`RESOURCE` là role theo mẫu bài học, có thể rộng hơn quyền tối thiểu. **Không tự nới quyền** khi lỗi; kiểm tra nguyên nhân và xác nhận với người thực hiện trước khi sửa grant/quota.

#### Cách chạy ứng dụng (không dùng Docker)

- Chạy Oracle Database Free đã cài trực tiếp trên Windows, sử dụng service/PDB `FREEPDB1`, schema `SIEUTHI` và tablespace `TS_SIEUTHI` sau khi các bước quản trị được thực hiện thủ công.
- Mở dự án Spring Boot bằng IDE (ví dụ IntelliJ IDEA) hoặc chạy Maven/Gradle của dự án. Spring Boot kết nối Oracle qua JDBC Thin driver tới `localhost:1521` theo cấu hình môi trường.
- Dùng SQL Developer/SQLcl để tạo tablespace, user, chạy `database/*.sql` và kiểm tra dữ liệu. Các script này **không tự động thực thi khi Spring Boot khởi động**.
- Codex **không tạo** `Dockerfile`, `compose.yml`, scripts `docker run`, cấu hình deploy hoặc cloud infrastructure, trừ khi người dùng yêu cầu riêng về sau.

#### Cấu hình Spring Boot cần dùng

Không nhúng mật khẩu thực vào `AGENTS.md`, SQL script được commit, hoặc `application.yml`. Mật khẩu người thực hiện đã cung cấp chỉ dùng để cấu hình **biến môi trường ở máy cá nhân**, không sao chép vào repo; nếu thông tin đó từng xuất hiện ở chỗ công khai, nên thay mật khẩu trước khi triển khai.

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:oracle:thin:@//localhost:1521/FREEPDB1}
    username: ${DB_USERNAME:sieuthi}
    password: ${DB_PASSWORD}
    driver-class-name: oracle.jdbc.OracleDriver
  sql:
    init:
      mode: never
```

- `DB_PASSWORD` phải được cấp **từ bên ngoài source code**, ví dụ cấu hình Environment Variables trong IDE / terminal; không commit file chứa mật khẩu. Nếu dùng `.env` cục bộ thì thêm vào `.gitignore` và bảo đảm cơ chế chạy ứng dụng thực sự đọc các biến đó (Spring Boot không mặc nhiên tự đọc mọi file `.env`).
- Dùng Oracle JDBC driver (`ojdbc11` hoặc bản tương thích với Java/Oracle thực tế), `spring-boot-starter-jdbc` để có `DataSource`; **không sử dụng `JdbcTemplate`**.
- Không thêm `spring.jpa.hibernate.ddl-auto`, vì dự án **không dùng Hibernate/JPA**. Không cấu hình tự chạy `schema.sql`/`data.sql` ở lúc khởi động; `spring.sql.init.mode=never`.
- **Nguồn sự thật về schema** là các script SQL Oracle trong `database/`, chạy **thủ công và có kiểm soát**, không phải Java annotation. Thay đổi bảng phải viết script `ALTER TABLE`/migration tương ứng; ứng dụng chỉ chạy SQL DML/truy vấn và gọi PL/SQL nghiệp vụ khi cần.

#### Kiểm tra kết nối trước khi code CRUD

Sau khi người thực hiện đã tạo user/tablespace và chạy DDL, Codex nên hướng dẫn kiểm tra lần lượt:

```sql
SELECT USER FROM DUAL;
SELECT SYS_CONTEXT('USERENV', 'CON_NAME') AS PDB_NAME FROM DUAL;
SELECT DEFAULT_TABLESPACE FROM USER_USERS;
SELECT TABLE_NAME FROM USER_TABLES ORDER BY TABLE_NAME;
```

Kỳ vọng: `USER = SIEUTHI`, `PDB_NAME = FREEPDB1`, `DEFAULT_TABLESPACE = TS_SIEUTHI`; danh sách bảng nghiệp vụ hiện ra **sau khi** DDL tạo 9 bảng được chạy thành công. Nếu kết nối lỗi, kiểm tra Oracle Listener/service, host/port, PDB, credential và trạng thái user trước; **không xóa/tạo lại dữ liệu để thử**.


## 3. Quy tắc làm việc của Codex

1. **Đọc và khảo sát repo trước khi sửa:** kiểm tra cấu trúc hiện có, `pom.xml`/`build.gradle`, phiên bản Java, cấu hình Oracle, SQL scripts và conventions đang được dùng.
2. **Tôn trọng các quyết định đã chốt** ở mục 5 và 9; không coi chúng là đề xuất mở. Chỉ các chi tiết ở mục 14 còn cần xác nhận. Khi chưa thể hỏi, chọn cách ít thay đổi ERD nhất và ghi lại giả định.
3. Chia thay đổi thành bước nhỏ, chạy được và dễ kiểm thử. Ưu tiên hoàn thành trọn một luồng chức năng trước khi nhân rộng.
4. Giữ Controller mỏng; không nhúng SQL trong Controller hoặc template; nghiệp vụ nằm ở Service, SQL nằm ở Repository/DAO.
5. Không tạo `BaseRepository<T>`, generic DAO, reflection mapper, framework tự viết hoặc utils trừ khi có lặp lại thực sự và lợi ích rõ ràng.
6. Không tự đổi tên bảng/cột, bỏ quan hệ, hoặc thêm nhiều bảng mới khi chưa được yêu cầu. Nếu phát hiện bất hợp lý, nêu đề xuất và tác động trước.
7. Khi viết SQL, sử dụng **đúng dialect Oracle**, tên bảng/cột thống nhất với script DDL, và cấu hình môi trường Oracle dự kiến ở mục 2.1.
8. Với mỗi tính năng, chú ý cả happy path, dữ liệu không hợp lệ, phân quyền, điều kiện cạnh tranh và thông báo lỗi cho người dùng.
9. Không hardcode tài khoản/mật khẩu DB hoặc khóa bí mật; không commit credential thật.
10. Nếu có code đang tồn tại, ưu tiên chỉnh sửa tối thiểu, tránh thay thế hàng loạt ngoài phạm vi nhiệm vụ.

## 4. Kiến trúc đề xuất

```text
src/main/java/<base-package>/
├── config/            # Cấu hình Web, DataSource, Security nếu có
├── controller/        # MVC Controller: trả về tên Thymeleaf template / redirect
├── service/           # Xử lý nghiệp vụ, transaction JDBC
├── repository/        # SQL và xử lý JDBC thuần
├── model/             # POJO mô tả dữ liệu nghiệp vụ
├── dto/               # Form DTO, DTO dữ liệu hiển thị / tìm kiếm
├── exception/         # Exception nghiệp vụ, xử lý lỗi thân thiện
└── ...Application.java

src/main/resources/
├── templates/
│   ├── fragments/     # layout, header, sidebar, messages
│   ├── auth/
│   ├── products/
│   ├── categories/
│   ├── suppliers/
│   ├── imports/
│   ├── pos/
│   ├── invoices/
│   └── reports/
├── static/
│   ├── css/
│   └── js/
└── application.yml

database/
├── 00_admin_setup.example.sql   # tùy chọn: mẫu chạy thủ công bởi DBA, tuyệt đối không chứa mật khẩu thật
├── 01_schema.sql
├── 02_constraints.sql
├── 03_seed.sql
├── 04_procedures.sql     # chỉ nếu được triển khai
└── 05_reports.sql        # truy vấn báo cáo mẫu
```

Đây là gợi ý tổ chức, **không bắt buộc tái cấu trúc repo có sẵn**. Tên package và công cụ build theo project thực tế.

### Trách nhiệm từng tầng

- **Controller:** `@GetMapping`/`@PostMapping`, nhận `@ModelAttribute`, `BindingResult`, `Model`, chuyển hướng bằng `redirect:`; tuyệt đối không viết SQL.
- **Service:** xử lý quy tắc nghiệp vụ, kiểm tra đầu vào, điều phối nhiều Repository, chịu trách nhiệm transaction.
- **Repository/DAO:** chứa câu SQL, bind tham số, map `ResultSet`, thao tác `PreparedStatement`/`CallableStatement`.
- **Model:** các đối tượng Java độc lập với ORM.
- **DTO/Form:** nhận input từ HTML form; không dùng trực tiếp entity-like model cho mọi thao tác.
- **View:** Thymeleaf; tái sử dụng fragments và hiển thị validation/message rõ ràng.

## 5. ERD hiện tại — 9 bảng (theo sơ đồ được cung cấp)

**Quan trọng:** Sơ đồ gốc hiển thị khóa chính, các quan hệ 1–N, nhưng chưa liệt kê cột khóa ngoại. Khi triển khai CSDL quan hệ, phải **bổ sung các FK dưới đây** vào DDL và model. Chưa ấn định kiểu dữ liệu, `NOT NULL` hoặc cơ chế sinh khóa chính nếu chưa có DDL chốt.

### 5.1. Bảng, cột và ý nghĩa

| Bảng | Cột hiện có trong ERD | Vai trò |
|---|---|---|
| `DANH_MUC` | `MA_DM` (PK), `TEN_DM`, `MO_TA` | Phân loại sản phẩm |
| `SAN_PHAM` | `MA_SP` (PK), `BARCODE`, `TEN_SP`, `GIA_BAN`, `SO_LUONG_TON`, `HAN_SU_DUNG` | Hàng hóa, giá bán, tồn kho hiện tại |
| `NHA_CUNG_CAP` | `MA_NCC` (PK), `TEN_NCC`, `SDT`, `DIA_CHI`, `EMAIL` | Thông tin nhà cung cấp |
| `PHIEU_NHAP` | `MA_PN` (PK), `NGAY_NHAP` | Đầu phiếu nhập |
| `CT_PHIEU_NHAP` | `MA_CTPN` (PK), `SO_LUONG`, `DON_GIA_NHAP` | Các dòng sản phẩm được nhập |
| `NHAN_VIEN` | `MA_NV` (PK), `HO_TEN`, `CHUC_VU`, `TEN_DANG_NHAP`, `MAT_KHAU_HASH`, `VAI_TRO`, `TRANG_THAI` | Nhân viên, đăng nhập và phân quyền (`MAT_KHAU_HASH`, `TRANG_THAI` mới bổ sung) |
| `HOA_DON` | `MA_HD` (PK), `NGAY_LAP`, `HINH_THUC_TT`, `TONG_TIEN` | Đầu hóa đơn bán hàng; `MA_KH` là FK **nullable** cho khách vãng lai |
| `CT_HOA_DON` | `MA_CT` (PK), `SO_LUONG`, `DON_GIA` | Các dòng hàng của hóa đơn |
| `KHACH_HANG` | `MA_KH` (PK), `HO_TEN`, `SDT`, `DIEM_TICH_LUY` | Thông tin khách hàng thành viên |

### 5.2. Các FK phải bổ sung theo quan hệ ERD

| Bảng con | FK đề xuất | Tham chiếu |
|---|---|---|
| `SAN_PHAM` | `MA_DM` | `DANH_MUC(MA_DM)` |
| `SAN_PHAM` | `MA_NCC` | `NHA_CUNG_CAP(MA_NCC)` |
| `PHIEU_NHAP` | `MA_NCC` | `NHA_CUNG_CAP(MA_NCC)` |
| `PHIEU_NHAP` | `MA_NV` | `NHAN_VIEN(MA_NV)` |
| `CT_PHIEU_NHAP` | `MA_PN` | `PHIEU_NHAP(MA_PN)` |
| `CT_PHIEU_NHAP` | `MA_SP` | `SAN_PHAM(MA_SP)` |
| `HOA_DON` | `MA_NV` | `NHAN_VIEN(MA_NV)` |
| `HOA_DON` | `MA_KH` | `KHACH_HANG(MA_KH)` |
| `CT_HOA_DON` | `MA_HD` | `HOA_DON(MA_HD)` |
| `CT_HOA_DON` | `MA_SP` | `SAN_PHAM(MA_SP)` |

Mọi quan hệ nêu trên đều 1–N theo ERD. `CT_PHIEU_NHAP` và `CT_HOA_DON` là hai bảng chi tiết nối nghiệp vụ phiếu/hóa đơn với sản phẩm. Mỗi bảng chi tiết đã có PK riêng (`MA_CTPN`, `MA_CT`).

### 5.3. Ràng buộc và mặc định khi viết DDL

- PK/FK đầy đủ; các FK khác đều bắt buộc (`NOT NULL`); **bắt buộc cho phép `HOA_DON.MA_KH = NULL`** để bán cho khách vãng lai. Khi có `MA_KH`, FK vẫn phải tham chiếu khách tồn tại.
- `BARCODE` và `TEN_DANG_NHAP` phải có `UNIQUE`; `TEN_DANG_NHAP` phải `NOT NULL`. Có thể để `BARCODE` nullable nếu dữ liệu mẫu chưa có mã, nhưng khi có giá trị thì phải duy nhất.
- `SO_LUONG_TON >= 0`, `GIA_BAN >= 0`, `DIEM_TICH_LUY >= 0`, `TONG_TIEN >= 0`. Đề xuất `DIEM_TICH_LUY DEFAULT 0 NOT NULL`, `SO_LUONG_TON DEFAULT 0 NOT NULL`.
- Số lượng trong bảng chi tiết phải `> 0`; đơn giá nhập/bán phải `>= 0`.
- Giá tiền sử dụng Oracle `NUMBER(…, 2)` hoặc precision/scale phù hợp, Java `BigDecimal`, không dùng `double` cho tiền. `CT_HOA_DON.DON_GIA` lưu giá bán tại thời điểm giao dịch.
- Định nghĩa kiểu thời gian (`DATE` hoặc `TIMESTAMP`) thống nhất; dùng Java `LocalDate`/`LocalDateTime` phù hợp.
- Dùng `VARCHAR2` với độ dài thích hợp, `NUMBER` cho số lượng và khóa số nếu chọn ID số.
- Xác định cơ chế sinh ID thống nhất với phiên bản Oracle: `IDENTITY` hoặc `SEQUENCE`; không tự sinh ID bằng `MAX(id) + 1`.
- Với mỗi dòng của cùng một hóa đơn/phiếu nhập, nên xác định rõ chính sách trùng `MA_SP`: hợp nhất số lượng hoặc cho phép nhiều dòng (ví dụ có lô/hạn khác nhau). Không tự áp unique khi nghiệp vụ chưa chốt.
- Cân nhắc index trên FK và những cột được tra cứu thường xuyên; tránh tạo index vô tội vạ.

### 5.4. Các quyết định database **đã chốt**

1. **Chỉ dùng 9 bảng** trong ERD; thêm đúng các FK cần thiết vào bảng tương ứng, không tự thêm bảng quan hệ NCC–sản phẩm, bảng giỏ hàng, bảng lô hàng, bảng đơn online, hay bảng vai trò/quyền.
2. **NCC và sản phẩm:** giữ quan hệ **1–N** ban đầu, `SAN_PHAM.MA_NCC NOT NULL` tham chiếu một NCC cố định. Khi nhập hàng, phải kiểm tra NCC trên từng sản phẩm bằng NCC của `PHIEU_NHAP` để tránh nhập sai nhà cung cấp.
3. **Hạn sử dụng:** giữ `SAN_PHAM.HAN_SU_DUNG` ở bảng sản phẩm, **không** quản lý theo lô. Chấp nhận đây là bản đơn giản hóa cho bài tập, không suy diễn tồn kho có nhiều hạn sử dụng. `HAN_SU_DUNG` có thể nullable cho mặt hàng không áp dụng hạn dùng.
4. **Khách vãng lai:** `HOA_DON.MA_KH` **cho phép NULL**. Nếu không có khách thành viên, không tạo bản ghi khách giả để lấp FK.
5. **Nhân viên đăng nhập:** thêm `NHAN_VIEN.MAT_KHAU_HASH VARCHAR2(255) NOT NULL`, lưu mật khẩu bằng password encoder an toàn (ví dụ BCrypt qua Spring Security); tuyệt đối không lưu plaintext. Thêm `NHAN_VIEN.TRANG_THAI` (đề xuất `NUMBER(1) DEFAULT 1 NOT NULL CHECK (TRANG_THAI IN (0,1))`: 1 = hoạt động, 0 = khóa). `TEN_DANG_NHAP` unique/not null.
6. **Vai trò:** dùng ngay `NHAN_VIEN.VAI_TRO` với **chỉ hai giá trị `ADMIN`, `STAFF`** (có thể dùng `CHECK`); `CHUC_VU` là mô tả chức danh, **không** quyết định quyền. Không tạo bảng ROLE/USER mới ở MVP.
7. **Chứng từ:** giữ `HOA_DON`, `CT_HOA_DON`, `PHIEU_NHAP`, `CT_PHIEU_NHAP` như hiện tại. Không tự thêm hủy/hoàn, khuyến mãi, VAT, phương thức trả tiền thật hay trạng thái giao vận.
8. **Điểm tích lũy:** giữ cột `KHACH_HANG.DIEM_TICH_LUY`, mặc định 0. **Công thức tính điểm chưa chốt**; Codex không được tự đặt công thức hoặc cho người dùng sửa điểm tùy ý. Có thể hoàn thiện luồng bán hàng trước khi bổ sung logic tích điểm.

> Các bổ sung nêu trên là phần điều chỉnh ERD được đồng ý cho bản MVP. Nếu cần thay đổi thiết kế vượt phạm vi này, hãy hỏi trước khi sửa DDL.

## 6. Luồng bán hàng POS (phạm vi chốt)

1. Nhân viên đã đăng nhập truy cập trang bán hàng, xem/lọc/tìm kiếm danh sách sản phẩm.
2. Nhân viên chọn sản phẩm để thêm vào **giỏ tạm trong `HttpSession`**, thay đổi số lượng hoặc xóa dòng. Giỏ phải gắn với phiên của nhân viên; không chia sẻ toàn ứng dụng.
3. Giỏ chỉ cần giữ `productId` và `quantity` (cộng dữ liệu hiển thị nếu cần). **Không tin giá/tồn kho gửi từ trình duyệt hoặc giá đã nằm trong session.**
4. Khi xác nhận thanh toán, Service lấy lại các sản phẩm, giá và số lượng từ Oracle; kiểm tra sản phẩm tồn tại, số lượng hợp lệ, tồn kho đủ.
5. Trong **cùng một transaction JDBC**: tạo `HOA_DON`, tạo các `CT_HOA_DON` với **đơn giá tại thời điểm bán**, trừ tồn kho, tính và lưu `TONG_TIEN`. Chỉ cộng điểm khách hàng sau khi có công thức được xác nhận; không tự đoán.
6. Nếu toàn bộ thao tác thành công thì `commit()`, làm rỗng giỏ và hiển thị hóa đơn; bất kỳ lỗi nào thì `rollback()` và giữ giỏ để nhân viên sửa/thử lại.
7. Cho phép khách vãng lai (`MA_KH` nullable). `MA_NV` lấy từ nhân viên đã xác thực, **không tin ID nhân viên do form gửi lên**. Với trang xem hóa đơn, `STAFF` chỉ được xem hóa đơn do chính mình lập; `ADMIN` có quyền xem tất cả.

### Ngăn bán quá số lượng tồn

Không chỉ đọc `SO_LUONG_TON` rồi mới trừ một cách thiếu kiểm soát. Dùng câu `UPDATE` có điều kiện nguyên tử như:

```sql
UPDATE SAN_PHAM
SET SO_LUONG_TON = SO_LUONG_TON - ?
WHERE MA_SP = ? AND SO_LUONG_TON >= ?
```

Bind `quantity`, `productId`, `quantity`; yêu cầu `executeUpdate() == 1`, nếu không phải rollback (hết hàng hoặc ID không còn hợp lệ). Các bước của cùng một hóa đơn dùng **một `Connection` duy nhất**.

### Barcode trong tương lai

`BARCODE` cần là dữ liệu có thể tìm kiếm. Giao diện POS hiện tại **không bắt buộc quét mã**: chọn sản phẩm từ catalog là đủ. Trong lần mở rộng sau, có thể thêm input barcode (nhập thủ công hoặc nhận chuỗi từ máy quét USB kiểu bàn phím) và tái sử dụng logic thêm vào giỏ hiện tại.

## 7. Luồng nhập hàng và tồn kho

1. `ADMIN` chọn nhà cung cấp và các sản phẩm nhập, nhập số lượng/giá nhập.
2. Trong một transaction JDBC, tạo `PHIEU_NHAP` với `MA_NCC`, `MA_NV`, thời gian nhập.
3. Tạo các dòng `CT_PHIEU_NHAP` tương ứng.
4. Cộng `SAN_PHAM.SO_LUONG_TON` đúng số lượng từng dòng.
5. Chỉ commit khi tất cả bước thành công, nếu lỗi rollback toàn bộ.

Vì đã **chốt** quan hệ `SAN_PHAM.MA_NCC`, phải kiểm tra NCC trên từng sản phẩm **trùng với** NCC trên phiếu nhập trước khi cho phép nhập. Trong MVP chỉ `ADMIN` được lập phiếu nhập.

> Quy tắc cần giữ: thay đổi tồn kho phải diễn ra **một lần** cho mỗi giao dịch hợp lệ. Nếu sau này dùng trigger/procedure để cập nhật tồn kho thì không đồng thời cập nhật thêm lần nữa trong Java.

## 8. Quy tắc JDBC thuần và transaction

### Đọc dữ liệu độc lập

- Inject `javax.sql.DataSource` qua constructor của Repository/Service.
- Dùng `try-with-resources` để đóng `Connection`, `PreparedStatement`, `ResultSet` trong các lệnh truy vấn độc lập.
- SQL dùng `?` placeholder và `PreparedStatement` cho mọi giá trị từ user/session; **không nối chuỗi SQL trực tiếp từ input**.
- Với sắp xếp động (`ORDER BY`), chỉ sử dụng tên cột trong whitelist ở server; không bind tên cột bằng `?`.
- Mapping `ResultSet` rõ ràng; có thể tách `mapRow(ResultSet)` nội bộ từng Repository nếu lặp lại.
- Sử dụng `executeQuery()` cho SELECT và `executeUpdate()` cho INSERT/UPDATE/DELETE; kiểm tra số dòng tác động.
- Không nuốt lỗi SQL, không in stack trace/chi tiết SQL nhạy cảm ra HTML.

### Giao dịch nhiều Repository

- Service mở `Connection`, tắt auto-commit, truyền **cùng instance `Connection`** vào mọi phương thức Repository tham gia nghiệp vụ.
- Trong các hàm Repository nhận `Connection` do Service cung cấp, **không** tự `commit()`, `rollback()` hay `close()` connection đó; chỉ đóng Statement/ResultSet do mình tạo.
- Service `commit()` một lần khi hoàn tất; nếu SQLException **hoặc** lỗi nghiệp vụ/runtime exception thì `rollback()`.
- Khôi phục `autoCommit` trước khi trả connection về pool; `try-with-resources` để connection được đóng/trả về pool; bảo toàn lỗi gốc nếu rollback cũng lỗi.
- Không mở transaction kéo dài qua tương tác người dùng (ví dụ trong suốt thời gian nhân viên chỉnh giỏ hàng).
- Không dùng `@Transactional` như thể nó tự điều phối các connection lấy bằng `dataSource.getConnection()`; kiến trúc đã chọn là **transaction JDBC thủ công**.

Mẫu định hướng (**minh họa**, tùy nghiệp vụ mà sửa):

```java
try (Connection conn = dataSource.getConnection()) {
    boolean oldAutoCommit = conn.getAutoCommit();
    try {
        conn.setAutoCommit(false);
        // daoA.insert(conn, ...);
        // daoB.update(conn, ...);
        conn.commit();
    } catch (SQLException | RuntimeException ex) {
        try {
            conn.rollback();
        } catch (SQLException rollbackEx) {
            ex.addSuppressed(rollbackEx);
        }
        throw ex;
    } finally {
        conn.setAutoCommit(oldAutoCommit);
    }
}
```

Với code thực tế, nếu `setAutoCommit(oldAutoCommit)` lỗi thì cần bảo toàn exception chính và bảo đảm connection vẫn được đóng; đừng tạo `Connection` dùng chung singleton.

### Oracle-specific

- Gọi stored procedure/function bằng `CallableStatement` khi bài tập Oracle yêu cầu; **không** gọi PL/SQL chỉ để thay thế mọi CRUD đơn giản.
- Có thể dùng `SEQUENCE.NEXTVAL` để lấy ID trước khi `INSERT` (trên cùng transaction/connection), hoặc identity/returning phù hợp DDL thực tế.
- Kiểm tra version Oracle trước khi viết cú pháp phân trang hoặc tính năng phụ thuộc version.
- Giữ các script SQL trong `database/` để tái tạo schema và dữ liệu mẫu, không dựa vào chỉnh sửa thủ công không được lưu lại. Các lệnh quản trị như `CREATE TABLESPACE`, `CREATE USER`, `GRANT` **không được thực hiện bởi Spring khi startup**; tách khỏi các script DDL nghiệp vụ và chạy bởi tài khoản có quyền.

## 9. MVC, Thymeleaf và bảo mật

- Ưu tiên các trang render bằng server: `GET` cho đọc/xem form, `POST` cho các lệnh thay đổi dữ liệu.
- Dùng `@ModelAttribute` + Jakarta Validation + `BindingResult` cho form; hiển thị thông báo lỗi bên cạnh field tương ứng.
- Sau khi POST thành công, áp dụng **Post/Redirect/Get** và flash message (`RedirectAttributes`) để hạn chế submit trùng khi refresh.
- Dùng Thymeleaf fragments cho header, menu/sidebar, footer, thông báo; không cần frontend framework lớn.
- Không đặt nghiệp vụ thanh toán/trừ kho ở JavaScript. JavaScript chỉ phục vụ UX; mọi kiểm tra và tính tiền phải được thực hiện lại ở server.
- Đảm bảo form thay đổi dữ liệu có cơ chế CSRF phù hợp; không dùng GET để xóa/cập nhật dữ liệu.
- Mật khẩu nhân viên bắt buộc lưu bằng password hash an toàn (ưu tiên Spring Security PasswordEncoder), **không** so sánh plaintext; trạng thái `TRANG_THAI=0` không được đăng nhập. Bảo vệ route theo quyền ở server, không chỉ ẩn menu.
### Ma trận phân quyền **đã chốt cho MVP**

| Chức năng | `ADMIN` | `STAFF` |
|---|---|---|
| Đăng nhập, đăng xuất, đổi mật khẩu của chính mình | Có | Có |
| Xem, tìm kiếm, lọc sản phẩm | Có | Có |
| Thêm sản phẩm vào giỏ POS và lập hóa đơn | Có | Có |
| Xem hóa đơn | Tất cả hóa đơn | **Chỉ** hóa đơn do chính mình lập (`HOA_DON.MA_NV` = người đăng nhập) |
| Xem, thêm, sửa **thông tin** khách hàng | Có | Có |
| Sửa trực tiếp `DIEM_TICH_LUY` | Không | Không |
| Tạo/sửa danh mục và sản phẩm, thay đổi giá | Có | Không |
| Quản lý nhà cung cấp, nhập hàng, cập nhật kho qua phiếu nhập | Có | Không |
| Quản lý tài khoản nhân viên và quyền | Có | Không |
| Báo cáo tổng thể doanh thu, tồn kho, bán hàng | Có | Không |

- `ADMIN` có thể tạo nhân viên, cập nhật thông tin, đặt lại mật khẩu, đổi `VAI_TRO`, khóa/mở tài khoản. **Không xóa cứng nhân viên** đã có chứng từ liên quan; dùng `TRANG_THAI=0`. Không cho phép vô hiệu hóa hoặc hạ quyền tài khoản `ADMIN` hoạt động cuối cùng.
- `STAFF` chỉ làm công việc bán hàng và hỗ trợ khách hàng; không được tự cấp quyền, quản lý nhân viên, chỉnh giá/tồn kho hay xem báo cáo quản trị. Trang và các action đều phải kiểm tra quyền server-side, không chỉ ẩn nút trên HTML.
- Khi `STAFF` yêu cầu chi tiết hóa đơn, phải kiểm tra chủ sở hữu theo **đúng mã nhân viên đăng nhập** tại truy vấn/Service; không tin mã nhân viên được gửi trong query/form.
- `STAFF` được cập nhật họ tên, số điện thoại của khách theo nghiệp vụ; điểm thưởng không được chỉnh thủ công. Không xóa cứng khách có lịch sử hóa đơn.
- Khi seed dữ liệu phải cung cấp tài khoản `ADMIN` đầu tiên với mật khẩu băm, tránh hardcode mật khẩu mặc định công khai trong code sản phẩm; có thể cung cấp giá trị demo qua tài liệu/dev config riêng.
- `CHUC_VU` chỉ để hiển thị nghiệp vụ; kiểm tra quyền dựa trên `VAI_TRO`. Nếu Spring Security dùng `ROLE_` prefix, ánh xạ `ADMIN`/`STAFF` nhất quán (ví dụ `ROLE_ADMIN`, `ROLE_STAFF` trong GrantedAuthority).

### Tuyến trang gợi ý (có thể điều chỉnh theo repo)

```text
GET  /login                      # form đăng nhập
GET  /products                   # catalog nội bộ, chỉ nhân viên đã đăng nhập
GET  /pos                        # trang giỏ POS
POST /pos/items                  # thêm sản phẩm
POST /pos/items/{productId}      # cập nhật số lượng (hoặc route tương đương)
POST /pos/items/{productId}/remove
POST /pos/checkout               # lập hóa đơn atomically
GET  /invoices/{id}              # ADMIN xem tất cả; STAFF chỉ của mình
GET  /imports                    # danh sách phiếu nhập: ADMIN
GET  /employees                  # quản lý nhân viên: ADMIN
GET  /customers                  # khách hàng: ADMIN và STAFF
GET  /reports                    # báo cáo tổng thể: ADMIN
```

Đây là **MVC routes trả view/redirect**, không phải đặc tả REST API. Hãy đảm bảo các route thực tế không mâu thuẫn với ứng dụng hiện có.

## 10. Chất lượng dữ liệu và xử lý lỗi

- Validate số lượng dương, barcode hợp lệ theo quy tắc ứng dụng, giá không âm, dữ liệu bắt buộc không trống.
- Trước khi xóa danh mục/NCC/sản phẩm, tôn trọng FK và lịch sử chứng từ; tránh cascade delete làm mất lịch sử giao dịch. Nếu cần, dùng cơ chế ngừng kinh doanh/ẩn thay vì xóa vật lý (cần bổ sung cột khi được duyệt).
- Hiển thị thông báo nghiệp vụ rõ nghĩa: không tìm thấy sản phẩm, không đủ tồn kho, sản phẩm bị trùng, tài khoản không có quyền, số lượng không hợp lệ.
- Không rò rỉ stack trace, username/password DB, hoặc SQL nội bộ cho người dùng cuối.
- Với tiền, dùng `BigDecimal`, quy tắc làm tròn rõ ràng; `TONG_TIEN` là tổng được tính từ chi tiết giao dịch trong transaction.
- Khi cần thống kê, ưu tiên SQL Oracle (`JOIN`, `GROUP BY`, `HAVING`, aggregate) thay vì tải mọi dòng về Java để tính.

## 11. Kiểm thử tối thiểu

Hãy viết kiểm thử hoặc kế hoạch kiểm thử cho các tình huống:

1. Nhân viên chưa đăng nhập không truy cập được `/products`, `/pos`, các trang nội bộ.
2. `STAFF` không thể truy cập quản lý nhân viên, NCC, nhập kho, sửa giá, báo cáo (kể cả gửi POST trực tiếp). `ADMIN` làm được các thao tác được cho phép.
3. Tìm kiếm và chọn sản phẩm hợp lệ vào giỏ; chọn trùng tăng số lượng đúng (không bắt buộc phần cứng quét mã).
4. Giỏ có sản phẩm không tồn tại / số lượng 0 hoặc âm → bị từ chối.
5. Bán hàng đủ tồn kho → tạo đúng hóa đơn, dòng chi tiết, tổng tiền và giảm tồn kho.
6. Bán vượt tồn kho → báo lỗi, không tạo hóa đơn nửa chừng, tồn kho không âm.
7. Lỗi khi insert chi tiết/ghi hóa đơn → rollback toàn bộ transaction.
8. Hai giao dịch đồng thời không thể trừ tồn kho vượt quá số lượng hiện có.
9. Nhập hàng thành công → tăng kho đúng; lỗi giữa chừng → rollback.
10. Lịch sử hóa đơn vẫn giữ đơn giá cũ dù giá niêm yết sản phẩm đã thay đổi.
11. Khách vãng lai (`MA_KH=NULL`) vẫn lập hóa đơn; khách thành viên có FK hợp lệ.
12. `STAFF` chỉ xem được hóa đơn của chính mình, không truy cập được hóa đơn khác khi đoán ID.
13. Tài khoản `TRANG_THAI=0` không đăng nhập được; `ADMIN` có thể khóa/mở tài khoản và thay đổi vai trò, `STAFF` thì không.
14. Phiếu nhập có sản phẩm thuộc NCC khác bị từ chối và rollback.
15. Các truy vấn có input đặc biệt không dẫn đến SQL injection.

Ưu tiên chạy kiểm thử tích hợp với Oracle thực tế hoặc môi trường Oracle test tương thích; không mặc định H2 có hành vi tương đương Oracle.

## 12. Kế hoạch triển khai tăng dần

**Giai đoạn 1 — Nền tảng:** Kiểm tra project, dependency; xác minh Oracle/PDB `FREEPDB1`, schema `sieuthi`, tablespace `TS_SIEUTHI` (mục 2.1) đã được tạo thủ công; cấu hình `DataSource` qua biến môi trường; chốt DDL 9 bảng và 10 FK; tạo script khởi tạo + seed để chạy thủ công.

**Giai đoạn 2 — Nhân viên và sản phẩm:** Xác thực với `MAT_KHAU_HASH`, kiểm tra `TRANG_THAI`, phân quyền `ADMIN`/`STAFF`, layout Thymeleaf; catalog nội bộ, tìm kiếm/lọc/phân trang.

**Giai đoạn 3 — POS:** Giỏ hàng session, chọn sản phẩm, checkout với transaction JDBC, hóa đơn và chi tiết.

**Giai đoạn 4 — Nhập kho:** Chỉ `ADMIN` quản lý NCC, lập phiếu nhập (kiểm tra NCC theo sản phẩm), cập nhật tồn kho bằng transaction.

**Giai đoạn 5 — Quản trị:** `ADMIN` quản lý danh mục/sản phẩm/nhân viên và thống kê; `ADMIN`/`STAFF` quản lý thông tin khách hàng theo quyền, không được chỉnh điểm thủ công.

**Giai đoạn 6 — Bài tập Oracle và hoàn thiện:** View, procedure, function, trigger, index hoặc báo cáo nâng cao **nếu phù hợp yêu cầu môn học**, kiểm thử và tài liệu hướng dẫn chạy.

Trong mỗi giai đoạn:
- Đề xuất các file sẽ thêm/sửa trước khi thay đổi lớn.
- Viết code + template + SQL tương ứng, không bỏ sót một tầng.
- Build/test bằng lệnh phù hợp repo (`./mvnw test`, `mvn test`, hoặc Gradle tương ứng).
- Báo rõ phần đã hoàn thành, phần chưa kiểm thử và các quyết định còn mở.

## 13. Điều kiện hoàn thành một chức năng

Một chức năng chỉ xem là hoàn thành khi:

- Giao diện Thymeleaf hoạt động đúng với Spring MVC, có xử lý input và lỗi.
- Controller, Service, Repository tuân thủ phân tầng; không dùng ORM/JdbcTemplate.
- SQL được bind tham số; connection/statement/result set được giải phóng đúng.
- Nếu chức năng thay đổi nhiều bảng, đã xử lý commit/rollback và concurrent stock update khi cần.
- Các trường hợp validation, authorization và lỗi nghiệp vụ được xử lý.
- Có script DDL/seed khi thay đổi cấu trúc DB, cùng ít nhất một kịch bản kiểm thử.

---

## 14. Các chi tiết **còn mở** và nguyên tắc không tự mở rộng

**ĐÃ CHỐT:** Giữ 9 bảng, 10 FK; `SAN_PHAM.MA_NCC` (một sản phẩm thuộc một NCC), `SAN_PHAM.HAN_SU_DUNG` (không quản lý lô); `HOA_DON.MA_KH` nullable; bổ sung `NHAN_VIEN.MAT_KHAU_HASH` và `TRANG_THAI`; `VAI_TRO` chỉ có `ADMIN`/`STAFF`; phân quyền tại mục 9; POS nội bộ + giỏ session, không làm đặt hàng online và không cần máy quét mã.

**CẤU HÌNH ORACLE DỰ KIẾN:** Oracle chạy local trên Windows, không Docker; `FREEPDB1` / `sieuthi` / `TS_SIEUTHI`, datafile Windows, dung lượng và các grant xem mục 2.1. Đây là kế hoạch môi trường, **không đồng nghĩa script đã chạy**. Mật khẩu không được lưu trong repo và Spring không tự tạo schema.

**CHƯA CẦN CHỐT ĐỂ BẮT ĐẦU:**

- Phiên bản Java/Spring Boot/Oracle và công cụ build: đọc repo/môi trường hiện có trước; không tự thêm Docker.
- Chi tiết DDL: kích thước VARCHAR2, precision NUMBER, `IDENTITY` hay `SEQUENCE`; đề xuất nhất quán theo version Oracle, ghi vào script, không tự đổi tên cột.
- Công thức tích điểm, thời điểm cộng/trừ điểm: **chưa triển khai tự động** cho tới khi thống nhất; có thể để cột mặc định 0.
- Hủy/hoàn hóa đơn, VAT, chiết khấu, quản lý lô, đa NCC, đa chi nhánh và tích hợp thanh toán thật: **ngoài phạm vi MVP**, không tự bổ sung.
- `HINH_THUC_TT`: có thể demo tiền mặt/chuyển khoản như một lựa chọn ghi nhận, không tích hợp cổng thanh toán.

Ưu tiên quy tắc nghiệp vụ rõ ràng và code dễ học. Không tự thiết kế lại ERD hoặc nâng hệ thống thành sản phẩm thương mại điện tử.
