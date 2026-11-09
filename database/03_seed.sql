-- SQLcl / SQL*Plus: run once after schema. Supply a BCrypt hash from PasswordHashTool.
-- No publicly known password; hash is entered hidden, not committed.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET VERIFY OFF
ACCEPT admin_hash CHAR PROMPT 'BCrypt hash for first admin: ' HIDE
INSERT INTO NHAN_VIEN (MA_NV, HO_TEN, CHUC_VU, TEN_DANG_NHAP, MAT_KHAU_HASH, VAI_TRO, TRANG_THAI)
VALUES (SEQ_NV.NEXTVAL, 'Quan tri vien', 'Quan ly', 'admin', '&admin_hash', 'ADMIN', 1);
UNDEFINE admin_hash
INSERT INTO DANH_MUC VALUES (SEQ_DM.NEXTVAL, 'Thuc pham', 'Danh muc mau');
INSERT INTO NHA_CUNG_CAP VALUES (SEQ_NCC.NEXTVAL, 'Nha cung cap mau', NULL, NULL, NULL);
INSERT INTO SAN_PHAM (MA_SP, BARCODE, TEN_SP, GIA_BAN, SO_LUONG_TON, MA_DM, MA_NCC)
SELECT SEQ_SP.NEXTVAL, 'DEMO001', 'San pham mau', 15000, 0, d.MA_DM, n.MA_NCC
FROM DANH_MUC d CROSS JOIN NHA_CUNG_CAP n WHERE d.TEN_DM='Thuc pham' AND n.TEN_NCC='Nha cung cap mau';
COMMIT;

