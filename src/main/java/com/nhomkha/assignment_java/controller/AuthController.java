package com.nhomkha.assignment_java.controller;

import com.nhomkha.assignment_java.config.EmployeePrincipal;
import com.nhomkha.assignment_java.dto.PasswordForm;
import com.nhomkha.assignment_java.exception.BusinessException;
import com.nhomkha.assignment_java.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.sql.SQLException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
  private final EmployeeService employees;

  public AuthController(EmployeeService employees) {
    this.employees = employees;
  }

  @GetMapping("/")
  String home() {
    return "redirect:/products";
  }

  @GetMapping("/login")
  String login() {
    return "auth/login";
  }

  @GetMapping("/password")
  String password(Model model) {
    model.addAttribute("form", new PasswordForm());
    return "auth/password";
  }

  @PostMapping("/password")
  String change(
      @Valid @ModelAttribute("form") PasswordForm form,
      BindingResult errors,
      @AuthenticationPrincipal EmployeePrincipal actor,
      HttpServletRequest request)
      throws SQLException, jakarta.servlet.ServletException {
    if (!errors.hasErrors())
      try {
        employees.changePassword(actor, form);
        request.logout();
        request.getSession().invalidate();
        return "redirect:/login?changed";
      } catch (BusinessException ex) {
        errors.reject("business", ex.getMessage());
      }
    return "auth/password";
  }
}
