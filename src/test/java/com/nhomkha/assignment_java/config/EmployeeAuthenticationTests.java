package com.nhomkha.assignment_java.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nhomkha.assignment_java.repository.EmployeeRepository;
import java.sql.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class EmployeeAuthenticationTests {
  @Test
  void configuredEncoderRequiresBcrypt() {
    var encoder = new SecurityConfig().passwordEncoder();
    String hash = encoder.encode("test-password");
    assertTrue(encoder.matches("test-password", hash));
    assertFalse(encoder.matches("wrong-password", hash));
    assertFalse(encoder.matches("test-password", "test-password"));
    assertFalse(encoder.matches(hash, hash));
  }

  @Test
  void databaseStatusControlsWhetherPrincipalIsEnabled() throws Exception {
    DataSource ds = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    PreparedStatement statement = mock(PreparedStatement.class);
    ResultSet result = mock(ResultSet.class);
    when(ds.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(result);
    when(result.next()).thenReturn(true);
    when(result.getString("TEN_DANG_NHAP")).thenReturn("staff");
    when(result.getString("MAT_KHAU")).thenReturn("test-hash");
    when(result.getString("VAI_TRO")).thenReturn("STAFF");
    when(result.getString("TRANG_THAI")).thenReturn("INACTIVE", "ACTIVE");
    var repository = new EmployeeRepository(ds);
    assertFalse(new EmployeePrincipal(repository.byUsername("staff").orElseThrow()).isEnabled());
    assertTrue(new EmployeePrincipal(repository.byUsername("staff").orElseThrow()).isEnabled());
  }
}
