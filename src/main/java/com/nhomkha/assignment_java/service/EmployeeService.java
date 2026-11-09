package com.nhomkha.assignment_java.service;

import com.nhomkha.assignment_java.config.EmployeePrincipal;
import com.nhomkha.assignment_java.dto.*;
import com.nhomkha.assignment_java.exception.BusinessException;
import com.nhomkha.assignment_java.model.Employee;
import com.nhomkha.assignment_java.repository.EmployeeRepository;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
  private final EmployeeRepository repo;
  private final JdbcTransactions tx;
  private final PasswordEncoder encoder;

  public EmployeeService(EmployeeRepository repo, JdbcTransactions tx, PasswordEncoder encoder) {
    this.repo = repo;
    this.tx = tx;
    this.encoder = encoder;
  }

  public List<Employee> all() throws SQLException {
    return repo.all();
  }

  public Employee get(long id) throws SQLException {
    return tx.run(
        c ->
            repo.byId(c, id).orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên.")));
  }

  public void save(Long id, EmployeeForm f, EmployeePrincipal actor) throws SQLException {
    tx.run(
        c -> {
          repo.lockAccounts(c);
          Employee current =
              repo.byId(c, actor.getId())
                  .orElseThrow(() -> new BusinessException("Tài khoản không còn tồn tại."));
          if (!current.isActive() || !current.getRole().equals("ADMIN"))
            throw new BusinessException("Bạn không có quyền quản lý nhân viên.");
          Employee old =
              id == null
                  ? null
                  : repo.byId(c, id)
                      .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên."));
          if (old != null
              && old.isActive()
              && old.getRole().equals("ADMIN")
              && (!f.getActive() || !f.getRole().equals("ADMIN"))
              && repo.activeAdmins(c) <= 1)
            throw new BusinessException("Không thể khóa hoặc hạ quyền ADMIN hoạt động cuối cùng.");
          String hash = old == null ? null : old.getPasswordHash();
          if (f.getPassword() != null && !f.getPassword().isBlank()) {
            validatePassword(f.getPassword());
            hash = encoder.encode(f.getPassword());
          }
          if (hash == null) throw new BusinessException("Nhân viên mới cần mật khẩu từ 10 ký tự.");
          repo.save(c, id, f, hash);
          return null;
        });
  }

  public void changePassword(EmployeePrincipal actor, PasswordForm f) throws SQLException {
    validatePassword(f.getNewPassword());
    tx.run(
        c -> {
          repo.lockAccounts(c);
          Employee e =
              repo.byId(c, actor.getId())
                  .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản."));
          if (!e.isActive() || !encoder.matches(f.getCurrentPassword(), e.getPasswordHash()))
            throw new BusinessException("Mật khẩu hiện tại không đúng.");
          repo.password(c, e.getId(), encoder.encode(f.getNewPassword()));
          return null;
        });
  }

  public static void validatePassword(String value) {
    if (value == null || value.length() < 10 || value.getBytes(StandardCharsets.UTF_8).length > 72)
      throw new BusinessException("Mật khẩu cần ít nhất 10 ký tự và tối đa 72 byte UTF-8.");
  }
}
