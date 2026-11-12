package com.nhomkha.assignment_java.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nhomkha.assignment_java.repository.EmployeeRepository;
import java.sql.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class EmployeeAuthenticationTests {
  @Test
  void cashierSessionRemainsValidUntilRoleChanges() throws Exception {
    var employee = new com.nhomkha.assignment_java.model.Employee(
        2, "Thu ngân", "", "thungan1", "test-hash", "THU_NGAN", true);
    var principal = new EmployeePrincipal(employee);
    var repo = mock(EmployeeRepository.class);
    when(repo.byUsername("thungan1")).thenReturn(java.util.Optional.of(employee));
    var context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
    context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
        principal, null, principal.getAuthorities()));
    org.springframework.security.core.context.SecurityContextHolder.setContext(context);
    try {
      var filter = new AccountStateFilter(repo);
      var chain = mock(jakarta.servlet.FilterChain.class);
      var request = new org.springframework.mock.web.MockHttpServletRequest();
      var response = new org.springframework.mock.web.MockHttpServletResponse();
      filter.doFilter(request, response, chain);
      verify(chain).doFilter(request, response);
      assertNull(response.getRedirectedUrl());

      employee.setRole("ADMIN");
      var changedResponse = new org.springframework.mock.web.MockHttpServletResponse();
      filter.doFilter(new org.springframework.mock.web.MockHttpServletRequest(), changedResponse, chain);
      assertEquals("/login?expired", changedResponse.getRedirectedUrl());
    } finally {
      org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
  }

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
    when(result.getString("VAI_TRO")).thenReturn("THU_NGAN");
    when(result.getString("TRANG_THAI")).thenReturn("INACTIVE", "ACTIVE");
    var repository = new EmployeeRepository(ds);
    assertFalse(new EmployeePrincipal(repository.byUsername("staff").orElseThrow()).isEnabled());
    assertTrue(new EmployeePrincipal(repository.byUsername("staff").orElseThrow()).isEnabled());
  }
}
