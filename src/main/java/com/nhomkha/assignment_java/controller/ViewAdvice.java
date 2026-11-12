package com.nhomkha.assignment_java.controller;

import com.nhomkha.assignment_java.config.EmployeePrincipal;
import com.nhomkha.assignment_java.exception.BusinessException;
import java.sql.SQLException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class ViewAdvice {
  @ModelAttribute
  public void common(Authentication auth, Model model) {
    if (auth != null && auth.getPrincipal() instanceof EmployeePrincipal p) {
      model.addAttribute("admin", p.isAdmin());
      model.addAttribute("loginName", p.getUsername());
    }
  }

  @ExceptionHandler(BusinessException.class)
  @org.springframework.web.bind.annotation.ResponseStatus(
      org.springframework.http.HttpStatus.BAD_REQUEST)
  public String business(BusinessException ex, Model model) {
    model.addAttribute("message", ex.getMessage());
    return "error";
  }

  @ExceptionHandler(SQLException.class)
  @org.springframework.web.bind.annotation.ResponseStatus(
      org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
  public String database(SQLException ex, Model model) {
    // Never expose SQL text, connection strings or credentials in HTML.
    model.addAttribute("message", sqlMessage(ex));
    return "error";
  }

  public static String sqlMessage(SQLException ex) {
    org.slf4j.LoggerFactory.getLogger(ViewAdvice.class).warn("Database operation failed: code={}, state={}", ex.getErrorCode(), ex.getSQLState());
    if (ex.getErrorCode() == 2289) return "Thiếu sequence trong database. Vui lòng kiểm tra cấu trúc bảng và cơ chế sinh mã.";
    if (ex.getErrorCode() == 1)
      return "Dữ liệu bị trùng (barcode hoặc tên đăng nhập). Vui lòng kiểm tra.";
    if (ex.getErrorCode() == 2291)
      return "Danh mục, nhà cung cấp hoặc khách hàng không còn tồn tại.";
    return "Không thể hoàn tất thao tác dữ liệu. Vui lòng kiểm tra kết nối Oracle và thử lại.";
  }
}
