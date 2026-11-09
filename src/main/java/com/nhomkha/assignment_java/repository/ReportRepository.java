package com.nhomkha.assignment_java.repository;

import com.nhomkha.assignment_java.model.ReportRow;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class ReportRepository {
  private final DataSource ds;

  public ReportRepository(DataSource ds) {
    this.ds = ds;
  }

  public List<ReportRow> revenue(LocalDate from, LocalDate to) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s =
            c.prepareStatement(
                "SELECT TO_CHAR(TRUNC(NGAY_LAP),'YYYY-MM-DD'),SUM(TONG_TIEN),COUNT(*) FROM HOA_DON"
                    + " WHERE NGAY_LAP>=? AND NGAY_LAP<? GROUP BY TRUNC(NGAY_LAP) ORDER BY"
                    + " TRUNC(NGAY_LAP)")) {
      s.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
      s.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
      return rows(s);
    }
  }

  public List<ReportRow> sales(LocalDate from, LocalDate to) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s =
            c.prepareStatement(
                "SELECT p.TEN_SP,SUM(ct.SO_LUONG*ct.DON_GIA),SUM(ct.SO_LUONG) FROM CT_HOA_DON ct"
                    + " JOIN HOA_DON h ON h.MA_HD=ct.MA_HD JOIN SAN_PHAM p ON p.MA_SP=ct.MA_SP"
                    + " WHERE h.NGAY_LAP>=? AND h.NGAY_LAP<? GROUP BY p.MA_SP,p.TEN_SP ORDER BY"
                    + " SUM(ct.SO_LUONG*ct.DON_GIA) DESC")) {
      s.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
      s.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
      return rows(s);
    }
  }

  public List<ReportRow> stock() throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement s =
            c.prepareStatement(
                "SELECT TEN_SP,GIA_BAN*SO_LUONG_TON,SO_LUONG_TON FROM SAN_PHAM ORDER BY"
                    + " SO_LUONG_TON,MA_SP")) {
      return rows(s);
    }
  }

  private List<ReportRow> rows(PreparedStatement s) throws SQLException {
    try (ResultSet r = s.executeQuery()) {
      List<ReportRow> a = new ArrayList<>();
      while (r.next()) a.add(new ReportRow(r.getString(1), r.getBigDecimal(2), r.getLong(3)));
      return a;
    }
  }
}
