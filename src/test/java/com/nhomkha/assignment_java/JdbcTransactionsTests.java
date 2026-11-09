package com.nhomkha.assignment_java;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nhomkha.assignment_java.service.JdbcTransactions;
import java.sql.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class JdbcTransactionsTests {
  @Test
  void commitsAndRestoresConnection() throws Exception {
    DataSource ds = mock(DataSource.class);
    Connection c = mock(Connection.class);
    when(ds.getConnection()).thenReturn(c);
    when(c.getAutoCommit()).thenReturn(true);
    assertEquals(
        "ok",
        new JdbcTransactions(ds)
            .run(
                conn -> {
                  assertSame(c, conn);
                  return "ok";
                }));
    var order = inOrder(c);
    order.verify(c).getAutoCommit();
    order.verify(c).setAutoCommit(false);
    order.verify(c).commit();
    order.verify(c).setAutoCommit(true);
    order.verify(c).close();
    verify(c, never()).rollback();
  }

  @Test
  void rollbackPreservesRuntimeErrorEvenIfResetFails() throws Exception {
    DataSource ds = mock(DataSource.class);
    Connection c = mock(Connection.class);
    when(ds.getConnection()).thenReturn(c);
    when(c.getAutoCommit()).thenReturn(true);
    doThrow(new SQLException("reset")).when(c).setAutoCommit(true);
    RuntimeException original = new RuntimeException("business");
    var thrown =
        assertThrows(
            RuntimeException.class,
            () ->
                new JdbcTransactions(ds)
                    .run(
                        conn -> {
                          throw original;
                        }));
    assertSame(original, thrown);
    assertEquals(1, thrown.getSuppressed().length);
    verify(c).rollback();
    verify(c).close();
    verify(c, never()).commit();
  }

  @Test
  void rollbackFailureAbortsWithoutEnablingAutoCommit() throws Exception {
    DataSource ds = mock(DataSource.class);
    Connection c = mock(Connection.class);
    when(ds.getConnection()).thenReturn(c);
    when(c.getAutoCommit()).thenReturn(true);
    doThrow(new SQLException("rollback")).when(c).rollback();
    SQLException original = new SQLException("insert");
    assertSame(
        original,
        assertThrows(
            SQLException.class,
            () ->
                new JdbcTransactions(ds)
                    .run(
                        conn -> {
                          throw original;
                        })));
    verify(c, never()).setAutoCommit(true);
    verify(c).abort(any());
    verify(c).close();
    assertEquals(1, original.getSuppressed().length);
  }
}
