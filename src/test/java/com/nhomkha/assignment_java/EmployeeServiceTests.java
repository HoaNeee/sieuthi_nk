package com.nhomkha.assignment_java;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nhomkha.assignment_java.config.EmployeePrincipal;
import com.nhomkha.assignment_java.dto.EmployeeForm;
import com.nhomkha.assignment_java.exception.BusinessException;
import com.nhomkha.assignment_java.model.Employee;
import com.nhomkha.assignment_java.repository.EmployeeRepository;
import com.nhomkha.assignment_java.service.*;
import java.sql.*;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class EmployeeServiceTests {
  Connection c;
  EmployeeRepository repo;
  EmployeeService service;
  Employee admin = new Employee(1, "Admin", "", "admin", "oldHash", "ADMIN", true);

  @BeforeEach
  void setup() throws Exception {
    var ds = mock(DataSource.class);
    c = mock(Connection.class);
    repo = mock(EmployeeRepository.class);
    when(ds.getConnection()).thenReturn(c);
    when(c.getAutoCommit()).thenReturn(true);
    when(repo.byId(c, 1)).thenReturn(Optional.of(admin));
    when(repo.activeAdmins(c)).thenReturn(1);
    service = new EmployeeService(repo, new JdbcTransactions(ds), new BCryptPasswordEncoder());
  }

  EmployeeForm form() {
    var f = new EmployeeForm();
    f.setName("Admin");
    f.setUsername("admin");
    f.setRole("ADMIN");
    f.setActive(true);
    return f;
  }

  @Test
  void lastAdminCannotBeLocked() throws Exception {
    var f = form();
    f.setActive(false);
    assertThrows(BusinessException.class, () -> service.save(1L, f, new EmployeePrincipal(admin)));
    verify(repo).lockAccounts(c);
    verify(repo, never()).save(any(), any(), any(), any());
    verify(c).rollback();
  }

  @Test
  void lastAdminCannotBeDemoted() throws Exception {
    var f = form();
    f.setRole("STAFF");
    assertThrows(BusinessException.class, () -> service.save(1L, f, new EmployeePrincipal(admin)));
    verify(c).rollback();
  }

  @Test
  void anotherActiveAdminAllowsDemotion() throws Exception {
    when(repo.activeAdmins(c)).thenReturn(2);
    var f = form();
    f.setRole("STAFF");
    service.save(1L, f, new EmployeePrincipal(admin));
    verify(repo).save(c, 1L, f, "oldHash");
    verify(c).commit();
  }

  @Test
  void newEmployeePasswordIsHashed() throws Exception {
    var f = form();
    f.setUsername("staff");
    f.setRole("STAFF");
    f.setPassword("testing-only-123");
    service.save(null, f, new EmployeePrincipal(admin));
    var hash = org.mockito.ArgumentCaptor.forClass(String.class);
    verify(repo).save(eq(c), isNull(), eq(f), hash.capture());
    assertNotEquals(f.getPassword(), hash.getValue());
    assertTrue(new BCryptPasswordEncoder().matches(f.getPassword(), hash.getValue()));
  }

  @Test
  void rejectsPasswordOverBcryptByteLimit() {
    assertThrows(BusinessException.class, () -> EmployeeService.validatePassword("ế".repeat(30)));
  }
}
