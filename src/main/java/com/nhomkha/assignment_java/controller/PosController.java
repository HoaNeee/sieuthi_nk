package com.nhomkha.assignment_java.controller;

import com.nhomkha.assignment_java.config.EmployeePrincipal;
import com.nhomkha.assignment_java.dto.CheckoutForm;
import com.nhomkha.assignment_java.exception.BusinessException;
import com.nhomkha.assignment_java.model.*;
import com.nhomkha.assignment_java.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller

public class PosController {
  private final CatalogService catalog;
  private final TradingService trading;

  public PosController(CatalogService catalog, TradingService trading) {
    this.catalog = catalog;
    this.trading = trading;
  }

  private Cart cart(HttpSession session, EmployeePrincipal actor) {
    synchronized (session) {
      var value = session.getAttribute("cart");
      if (value instanceof Cart c && c.getEmployeeId() == actor.getId()) return c;
      Cart c = new Cart(actor.getId());
      session.setAttribute("cart", c);
      return c;
    }
  }

  @GetMapping({"/products", "/pos"})
  String catalog(@RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) Long category,
      @RequestParam(defaultValue = "0") int page,
      HttpSession session, @AuthenticationPrincipal EmployeePrincipal actor, Model m) throws SQLException {
    page = Math.max(0, page);
    var products = catalog.search(q, category, page);
    m.addAttribute("products", products.size() > 20 ? products.subList(0, 20) : products);
    m.addAttribute("hasNext", products.size() > 20);
    m.addAttribute("page", page);
    m.addAttribute("q", q);
    m.addAttribute("category", category);
    return view(session, actor, m);
  }

  String view(HttpSession session, @AuthenticationPrincipal EmployeePrincipal actor, Model m)
      throws SQLException {
    if (!m.containsAttribute("products")) {
      var products = catalog.search("", null, 0);
      m.addAttribute("products", products.size() > 20 ? products.subList(0, 20) : products);
      m.addAttribute("hasNext", products.size() > 20);
      m.addAttribute("page", 0);
      m.addAttribute("q", "");
    }
    m.addAttribute("categories", catalog.categories());
    if (!m.containsAttribute("customerForm")) m.addAttribute("customerForm", new com.nhomkha.assignment_java.dto.CustomerForm());
    var rows = new ArrayList<Map<String, Object>>();
    BigDecimal total = BigDecimal.ZERO;
    for (var entry : cart(session, actor).snapshot().entrySet()) {
      try {
        Product p = catalog.product(entry.getKey());
        BigDecimal amount = p.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
        total = total.add(amount);
        rows.add(
            Map.of(
                "id",
                p.getId(),
                "name",
                p.getName(),
                "price",
                p.getPrice(),
                "quantity",
                entry.getValue(),
                "amount",
                amount,
                "stock",
                p.getStock()));
      } catch (BusinessException ex) {
        rows.add(
            Map.of(
                "id",
                entry.getKey(),
                "name",
                "Sản phẩm không còn tồn tại — hãy xóa dòng này",
                "price",
                BigDecimal.ZERO,
                "quantity",
                entry.getValue(),
                "amount",
                BigDecimal.ZERO,
                "stock",
                0));
      }
    }
    m.addAttribute("rows", rows);
    m.addAttribute("total", total);
    m.addAttribute("customers", catalog.customers(""));
    if (!m.containsAttribute("form")) {
      var f = new CheckoutForm();
      f.setPayment("TIEN_MAT");
      m.addAttribute("form", f);
    }
    return "products/list";
  }

  @PostMapping("/pos/customers")
  String quickCustomer(@Valid @ModelAttribute("customerForm") com.nhomkha.assignment_java.dto.CustomerForm f,
      BindingResult errors, HttpSession session, @AuthenticationPrincipal EmployeePrincipal actor,
      Model m, RedirectAttributes flash) throws SQLException {
    if (!errors.hasErrors()) try {
      long id = catalog.createCustomer(f);
      var checkout = new CheckoutForm();
      checkout.setCustomerId(id);
      checkout.setPayment("TIEN_MAT");
      flash.addFlashAttribute("form", checkout);
      flash.addFlashAttribute("success", "Đã thêm và chọn khách hàng mới.");
      return "redirect:/products#checkout";
    } catch (SQLException ex) { errors.reject("database", ViewAdvice.sqlMessage(ex)); }
      catch (BusinessException ex) { errors.reject("business", ex.getMessage()); }
    return view(session, actor, m);
  }

  @PostMapping("/pos/items")
  String add(
      @RequestParam long productId,
      @RequestParam(defaultValue = "1") int quantity,
      HttpSession session,
      @AuthenticationPrincipal EmployeePrincipal actor,
      RedirectAttributes flash) {
    try {
      catalog.product(productId);
      cart(session, actor).add(productId, quantity);
      flash.addFlashAttribute("success", "Đã thêm vào giỏ.");
    } catch (BusinessException ex) {
      flash.addFlashAttribute("error", ex.getMessage());
    } catch (SQLException ex) {
      flash.addFlashAttribute("error", ViewAdvice.sqlMessage(ex));
    }
    return "redirect:/products#checkout";
  }

  @PostMapping("/pos/items/{id}")
  String update(
      @PathVariable long id,
      @RequestParam int quantity,
      HttpSession session,
      @AuthenticationPrincipal EmployeePrincipal actor,
      RedirectAttributes flash) {
    try {
      cart(session, actor).update(id, quantity);
    } catch (BusinessException ex) {
      flash.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/products#checkout";
  }

  @PostMapping("/pos/items/{id}/remove")
  String remove(
      @PathVariable long id,
      HttpSession session,
      @AuthenticationPrincipal EmployeePrincipal actor) {
    cart(session, actor).remove(id);
    return "redirect:/products#checkout";
  }

  @PostMapping("/pos/checkout")
  String checkout(
      @Valid @ModelAttribute("form") CheckoutForm f,
      BindingResult errors,
      HttpSession session,
      @AuthenticationPrincipal EmployeePrincipal actor,
      Model m)
      throws SQLException {
    if (!errors.hasErrors())
      try {
        Cart cart = cart(session, actor);
        long id;
        // Prevent two simultaneous POSTs for the same session from issuing two invoices.
        synchronized (cart) {
          id = trading.checkout(cart.snapshot(), f, actor);
          cart.clear();
        }
        return "redirect:/invoices/" + id;
      } catch (BusinessException ex) {
        errors.reject("business", ex.getMessage());
      } catch (SQLException ex) {
        errors.reject("database", ViewAdvice.sqlMessage(ex));
      }
    return view(session, actor, m);
  }
}
