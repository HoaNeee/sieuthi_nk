package com.nhomkha.assignment_java.controller;

import com.nhomkha.assignment_java.config.EmployeePrincipal;
import com.nhomkha.assignment_java.dto.*;
import com.nhomkha.assignment_java.exception.BusinessException;
import com.nhomkha.assignment_java.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.sql.SQLException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class DocumentController {
  private final TradingService trading;
  private final CatalogService catalog;

  public DocumentController(TradingService trading, CatalogService catalog) {
    this.trading = trading;
    this.catalog = catalog;
  }

  @GetMapping("/invoices")
  String invoices(@AuthenticationPrincipal EmployeePrincipal actor, Model m) throws SQLException {
    m.addAttribute("rows", trading.invoices(actor));
    m.addAttribute("kind", "invoices");
    return "documents/list";
  }

  @GetMapping("/invoices/{id}")
  String invoice(@PathVariable long id, @AuthenticationPrincipal EmployeePrincipal actor, Model m)
      throws SQLException {
    m.addAttribute("doc", trading.invoice(id, actor));
    m.addAttribute("lines", trading.invoiceLines(id, actor));
    m.addAttribute("kind", "invoices");
    return "documents/detail";
  }

  @GetMapping("/imports")
  String imports(Model m) throws SQLException {
    m.addAttribute("rows", trading.imports());
    m.addAttribute("kind", "imports");
    return "documents/list";
  }

  @GetMapping("/imports/{id}")
  String stockImport(@PathVariable long id, Model m) throws SQLException {
    m.addAttribute("doc", trading.stockImport(id));
    m.addAttribute("lines", trading.importLines(id));
    m.addAttribute("kind", "imports");
    return "documents/detail";
  }

  @GetMapping("/imports/new")
  String newImport(Model m, HttpSession session) throws SQLException {
    var f = new ImportForm();
    f.getLines().add(new ImportLineForm());
    m.addAttribute("form", f);
    String token = java.util.UUID.randomUUID().toString();
    session.setAttribute("importToken", token);
    m.addAttribute("token", token);
    return importForm(m);
  }

  @PostMapping("/imports")
  String saveImport(
      @Valid @ModelAttribute("form") ImportForm f,
      BindingResult errors,
      @RequestParam String token,
      HttpSession session,
      @AuthenticationPrincipal EmployeePrincipal actor,
      Model m)
      throws SQLException {
    if (!errors.hasErrors())
      try {
        long id;
        synchronized (session) {
          if (!token.equals(session.getAttribute("importToken")))
            throw new BusinessException(
                "Phiếu đã được gửi hoặc form đã hết hạn. Mở phiếu mới để tiếp tục.");
          id = trading.stockImport(f, actor);
          session.removeAttribute("importToken");
        }
        return "redirect:/imports/" + id;
      } catch (BusinessException ex) {
        errors.reject("business", ex.getMessage());
      } catch (SQLException ex) {
        errors.reject("database", ViewAdvice.sqlMessage(ex));
      }
    m.addAttribute("token", token);
    return importForm(m);
  }

  private String importForm(Model m) throws SQLException {
    m.addAttribute("suppliers", catalog.suppliers());
    m.addAttribute("products", catalog.importChoices());
    return "imports/form";
  }
}
