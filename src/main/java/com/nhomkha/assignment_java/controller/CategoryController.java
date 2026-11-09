package com.nhomkha.assignment_java.controller;

import com.nhomkha.assignment_java.dto.CategoryForm;
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
@RequestMapping("/categories")
public class CategoryController {
  private final CatalogService catalog;

  public CategoryController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping
  String list(@RequestParam(defaultValue = "") String q, Model m) throws SQLException {
    m.addAttribute("rows", catalog.categories());
    m.addAttribute("q", q);
    return "categories/list";
  }

  @GetMapping("/new")
  String create(Model m) {
    m.addAttribute("form", new CategoryForm());
    return "categories/form";
  }

  @GetMapping("/{id}/edit")
  String edit(@PathVariable long id, Model m) throws SQLException {
    var row = catalog.category(id);
    var f = new CategoryForm();
    f.setName(row.getName());
    f.setDescription(row.getDescription());
    m.addAttribute("id", id);
    m.addAttribute("form", f);
    return "categories/form";
  }

  @PostMapping({"", "/{id}/edit"})
  String save(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") CategoryForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    if (!errors.hasErrors())
      try {
        catalog.saveCategory(id, f);
        flash.addFlashAttribute("success", "Đã lưu thông tin.");
        return "redirect:/categories";
      } catch (BusinessException ex) {
        errors.reject("business", ex.getMessage());
      } catch (SQLException ex) {
        errors.reject("database", ViewAdvice.sqlMessage(ex));
      }
    m.addAttribute("id", id);
    return "categories/form";
  }
}
