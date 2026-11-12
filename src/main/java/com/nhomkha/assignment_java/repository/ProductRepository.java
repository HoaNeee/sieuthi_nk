package com.nhomkha.assignment_java.repository;

import com.nhomkha.assignment_java.dto.ProductForm;
import com.nhomkha.assignment_java.model.Product;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {
  private final DataSource ds;
  private static final String SELECT =
      "SELECT p.*, d.TEN_DM, n.TEN_NCC FROM SAN_PHAM p JOIN DANH_MUC d ON d.MA_DM=p.MA_DM JOIN"
          + " NHA_CUNG_CAP n ON n.MA_NCC=p.MA_NCC";

  public ProductRepository(DataSource ds) {
    this.ds = ds;
  }

  private Product map(ResultSet r) throws SQLException {
    java.sql.Date expiry = r.getDate("HAN_SU_DUNG");
    Product product = new Product(
        r.getLong("MA_SP"),
        r.getString("BARCODE"),
        r.getString("TEN_SP"),
        r.getBigDecimal("GIA_BAN"),
        r.getInt("SO_LUONG_TON"),
        expiry == null ? null : expiry.toLocalDate(),
        r.getLong("MA_DM"),
        r.getLong("MA_NCC"),
        r.getString("TEN_DM"),
        r.getString("TEN_NCC"));
    product.setUnit(r.getString("DON_VI_TINH"));
    product.setPurchasePrice(r.getBigDecimal("GIA_NHAP"));
    return product;
  }

  public List<Product> search(String query, Long category, int page) throws SQLException {
    // ROWNUM pagination works on older Oracle releases too; order is stable.
    String sql =
        "SELECT * FROM (SELECT a.*, ROWNUM rn FROM ("
            + SELECT
            + " WHERE (LOWER(p.TEN_SP) LIKE ? OR LOWER(p.BARCODE) LIKE ?) AND (? IS NULL OR"
            + " p.MA_DM=?) ORDER BY p.MA_SP) a WHERE ROWNUM<=?) WHERE rn>?";
    try (Connection c = ds.getConnection();
        PreparedStatement s = c.prepareStatement(sql)) {
      String q = "%" + query.toLowerCase(Locale.ROOT) + "%";
      s.setString(1, q);
      s.setString(2, q);
      s.setObject(3, category, Types.NUMERIC);
      s.setObject(4, category, Types.NUMERIC);
      s.setLong(5, (long) page * 20 + 21);
      s.setLong(6, (long) page * 20);
      try (ResultSet r = s.executeQuery()) {
        List<Product> a = new ArrayList<>();
        while (r.next()) a.add(map(r));
        return a;
      }
    }
  }

  public Optional<Product> find(long id) throws SQLException {
    try (Connection c = ds.getConnection()) {
      return find(c, id, false);
    }
  }

  public List<Product> importChoices() throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s = c.prepareStatement(SELECT + " ORDER BY p.TEN_SP");
        ResultSet r = s.executeQuery()) {
      List<Product> a = new ArrayList<>();
      while (r.next()) a.add(map(r));
      return a;
    }
  }

  public Optional<Product> find(Connection c, long id, boolean lock) throws SQLException {
    try (PreparedStatement s =
        c.prepareStatement(
            SELECT + " WHERE p.MA_SP=?" + (lock ? " FOR UPDATE OF p.SO_LUONG_TON" : ""))) {
      s.setLong(1, id);
      try (ResultSet r = s.executeQuery()) {
        return r.next() ? Optional.of(map(r)) : Optional.empty();
      }
    }
  }

  public void save(Long id, ProductForm f) throws SQLException {
    String sql =
        id == null
            ? "INSERT INTO SAN_PHAM(TEN_SP,BARCODE,GIA_BAN,MA_DM,MA_NCC,HAN_SU_DUNG,DON_VI_TINH,GIA_NHAP)"
                + " VALUES(?,?,?,?,?,?,?,?)"
            : "UPDATE SAN_PHAM SET TEN_SP=?,BARCODE=?,GIA_BAN=?,MA_DM=?,MA_NCC=?,HAN_SU_DUNG=?,DON_VI_TINH=?,GIA_NHAP=?"
                + " WHERE MA_SP=?";
    try (Connection c = ds.getConnection();
        PreparedStatement s = c.prepareStatement(sql)) {
      s.setString(1, f.getName().trim());
      s.setString(2, f.getBarcode());
      s.setBigDecimal(3, f.getPrice());
      s.setLong(4, f.getCategoryId());
      s.setLong(5, f.getSupplierId());
      s.setDate(6, f.getExpiry() == null ? null : java.sql.Date.valueOf(f.getExpiry()));
      s.setString(7, f.getUnit().trim());
      s.setBigDecimal(8, f.getPurchasePrice());
      if (id != null) s.setLong(9, id);
      if (s.executeUpdate() != 1)
        throw new com.nhomkha.assignment_java.exception.BusinessException(
            "Không tìm thấy sản phẩm.");
    }
  }

  public boolean decrease(Connection c, long id, int qty) throws SQLException {
    try (PreparedStatement s =
        c.prepareStatement(
            "UPDATE SAN_PHAM SET SO_LUONG_TON=SO_LUONG_TON-? WHERE MA_SP=? AND SO_LUONG_TON>=?")) {
      s.setInt(1, qty);
      s.setLong(2, id);
      s.setInt(3, qty);
      return s.executeUpdate() == 1;
    }
  }

  public boolean increase(Connection c, long id, int qty, long supplier) throws SQLException {
    try (PreparedStatement s =
        c.prepareStatement(
            "UPDATE SAN_PHAM SET SO_LUONG_TON=SO_LUONG_TON+? WHERE MA_SP=? AND MA_NCC=? AND"
                + " SO_LUONG_TON<=999999999-?")) {
      s.setInt(1, qty);
      s.setLong(2, id);
      s.setLong(3, supplier);
      s.setInt(4, qty);
      return s.executeUpdate() == 1;
    }
  }
}
