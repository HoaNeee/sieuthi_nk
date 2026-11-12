package com.nhomkha.assignment_java.controller;

import com.nhomkha.assignment_java.config.EmployeePrincipal;
import com.nhomkha.assignment_java.dto.EmployeeForm;
import com.nhomkha.assignment_java.exception.BusinessException;
import com.nhomkha.assignment_java.service.EmployeeService;
import jakarta.validation.Valid;
import java.sql.SQLException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {
  private final EmployeeService employees;

  public EmployeeController(EmployeeService employees) {
    this.employees = employees;
  }

  @GetMapping
  String list(Model m) throws SQLException {
    m.addAttribute("rows", employees.all());
    return "employees/list";
  }

  @GetMapping("/new")
  String create(Model m) {
    var f = new EmployeeForm();
    f.setRole("THU_NGAN");
    m.addAttribute("form", f);
    return "employees/form";
  }

  @GetMapping("/{id}/edit")
  String edit(@PathVariable long id, Model m) throws SQLException {
    var e = employees.get(id);
    var f = new EmployeeForm();
    f.setName(e.getName());
    f.setTitle(e.getTitle());
    f.setUsername(e.getUsername());
    f.setRole(e.getRole());
    f.setActive(e.isActive());
    m.addAttribute("form", f);
    m.addAttribute("id", id);
    return "employees/form";
  }

  @PostMapping({"", "/{id}/edit"})
  String save(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") EmployeeForm f,
      BindingResult errors,
      @AuthenticationPrincipal EmployeePrincipal actor,
      Model m,
      RedirectAttributes flash) {
    if (!errors.hasErrors())
      try {
        employees.save(id, f, actor);
        flash.addFlashAttribute(
            "success", "Đã lưu nhân viên. Tài khoản bị thay đổi quyền/mật khẩu cần đăng nhập lại.");
        return "redirect:/employees";
      } catch (BusinessException ex) {
        errors.reject("business", ex.getMessage());
      } catch (SQLException ex) {
        errors.reject("database", ViewAdvice.sqlMessage(ex));
      }
    f.setPassword(null);
    m.addAttribute("id", id);
    return "employees/form";
  }
}
