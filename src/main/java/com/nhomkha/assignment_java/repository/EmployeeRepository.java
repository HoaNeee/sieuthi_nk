package com.nhomkha.assignment_java.repository;

import com.nhomkha.assignment_java.dto.EmployeeForm;
import com.nhomkha.assignment_java.model.Employee;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeRepository {
  private final DataSource ds;

  public EmployeeRepository(DataSource ds) {
    this.ds = ds;
  }

  private Employee map(ResultSet r) throws SQLException {
    return new Employee(
        r.getLong("MA_NV"),
        r.getString("HO_TEN"),
        r.getString("CHUC_VU"),
        r.getString("TEN_DANG_NHAP"),
        r.getString("MAT_KHAU_HASH"),
        r.getString("VAI_TRO"),
        r.getInt("TRANG_THAI") == 1);
  }

  public Optional<Employee> byUsername(String username) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s = c.prepareStatement("SELECT * FROM NHAN_VIEN WHERE TEN_DANG_NHAP=?")) {
      s.setString(1, username);
      try (ResultSet r = s.executeQuery()) {
        return r.next() ? Optional.of(map(r)) : Optional.empty();
      }
    }
  }

  public Optional<Employee> byId(Connection c, long id) throws SQLException {
    try (PreparedStatement s = c.prepareStatement("SELECT * FROM NHAN_VIEN WHERE MA_NV=?")) {
      s.setLong(1, id);
      try (ResultSet r = s.executeQuery()) {
        return r.next() ? Optional.of(map(r)) : Optional.empty();
      }
    }
  }

  public List<Employee> all() throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s = c.prepareStatement("SELECT * FROM NHAN_VIEN ORDER BY MA_NV");
        ResultSet r = s.executeQuery()) {
      List<Employee> list = new ArrayList<>();
      while (r.next()) list.add(map(r));
      return list;
    }
  }

  // Serializes account changes, including two administrators concurrently demoting one another.
  public void lockAccounts(Connection c) throws SQLException {
    try (PreparedStatement s = c.prepareStatement("LOCK TABLE NHAN_VIEN IN EXCLUSIVE MODE")) {
      s.executeUpdate();
    }
  }

  public int activeAdmins(Connection c) throws SQLException {
    try (PreparedStatement s =
            c.prepareStatement(
                "SELECT COUNT(*) FROM NHAN_VIEN WHERE VAI_TRO='ADMIN' AND TRANG_THAI=1");
        ResultSet r = s.executeQuery()) {
      r.next();
      return r.getInt(1);
    }
  }

  public void save(Connection c, Long id, EmployeeForm f, String hash) throws SQLException {
    String sql =
        id == null
            ? "INSERT INTO"
                + " NHAN_VIEN(HO_TEN,CHUC_VU,TEN_DANG_NHAP,VAI_TRO,TRANG_THAI,MAT_KHAU_HASH,MA_NV)"
                + " VALUES(?,?,?,?,?,?,SEQ_NV.NEXTVAL)"
            : "UPDATE NHAN_VIEN SET"
                + " HO_TEN=?,CHUC_VU=?,TEN_DANG_NHAP=?,VAI_TRO=?,TRANG_THAI=?,MAT_KHAU_HASH=?"
                + " WHERE MA_NV=?";
    try (PreparedStatement s = c.prepareStatement(sql)) {
      s.setString(1, f.getName().trim());
      s.setString(2, f.getTitle());
      s.setString(3, f.getUsername());
      s.setString(4, f.getRole());
      s.setInt(5, f.getActive() ? 1 : 0);
      s.setString(6, hash);
      if (id != null) s.setLong(7, id);
      if (s.executeUpdate() != 1) throw new SQLException("Employee not updated");
    }
  }

  public void password(Connection c, long id, String hash) throws SQLException {
    try (PreparedStatement s =
        c.prepareStatement("UPDATE NHAN_VIEN SET MAT_KHAU_HASH=? WHERE MA_NV=?")) {
      s.setString(1, hash);
      s.setLong(2, id);
      if (s.executeUpdate() != 1) throw new SQLException("Employee not updated");
    }
  }
}
