package com.nhomkha.assignment_java.service;

import java.sql.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

/** Shared only by multi-statement business transactions. Repositories never own the connection. */
@Component
public class JdbcTransactions {
  private final DataSource dataSource;

  public JdbcTransactions(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @FunctionalInterface
  public interface Work<T> {
    T run(Connection connection) throws SQLException;
  }

  public <T> T run(Work<T> work) throws SQLException {
    try (Connection c = dataSource.getConnection()) {
      boolean previous = c.getAutoCommit();
      Throwable primary = null;
      boolean completed = false;
      try {
        c.setAutoCommit(false);
        T value = work.run(c);
        c.commit();
        completed = true;
        return value;
      } catch (SQLException | RuntimeException | Error ex) {
        primary = ex;
        try {
          c.rollback();
          completed = true;
        } catch (SQLException rollback) {
          ex.addSuppressed(rollback);
        }
        throw ex;
      } finally {
        // Never enable autoCommit after a failed rollback (it might commit partial work).
        if (completed) {
          try {
            c.setAutoCommit(previous);
          } catch (SQLException reset) {
            if (primary != null) primary.addSuppressed(reset);
            else throw reset;
          }
        } else {
          try {
            c.abort(Runnable::run);
          } catch (SQLException abort) {
            if (primary != null) primary.addSuppressed(abort);
            else throw abort;
          }
        }
      }
    }
  }
}
