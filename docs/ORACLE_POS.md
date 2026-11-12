# Oracle POS đã đối chiếu ngày 12/11/2026

Schema thực tế: SIEUTHI trong XEPDB1. HOA_DON.MA_HD, CT_HOA_DON.MA_CT và KHACH_HANG.MA_KH dùng IDENTITY. Code lấy mã bằng JDBC generated keys, không gọi SEQ_HD/SEQ_CT/SEQ_KH.

Trigger TRG_CTHD_TON_KHO trên CT_HOA_DON tự trừ tồn khi INSERT. Checkout khóa dòng sản phẩm theo thứ tự mã và kiểm tra đủ tồn, rồi ghi hóa đơn và chi tiết trên cùng connection. Java không trừ thêm. Trigger phải được bật; không tắt trigger khi dùng luồng này. Các thao tác sửa/xóa chi tiết hóa đơn không được cung cấp trong ứng dụng.

Đã thử ghi hóa đơn khách vãng lai, chi tiết và xác nhận tồn giảm đúng một lần trên Oracle; rollback toàn bộ dữ liệu thử. Sequence identity có thể tăng dù rollback.

/products tích hợp catalog và giỏ; /pos vẫn mở cùng trang. Thêm nhanh khách hàng yêu cầu tên và số điện thoại, tự chọn khách vừa tạo; không sửa điểm.

01_schema.sql là script cho schema mới, không chạy lại trên database hiện có. Thay đổi lần này không chạy DDL hoặc đổi trigger trong database của người dùng. Đã đối chiếu thêm toàn bộ 9 bảng: tất cả dùng IDENTITY. Đã thử insert qua các Repository của danh mục, NCC, nhân viên, khách hàng, sản phẩm, phiếu nhập/chi tiết, hóa đơn/chi tiết và rollback toàn bộ. Trigger TRG_CTPN_TON_KHO cộng kho khi nhập, Java không cộng thêm. Kiểm tra chuỗi nhập 3 → bán 1 → tồn 2 đạt. Sản phẩm bắt buộc có barcode, đơn vị tính và giá nhập; đã bổ sung form và mapping.
