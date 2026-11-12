package com.nhomkha.assignment_java.controller;

import com.nhomkha.assignment_java.dto.ProductForm;
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
@RequestMapping("/products")
public class ProductController {
  private final CatalogService catalog;

  public ProductController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping("/new")
  String create(Model m) throws SQLException {
    m.addAttribute("form", new ProductForm());
    return form(m, null);
  }

  @GetMapping("/{id}/edit")
  String edit(@PathVariable long id, Model m) throws SQLException {
    var p = catalog.product(id);
    var f = new ProductForm();
    f.setName(p.getName());
    f.setUnit(p.getUnit());
    f.setPurchasePrice(p.getPurchasePrice());
    f.setBarcode(p.getBarcode());
    f.setPrice(p.getPrice());
    f.setCategoryId(p.getCategoryId());
    f.setSupplierId(p.getSupplierId());
    f.setExpiry(p.getExpiry());
    m.addAttribute("form", f);
    return form(m, id);
  }

  @PostMapping({"", "/{id}/edit"})
  String save(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") ProductForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash)
      throws SQLException {
    if (!errors.hasErrors())
      try {
        catalog.saveProduct(id, f);
        flash.addFlashAttribute("success", "Đã lưu sản phẩm.");
        return "redirect:/products";
      } catch (BusinessException ex) {
        errors.reject("business", ex.getMessage());
      } catch (SQLException ex) {
        errors.reject("database", ViewAdvice.sqlMessage(ex));
      }
    return form(m, id);
  }

  private String form(Model m, Long id) throws SQLException {
    m.addAttribute("id", id);
    m.addAttribute("categories", catalog.categories());
    m.addAttribute("suppliers", catalog.suppliers());
    return "products/form";
  }
}
