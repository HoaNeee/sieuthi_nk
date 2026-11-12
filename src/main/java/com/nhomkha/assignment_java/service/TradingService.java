package com.nhomkha.assignment_java.service;

import com.nhomkha.assignment_java.config.EmployeePrincipal;
import com.nhomkha.assignment_java.dto.*;
import com.nhomkha.assignment_java.exception.BusinessException;
import com.nhomkha.assignment_java.model.*;
import com.nhomkha.assignment_java.repository.*;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class TradingService {
  private final JdbcTransactions tx;
  private final ProductRepository products;
  private final DocumentRepository docs;
  private final EmployeeRepository employees;
  private final Validator validator;

  public TradingService(
      JdbcTransactions tx,
      ProductRepository products,
      DocumentRepository docs,
      EmployeeRepository employees,
      Validator validator) {
    this.tx = tx;
    this.products = products;
    this.docs = docs;
    this.employees = employees;
    this.validator = validator;
  }

  private void actor(Connection c, EmployeePrincipal principal, boolean admin) throws SQLException {
    Employee e =
        employees
            .byId(c, principal.getId())
            .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên."));
    if (!e.isActive() || (admin && !e.getRole().equals("ADMIN")))
      throw new BusinessException("Tài khoản không có quyền thực hiện.");
  }

  public long checkout(Map<Long, Integer> input, CheckoutForm form, EmployeePrincipal principal)
      throws SQLException {
    if (!validator.validate(form).isEmpty())
      throw new BusinessException("Thông tin thanh toán không hợp lệ.");
    if (input == null || input.isEmpty() || input.size() > 100)
      throw new BusinessException("Giỏ hàng cần từ 1 đến 100 sản phẩm.");
    Map<Long, Integer> cart = new TreeMap<>();
    input.forEach(
        (id, q) -> {
          if (id == null || id <= 0 || q == null || q <= 0 || q > 999999999)
            throw new BusinessException("Số lượng sản phẩm không hợp lệ.");
          cart.put(id, q);
        });
    return tx.run(
        c -> {
          actor(c, principal, false);
          if (form.getCustomerId() != null && !docs.customerExists(c, form.getCustomerId()))
            throw new BusinessException("Khách hàng không tồn tại.");
          Map<Long, Product> current = new LinkedHashMap<>();
          BigDecimal total = BigDecimal.ZERO;
          for (var entry : cart.entrySet()) {
            Product p =
                products
                    .find(c, entry.getKey(), true)
                    .orElseThrow(() -> new BusinessException("Sản phẩm không còn tồn tại."));
            if (p.getStock() < entry.getValue())
              throw new BusinessException("Không đủ tồn kho: " + p.getName());
            current.put(p.getId(), p);
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
          }
          if (total.compareTo(new BigDecimal("9999999999999999.99")) > 0)
            throw new BusinessException("Tổng tiền vượt giới hạn hóa đơn.");
          long id = docs.invoice(c, principal.getId(), form.getCustomerId(), form.getPayment(), total);
          for (var entry : cart.entrySet()) {
            // Rows remain locked until commit; the Oracle detail trigger decrements stock once.
            docs.invoiceLine(
                c, id, entry.getKey(), entry.getValue(), current.get(entry.getKey()).getPrice());
          }
          if (form.getCustomerId() != null) {
            long points = total.divideToIntegralValue(new BigDecimal("10000")).longValueExact();
            if (points > 0) docs.addCustomerPoints(c, form.getCustomerId(), points);
          }
          return id;
        });
  }

  public long stockImport(ImportForm form, EmployeePrincipal principal) throws SQLException {
    if (!principal.isAdmin()) throw new BusinessException("Chỉ ADMIN được nhập kho.");
    if (!validator.validate(form).isEmpty())
      throw new BusinessException("Phiếu nhập không hợp lệ.");
    // Repeated products retain separate costs, but lock products in a stable order.
    List<ImportLineForm> lines = new ArrayList<>(form.getLines());
    lines.sort(Comparator.comparing(ImportLineForm::getProductId));
    return tx.run(
        c -> {
          actor(c, principal, true);
          Map<Long, Long> quantities = new HashMap<>();
          for (var line : lines) {
            Product p =
                products
                    .find(c, line.getProductId(), true)
                    .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại."));
            long quantity = quantities.merge(p.getId(), (long) line.getQuantity(), Long::sum);
            if (quantity + p.getStock() > 999999999L)
              throw new BusinessException("Tồn kho vượt giới hạn cho phép.");
            if (p.getSupplierId() != form.getSupplierId())
              throw new BusinessException(
                  "Sản phẩm " + p.getName() + " không thuộc nhà cung cấp đã chọn.");
          }
          long id = docs.stockImport(c, principal.getId(), form.getSupplierId());
          for (var line : lines) {
            // Locked rows were validated above; TRG_CTPN_TON_KHO increases stock once.
            docs.importLine(c, id, line.getProductId(), line.getQuantity(), line.getPrice());
          }
          return id;
        });
  }

  public List<Invoice> invoices(EmployeePrincipal actor) throws SQLException {
    return docs.invoices(actor.isAdmin() ? null : actor.getId(), null);
  }

  public Invoice invoice(long id, EmployeePrincipal actor) throws SQLException {
    return docs.invoices(actor.isAdmin() ? null : actor.getId(), id).stream()
        .findFirst()
        .orElseThrow(
            () ->
                new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND));
  }

  public List<InvoiceDetail> invoiceLines(long id, EmployeePrincipal actor) throws SQLException {
    return docs.invoiceLines(id, actor.isAdmin() ? null : actor.getId());
  }

  public List<StockImport> imports() throws SQLException {
    return docs.imports(null);
  }

  public StockImport stockImport(long id) throws SQLException {
    return docs.imports(id).stream()
        .findFirst()
        .orElseThrow(
            () ->
                new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND));
  }

  public List<StockImportDetail> importLines(long id) throws SQLException {
    return docs.importLines(id);
  }
}
