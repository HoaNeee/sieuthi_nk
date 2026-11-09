package com.nhomkha.assignment_java.config;

import java.sql.*;
import java.util.*;

/** Read-only connection verification, explicitly invoked from a terminal. */
public class DatabaseCheckTool {
  public static void main(String[] args) throws Exception {
    Properties local = new Properties();
    var envFile = java.nio.file.Path.of(".env");
    if (java.nio.file.Files.exists(envFile)) {
      try (var input = java.nio.file.Files.newInputStream(envFile)) {
        local.load(input);
      }
    }
    String password = System.getenv().getOrDefault("DB_PASSWORD", local.getProperty("DB_PASSWORD"));
    if (password == null || password.isBlank())
      throw new IllegalStateException("Set DB_PASSWORD in the local .env file or environment first.");
    String url =
        System.getenv().getOrDefault("DB_URL", local.getProperty("DB_URL", "jdbc:oracle:thin:@//localhost:1521/FREEPDB1"));
    String user = System.getenv().getOrDefault("DB_USERNAME", local.getProperty("DB_USERNAME", "sieuthi"));
    if (!user.equalsIgnoreCase("sieuthi"))
      throw new IllegalStateException(
          "Use application schema SIEUTHI, not an administrative user.");
    try (Connection c = DriverManager.getConnection(url, user, password)) {
      System.out.println("Oracle version: " + c.getMetaData().getDatabaseProductVersion());
      try (PreparedStatement s =
              c.prepareStatement(
                  "SELECT USER, SYS_CONTEXT('USERENV','CON_NAME'), DEFAULT_TABLESPACE FROM"
                      + " USER_USERS");
          ResultSet r = s.executeQuery()) {
        if (!r.next()) throw new IllegalStateException("Cannot read schema information.");
        System.out.printf(
            "USER=%s, PDB=%s, TABLESPACE=%s%n", r.getString(1), r.getString(2), r.getString(3));
        if (!"SIEUTHI".equals(r.getString(1))
            || !"FREEPDB1".equals(r.getString(2))
            || !"TS_SIEUTHI".equals(r.getString(3)))
          throw new IllegalStateException(
              "Connection does not match the expected application schema.");
      }
      Set<String> expected =
          new TreeSet<>(
              Set.of(
                  "DANH_MUC",
                  "SAN_PHAM",
                  "NHA_CUNG_CAP",
                  "NHAN_VIEN",
                  "KHACH_HANG",
                  "PHIEU_NHAP",
                  "CT_PHIEU_NHAP",
                  "HOA_DON",
                  "CT_HOA_DON"));
      try (PreparedStatement s =
              c.prepareStatement("SELECT TABLE_NAME FROM USER_TABLES ORDER BY TABLE_NAME");
          ResultSet r = s.executeQuery()) {
        while (r.next()) {
          String table = r.getString(1);
          System.out.println(table);
          expected.remove(table);
        }
      }
      if (!expected.isEmpty()) throw new IllegalStateException("Missing tables: " + expected);
      System.out.println("Connection and table names verified. No data was modified.");
    } catch (SQLException ex) {
      System.err.println(
          "Oracle check failed. Error code: "
              + ex.getErrorCode()
              + ". Check listener, PDB, credentials and schema.");
      System.exit(1);
    }
  }
}
