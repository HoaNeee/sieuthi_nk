package com.nhomkha.assignment_java.repository;

import com.nhomkha.assignment_java.dto.*;
import com.nhomkha.assignment_java.model.*;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class DirectoryRepository {
  private final DataSource ds;

  public DirectoryRepository(DataSource ds) {
    this.ds = ds;
  }

  public List<Category> categories() throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s = c.prepareStatement("SELECT * FROM DANH_MUC ORDER BY TEN_DM");
        ResultSet r = s.executeQuery()) {
      List<Category> a = new ArrayList<>();
      while (r.next())
        a.add(new Category(r.getLong("MA_DM"), r.getString("TEN_DM"), r.getString("MO_TA")));
      return a;
    }
  }

  public void saveCategory(Long id, CategoryForm f) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s =
            c.prepareStatement(
                id == null
                    ? "INSERT INTO DANH_MUC(TEN_DM,MO_TA,MA_DM) VALUES(?,?,SEQ_DM.NEXTVAL)"
                    : "UPDATE DANH_MUC SET TEN_DM=?,MO_TA=? WHERE MA_DM=?")) {
      s.setString(1, f.getName().trim());
      s.setString(2, f.getDescription());
      if (id != null) s.setLong(3, id);
      check(s.executeUpdate());
    }
  }

  public List<Supplier> suppliers() throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s = c.prepareStatement("SELECT * FROM NHA_CUNG_CAP ORDER BY TEN_NCC");
        ResultSet r = s.executeQuery()) {
      List<Supplier> a = new ArrayList<>();
      while (r.next())
        a.add(
            new Supplier(
                r.getLong("MA_NCC"),
                r.getString("TEN_NCC"),
                r.getString("SDT"),
                r.getString("DIA_CHI"),
                r.getString("EMAIL")));
      return a;
    }
  }

  public void saveSupplier(Long id, SupplierForm f) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s =
            c.prepareStatement(
                id == null
                    ? "INSERT INTO NHA_CUNG_CAP(TEN_NCC,SDT,DIA_CHI,EMAIL,MA_NCC)"
                        + " VALUES(?,?,?,?,SEQ_NCC.NEXTVAL)"
                    : "UPDATE NHA_CUNG_CAP SET TEN_NCC=?,SDT=?,DIA_CHI=?,EMAIL=? WHERE MA_NCC=?")) {
      s.setString(1, f.getName().trim());
      s.setString(2, f.getPhone());
      s.setString(3, f.getAddress());
      s.setString(4, f.getEmail());
      if (id != null) s.setLong(5, id);
      check(s.executeUpdate());
    }
  }

  public List<Customer> customers(String query) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s =
            c.prepareStatement(
                "SELECT * FROM KHACH_HANG WHERE LOWER(HO_TEN) LIKE ? OR SDT LIKE ? ORDER BY MA_KH"
                    + " DESC")) {
      s.setString(1, "%" + query.toLowerCase(java.util.Locale.ROOT) + "%");
      s.setString(2, "%" + query + "%");
      try (ResultSet r = s.executeQuery()) {
        List<Customer> a = new ArrayList<>();
        while (r.next())
          a.add(
              new Customer(
                  r.getLong("MA_KH"),
                  r.getString("HO_TEN"),
                  r.getString("SDT"),
                  r.getInt("DIEM_TICH_LUY")));
        return a;
      }
    }
  }

  public void saveCustomer(Long id, CustomerForm f) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s =
            c.prepareStatement(
                id == null
                    ? "INSERT INTO KHACH_HANG(HO_TEN,SDT,MA_KH) VALUES(?,?,SEQ_KH.NEXTVAL)"
                    : "UPDATE KHACH_HANG SET HO_TEN=?,SDT=? WHERE MA_KH=?")) {
      s.setString(1, f.getName().trim());
      s.setString(2, f.getPhone());
      if (id != null) s.setLong(3, id);
      check(s.executeUpdate());
    }
  }

  private void check(int count) {
    if (count != 1)
      throw new com.nhomkha.assignment_java.exception.BusinessException(
          "Bản ghi không còn tồn tại.");
  }
}
