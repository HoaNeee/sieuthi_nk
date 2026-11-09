package com.nhomkha.assignment_java.controller;

import com.nhomkha.assignment_java.dto.CustomerForm;
import com.nhomkha.assignment_java.exception.BusinessException;
import com.nhomkha.assignment_java.service.CatalogService;
import jakarta.validation.Valid;
import java.sql.SQLException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {
  private final CatalogService catalog;

  public CustomerController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping
  String list(@RequestParam(defaultValue = "") String q, Model m) throws SQLException {
    m.addAttribute("rows", catalog.customers(q));
    m.addAttribute("q", q);
    return "customers/list";
  }

  @GetMapping("/new")
  String create(Model m) {
    m.addAttribute("form", new CustomerForm());
    return "customers/form";
  }

  @GetMapping("/{id}/edit")
  String edit(@PathVariable long id, Model m) throws SQLException {
    var row = catalog.customer(id);
    var f = new CustomerForm();
    f.setName(row.getName());
    f.setPhone(row.getPhone());
    m.addAttribute("id", id);
    m.addAttribute("form", f);
    return "customers/form";
  }

  @PostMapping({"", "/{id}/edit"})
  String save(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") CustomerForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    if (!errors.hasErrors())
      try {
        catalog.saveCustomer(id, f);
        flash.addFlashAttribute("success", "Đã lưu thông tin.");
        return "redirect:/customers";
      } catch (BusinessException ex) {
        errors.reject("business", ex.getMessage());
      } catch (SQLException ex) {
        errors.reject("database", ViewAdvice.sqlMessage(ex));
      }
    m.addAttribute("id", id);
    return "customers/form";
  }
}
