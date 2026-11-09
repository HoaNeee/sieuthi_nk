package com.nhomkha.assignment_java.controller;

import com.nhomkha.assignment_java.dto.SupplierForm;
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
@RequestMapping("/suppliers")
public class SupplierController {
  private final CatalogService catalog;

  public SupplierController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping
  String list(@RequestParam(defaultValue = "") String q, Model m) throws SQLException {
    m.addAttribute("rows", catalog.suppliers());
    m.addAttribute("q", q);
    return "suppliers/list";
  }

  @GetMapping("/new")
  String create(Model m) {
    m.addAttribute("form", new SupplierForm());
    return "suppliers/form";
  }

  @GetMapping("/{id}/edit")
  String edit(@PathVariable long id, Model m) throws SQLException {
    var row = catalog.supplier(id);
    var f = new SupplierForm();
    f.setName(row.getName());
    f.setPhone(row.getPhone());
    f.setAddress(row.getAddress());
    f.setEmail(row.getEmail());
    m.addAttribute("id", id);
    m.addAttribute("form", f);
    return "suppliers/form";
  }

  @PostMapping({"", "/{id}/edit"})
  String save(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") SupplierForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    if (!errors.hasErrors())
      try {
        catalog.saveSupplier(id, f);
        flash.addFlashAttribute("success", "Đã lưu thông tin.");
        return "redirect:/suppliers";
      } catch (BusinessException ex) {
        errors.reject("business", ex.getMessage());
      } catch (SQLException ex) {
        errors.reject("database", ViewAdvice.sqlMessage(ex));
      }
    m.addAttribute("id", id);
    return "suppliers/form";
  }
}
