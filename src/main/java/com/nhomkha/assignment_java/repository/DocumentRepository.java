package com.nhomkha.assignment_java.repository;

import com.nhomkha.assignment_java.model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentRepository {
  private final DataSource ds;

  public DocumentRepository(DataSource ds) {
    this.ds = ds;
  }

  public long nextInvoice(Connection c) throws SQLException {
    try (PreparedStatement s = c.prepareStatement("SELECT SEQ_HD.NEXTVAL FROM DUAL");
        ResultSet r = s.executeQuery()) {
      r.next();
      return r.getLong(1);
    }
  }

  public long nextImport(Connection c) throws SQLException {
    try (PreparedStatement s = c.prepareStatement("SELECT SEQ_PN.NEXTVAL FROM DUAL");
        ResultSet r = s.executeQuery()) {
      r.next();
      return r.getLong(1);
    }
  }

  public boolean customerExists(Connection c, long id) throws SQLException {
    try (PreparedStatement s = c.prepareStatement("SELECT MA_KH FROM KHACH_HANG WHERE MA_KH=?")) {
      s.setLong(1, id);
      try (ResultSet r = s.executeQuery()) {
        return r.next();
      }
    }
  }

  public void invoice(
      Connection c, long id, long employee, Long customer, String payment, BigDecimal total)
      throws SQLException {
    try (PreparedStatement s =
        c.prepareStatement(
            "INSERT INTO HOA_DON(MA_HD,MA_NV,MA_KH,HINH_THUC_TT,TONG_TIEN) VALUES(?,?,?,?,?)")) {
      s.setLong(1, id);
      s.setLong(2, employee);
      s.setObject(3, customer, Types.NUMERIC);
      s.setString(4, payment);
      s.setBigDecimal(5, total);
      check(s.executeUpdate());
    }
  }

  public void invoiceLine(Connection c, long invoice, long product, int quantity, BigDecimal price)
      throws SQLException {
    try (PreparedStatement s =
        c.prepareStatement(
            "INSERT INTO CT_HOA_DON(MA_CT,MA_HD,MA_SP,SO_LUONG,DON_GIA)"
                + " VALUES(SEQ_CT.NEXTVAL,?,?,?,?)")) {
      s.setLong(1, invoice);
      s.setLong(2, product);
      s.setInt(3, quantity);
      s.setBigDecimal(4, price);
      check(s.executeUpdate());
    }
  }

  public void stockImport(Connection c, long id, long employee, long supplier) throws SQLException {
    try (PreparedStatement s =
        c.prepareStatement("INSERT INTO PHIEU_NHAP(MA_PN,MA_NV,MA_NCC) VALUES(?,?,?)")) {
      s.setLong(1, id);
      s.setLong(2, employee);
      s.setLong(3, supplier);
      check(s.executeUpdate());
    }
  }

  public void importLine(Connection c, long document, long product, int quantity, BigDecimal price)
      throws SQLException {
    try (PreparedStatement s =
        c.prepareStatement(
            "INSERT INTO CT_PHIEU_NHAP(MA_CTPN,MA_PN,MA_SP,SO_LUONG,DON_GIA_NHAP)"
                + " VALUES(SEQ_CTPN.NEXTVAL,?,?,?,?)")) {
      s.setLong(1, document);
      s.setLong(2, product);
      s.setInt(3, quantity);
      s.setBigDecimal(4, price);
      check(s.executeUpdate());
    }
  }

  public List<DocumentHeader> invoices(Long employee, Long id) throws SQLException {
    String sql =
        "SELECT h.MA_HD,h.NGAY_LAP,n.HO_TEN employee,k.HO_TEN partner,h.HINH_THUC_TT,h.TONG_TIEN"
            + " FROM HOA_DON h JOIN NHAN_VIEN n ON n.MA_NV=h.MA_NV LEFT JOIN KHACH_HANG k ON"
            + " k.MA_KH=h.MA_KH WHERE (? IS NULL OR h.MA_NV=?) AND (? IS NULL OR h.MA_HD=?) ORDER"
            + " BY h.MA_HD DESC";
    try (Connection c = ds.getConnection();
        PreparedStatement s = c.prepareStatement(sql)) {
      s.setObject(1, employee, Types.NUMERIC);
      s.setObject(2, employee, Types.NUMERIC);
      s.setObject(3, id, Types.NUMERIC);
      s.setObject(4, id, Types.NUMERIC);
      try (ResultSet r = s.executeQuery()) {
        List<DocumentHeader> a = new ArrayList<>();
        while (r.next())
          a.add(
              new DocumentHeader(
                  r.getLong(1),
                  r.getTimestamp(2).toLocalDateTime(),
                  r.getString(3),
                  r.getString(4),
                  r.getString(5),
                  r.getBigDecimal(6)));
        return a;
      }
    }
  }

  public List<DocumentHeader> imports(Long id) throws SQLException {
    String sql =
        "SELECT p.MA_PN,p.NGAY_NHAP,n.HO_TEN,s.TEN_NCC,(SELECT SUM(ct.SO_LUONG*ct.DON_GIA_NHAP)"
            + " FROM CT_PHIEU_NHAP ct WHERE ct.MA_PN=p.MA_PN) total FROM PHIEU_NHAP p JOIN"
            + " NHAN_VIEN n ON n.MA_NV=p.MA_NV JOIN NHA_CUNG_CAP s ON s.MA_NCC=p.MA_NCC WHERE (? IS"
            + " NULL OR p.MA_PN=?) ORDER BY p.MA_PN DESC";
    try (Connection c = ds.getConnection();
        PreparedStatement s = c.prepareStatement(sql)) {
      s.setObject(1, id, Types.NUMERIC);
      s.setObject(2, id, Types.NUMERIC);
      try (ResultSet r = s.executeQuery()) {
        List<DocumentHeader> a = new ArrayList<>();
        while (r.next())
          a.add(
              new DocumentHeader(
                  r.getLong(1),
                  r.getTimestamp(2).toLocalDateTime(),
                  r.getString(3),
                  r.getString(4),
                  null,
                  r.getBigDecimal(5)));
        return a;
      }
    }
  }

  public List<DocumentLine> invoiceLines(long id, Long employee) throws SQLException {
    String sql =
        "SELECT p.TEN_SP,ct.SO_LUONG,ct.DON_GIA FROM CT_HOA_DON ct JOIN SAN_PHAM p ON"
            + " p.MA_SP=ct.MA_SP JOIN HOA_DON h ON h.MA_HD=ct.MA_HD WHERE ct.MA_HD=? AND (? IS NULL"
            + " OR h.MA_NV=?) ORDER BY ct.MA_CT";
    try (Connection c = ds.getConnection();
        PreparedStatement s = c.prepareStatement(sql)) {
      s.setLong(1, id);
      s.setObject(2, employee, Types.NUMERIC);
      s.setObject(3, employee, Types.NUMERIC);
      return readLines(s);
    }
  }

  public List<DocumentLine> importLines(long id) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s =
            c.prepareStatement(
                "SELECT p.TEN_SP,ct.SO_LUONG,ct.DON_GIA_NHAP FROM CT_PHIEU_NHAP ct JOIN SAN_PHAM p"
                    + " ON p.MA_SP=ct.MA_SP WHERE ct.MA_PN=? ORDER BY ct.MA_CTPN")) {
      s.setLong(1, id);
      return readLines(s);
    }
  }

  private List<DocumentLine> readLines(PreparedStatement s) throws SQLException {
    try (ResultSet r = s.executeQuery()) {
      List<DocumentLine> a = new ArrayList<>();
      while (r.next()) a.add(new DocumentLine(r.getString(1), r.getInt(2), r.getBigDecimal(3)));
      return a;
    }
  }

  private void check(int n) throws SQLException {
    if (n != 1) throw new SQLException("Document write failed");
  }
}
