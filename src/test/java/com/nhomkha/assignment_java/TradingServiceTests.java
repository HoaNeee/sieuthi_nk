package com.nhomkha.assignment_java;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nhomkha.assignment_java.config.EmployeePrincipal;
import com.nhomkha.assignment_java.dto.*;
import com.nhomkha.assignment_java.exception.BusinessException;
import com.nhomkha.assignment_java.model.*;
import com.nhomkha.assignment_java.repository.*;
import com.nhomkha.assignment_java.service.*;
import jakarta.validation.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;

class TradingServiceTests {
  DataSource ds;
  Connection c;
  ProductRepository products;
  DocumentRepository docs;
  EmployeeRepository employees;
  TradingService service;
  EmployeePrincipal staff =
      new EmployeePrincipal(new Employee(2, "Staff", "", "staff", "hash", "STAFF", true));
  EmployeePrincipal admin =
      new EmployeePrincipal(new Employee(1, "Admin", "", "admin", "hash", "ADMIN", true));
  static ValidatorFactory validation;

  @BeforeAll
  static void factory() {
    validation = Validation.buildDefaultValidatorFactory();
  }

  @AfterAll
  static void close() {
    validation.close();
  }

  @BeforeEach
  void setup() throws Exception {
    ds = mock(DataSource.class);
    c = mock(Connection.class);
    products = mock(ProductRepository.class);
    docs = mock(DocumentRepository.class);
    employees = mock(EmployeeRepository.class);
    when(ds.getConnection()).thenReturn(c);
    when(c.getAutoCommit()).thenReturn(true);
    when(employees.byId(c, 2))
        .thenReturn(Optional.of(new Employee(2, "Staff", "", "staff", "hash", "STAFF", true)));
    when(employees.byId(c, 1))
        .thenReturn(Optional.of(new Employee(1, "Admin", "", "admin", "hash", "ADMIN", true)));
    when(products.find(c, 10, true))
        .thenReturn(
            Optional.of(
                new Product(10, "A", "Gạo", new BigDecimal("12.35"), 5, null, 1, 3, "DM", "NCC")));
    when(products.decrease(c, 10, 2)).thenReturn(true);
    when(products.increase(c, 10, 2, 3)).thenReturn(true);
    when(docs.nextInvoice(c)).thenReturn(99L);
    when(docs.nextImport(c)).thenReturn(88L);
    service =
        new TradingService(
            new JdbcTransactions(ds), products, docs, employees, validation.getValidator());
  }

  CheckoutForm checkout() {
    var f = new CheckoutForm();
    f.setPayment("TIEN_MAT");
    return f;
  }

  ImportForm stockImport() {
    var f = new ImportForm();
    f.setSupplierId(3L);
    var line = new ImportLineForm();
    line.setProductId(10L);
    line.setQuantity(2);
    line.setPrice(new BigDecimal("9.50"));
    f.setLines(List.of(line));
    return f;
  }

  @Test
  void cashSaleUsesDatabasePriceAndWalkInCustomer() throws Exception {
    assertEquals(99, service.checkout(Map.of(10L, 2), checkout(), staff));
    verify(docs).invoice(c, 99, 2, null, "TIEN_MAT", new BigDecimal("24.70"));
    verify(docs).invoiceLine(c, 99, 10, 2, new BigDecimal("12.35"));
    verify(products).decrease(c, 10, 2);
    verify(c).commit();
    verify(c, never()).rollback();
  }

  @Test
  void insufficientStockRollsBack() throws Exception {
    assertThrows(
        BusinessException.class, () -> service.checkout(Map.of(10L, 6), checkout(), staff));
    verify(c).rollback();
    verify(c, never()).commit();
    verify(docs, never()).nextInvoice(any());
  }

  @Test
  void conditionalStockFailureRollsBack() throws Exception {
    when(products.decrease(c, 10, 2)).thenReturn(false);
    assertThrows(
        BusinessException.class, () -> service.checkout(Map.of(10L, 2), checkout(), staff));
    verify(c).rollback();
    verify(c, never()).commit();
  }

  @Test
  void detailInsertFailureRollsBack() throws Exception {
    doThrow(new SQLException("failure"))
        .when(docs)
        .invoiceLine(any(), anyLong(), anyLong(), anyInt(), any());
    assertThrows(SQLException.class, () -> service.checkout(Map.of(10L, 2), checkout(), staff));
    verify(c).rollback();
    verify(c, never()).commit();
  }

  @Test
  void invalidQuantityRejectedBeforeConnection() throws Exception {
    assertThrows(
        BusinessException.class, () -> service.checkout(Map.of(10L, 0), checkout(), staff));
    verify(ds, never()).getConnection();
  }

  @Test
  void missingProductRollsBack() throws Exception {
    assertThrows(
        BusinessException.class, () -> service.checkout(Map.of(500L, 1), checkout(), staff));
    verify(c).rollback();
  }

  @Test
  void customerMustExist() throws Exception {
    var f = checkout();
    f.setCustomerId(77L);
    assertThrows(BusinessException.class, () -> service.checkout(Map.of(10L, 2), f, staff));
    verify(c).rollback();
  }

  @Test
  void matchingSupplierImportIncreasesOnce() throws Exception {
    assertEquals(88, service.stockImport(stockImport(), admin));
    verify(products, times(1)).increase(c, 10, 2, 3);
    verify(docs).importLine(c, 88, 10, 2, new BigDecimal("9.50"));
    verify(c).commit();
  }

  @Test
  void differentSupplierRejected() throws Exception {
    var f = stockImport();
    f.setSupplierId(4L);
    assertThrows(BusinessException.class, () -> service.stockImport(f, admin));
    verify(c).rollback();
    verify(products, never()).increase(any(), anyLong(), anyInt(), anyLong());
  }

  @Test
  void importFailureRollsBack() throws Exception {
    doThrow(new SQLException("detail"))
        .when(docs)
        .importLine(any(), anyLong(), anyLong(), anyInt(), any());
    assertThrows(SQLException.class, () -> service.stockImport(stockImport(), admin));
    verify(c).rollback();
    verify(c, never()).commit();
  }

  @Test
  void staffCannotImport() throws Exception {
    assertThrows(BusinessException.class, () -> service.stockImport(stockImport(), staff));
    verify(ds, never()).getConnection();
  }

  @Test
  void invoiceReadsScopeToCurrentEmployee() throws Exception {
    when(docs.invoices(2L, 4L)).thenReturn(List.of());
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> service.invoice(4, staff));
    verify(docs).invoices(2L, 4L);
    service.invoices(admin);
    verify(docs).invoices(null, null);
  }
}
