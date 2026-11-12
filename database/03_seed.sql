-- SQLcl / SQL*Plus: run once after schema. Supply a BCrypt hash from PasswordHashTool.
-- No publicly known password; hash is entered hidden, not committed.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET VERIFY OFF
ACCEPT admin_hash CHAR PROMPT 'BCrypt hash for first admin: ' HIDE
INSERT INTO NHAN_VIEN (HO_TEN, CHUC_VU, TEN_DANG_NHAP, MAT_KHAU, VAI_TRO, TRANG_THAI)
VALUES ( 'Quan tri vien', 'Quan ly', 'admin', '&admin_hash', 'ADMIN', 'ACTIVE');
UNDEFINE admin_hash
INSERT INTO DANH_MUC (TEN_DM, MO_TA) VALUES ( 'Thuc pham', 'Danh muc mau');
INSERT INTO NHA_CUNG_CAP (TEN_NCC, SDT, DIA_CHI, EMAIL) VALUES ( 'Nha cung cap mau', NULL, NULL, NULL);
INSERT INTO SAN_PHAM (BARCODE, TEN_SP, GIA_BAN, SO_LUONG_TON, MA_DM, MA_NCC, DON_VI_TINH, GIA_NHAP)
SELECT 'DEMO001', 'San pham mau', 15000, 0, d.MA_DM, n.MA_NCC, 'Cai', 10000
FROM DANH_MUC d CROSS JOIN NHA_CUNG_CAP n WHERE d.TEN_DM='Thuc pham' AND n.TEN_NCC='Nha cung cap mau';
COMMIT;

