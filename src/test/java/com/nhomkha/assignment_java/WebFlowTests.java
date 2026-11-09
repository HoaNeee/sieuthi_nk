package com.nhomkha.assignment_java;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.nhomkha.assignment_java.config.EmployeePrincipal;
import com.nhomkha.assignment_java.model.*;
import com.nhomkha.assignment_java.repository.*;
import com.nhomkha.assignment_java.service.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "DB_PASSWORD=test-only-not-a-real-password")
@AutoConfigureMockMvc
class WebFlowTests {
  @Autowired MockMvc mvc;
  @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
  @MockitoBean EmployeeRepository employees;
  @MockitoBean CatalogService catalog;
  @MockitoBean TradingService trading;
  @MockitoBean ReportService reports;
  Employee admin = new Employee(1, "Admin", "Quản lý", "admin", "hash", "ADMIN", true);
  Employee staff = new Employee(2, "Staff", "Thu ngân", "staff", "hash", "STAFF", true);
  Product product =
      new Product(
          1, "SP001", "Gạo", new BigDecimal("15000.00"), 10, null, 1, 1, "Thực phẩm", "NCC");
  DocumentHeader doc =
      new DocumentHeader(
          1,
          LocalDateTime.of(2026, 1, 1, 10, 0),
          "Staff",
          null,
          "TIEN_MAT",
          new BigDecimal("15000"));

  @BeforeEach
  void setup() throws Exception {
    when(employees.byUsername("admin")).thenReturn(Optional.of(admin));
    when(employees.byUsername("staff")).thenReturn(Optional.of(staff));
    when(employees.all()).thenReturn(List.of(admin, staff));
    when(catalog.categories()).thenReturn(List.of(new Category(1, "Thực phẩm", "Mô tả")));
    when(catalog.suppliers())
        .thenReturn(List.of(new Supplier(1, "NCC", "0123", "Hà Nội", "a@example.test")));
    when(catalog.customers(anyString())).thenReturn(List.of(new Customer(1, "Khách", "0123", 0)));
    when(catalog.search(anyString(), any(), anyInt())).thenReturn(List.of(product));
    when(catalog.product(1)).thenReturn(product);
    when(catalog.importChoices()).thenReturn(List.of(product));
    when(catalog.category(1)).thenReturn(new Category(1, "Thực phẩm", "Mô tả"));
    when(catalog.supplier(1)).thenReturn(new Supplier(1, "NCC", "0123", "HN", "a@example.test"));
    when(catalog.customer(1)).thenReturn(new Customer(1, "Khách", "0123", 0));
    when(trading.invoices(any())).thenReturn(List.of(doc));
    when(trading.imports()).thenReturn(List.of(doc));
    when(trading.invoice(eq(1L), any())).thenReturn(doc);
    when(trading.stockImport(1)).thenReturn(doc);
    when(trading.invoiceLines(eq(1L), any()))
        .thenReturn(List.of(new DocumentLine("Gạo", 1, new BigDecimal("15000"))));
    when(trading.importLines(1))
        .thenReturn(List.of(new DocumentLine("Gạo", 1, new BigDecimal("15000"))));
    when(reports.revenue(any(), any()))
        .thenReturn(List.of(new ReportRow("2026-01-01", new BigDecimal("15000"), 1)));
    when(reports.sales(any(), any()))
        .thenReturn(List.of(new ReportRow("Gạo", new BigDecimal("15000"), 1)));
    when(reports.stock()).thenReturn(List.of(new ReportRow("Gạo", new BigDecimal("150000"), 10)));
  }

  @Test
  void anonymousCannotReadInternalPages() throws Exception {
    for (String path : List.of("/products", "/pos", "/invoices", "/customers", "/reports"))
      mvc.perform(get(path))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/login"));
  }

  @Test
  void staffCannotReadOrPostAdminRoutes() throws Exception {
    for (String path :
        List.of(
            "/employees",
            "/suppliers",
            "/imports",
            "/categories",
            "/reports",
            "/products/new",
            "/products/1/edit")) {
      mvc.perform(get(path).with(user(new EmployeePrincipal(staff))))
          .andExpect(status().isForbidden());
      mvc.perform(post(path).with(user(new EmployeePrincipal(staff))).with(csrf()))
          .andExpect(status().isForbidden());
    }
    mvc.perform(post("/products").with(user(new EmployeePrincipal(staff))).with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void allMainTemplatesRenderWithRealThymeleaf() throws Exception {
    for (String path :
        List.of(
            "/products",
            "/products/new",
            "/products/1/edit",
            "/categories",
            "/categories/new",
            "/categories/1/edit",
            "/suppliers",
            "/suppliers/new",
            "/suppliers/1/edit",
            "/customers",
            "/customers/new",
            "/customers/1/edit",
            "/employees",
            "/employees/new",
            "/pos",
            "/invoices",
            "/invoices/1",
            "/imports",
            "/imports/new",
            "/imports/1",
            "/reports",
            "/password")) {
      mvc.perform(get(path).with(user(new EmployeePrincipal(admin)))).andExpect(status().isOk());
    }
    mvc.perform(get("/login")).andExpect(status().isOk());
  }

  @Test
  void csrfRequiredForMutation() throws Exception {
    mvc.perform(post("/pos/items").param("productId", "1").with(user(new EmployeePrincipal(staff))))
        .andExpect(status().isForbidden());
  }

  @Test
  void realLoginAcceptsHashAndRejectsLockedAccount() throws Exception {
    String hash = encoder.encode("testing-only-123");
    when(employees.byUsername("staff"))
        .thenReturn(Optional.of(new Employee(2, "Staff", "", "staff", hash, "STAFF", true)));
    var result =
        mvc.perform(
                post("/login")
                    .with(csrf())
                    .param("username", "staff")
                    .param("password", "testing-only-123"))
            .andExpect(redirectedUrl("/products"))
            .andReturn();
    var session =
        (org.springframework.mock.web.MockHttpSession) result.getRequest().getSession(false);
    mvc.perform(get("/products").session(session)).andExpect(status().isOk());
    when(employees.byUsername("staff"))
        .thenReturn(Optional.of(new Employee(2, "Staff", "", "staff", hash, "STAFF", false)));
    mvc.perform(
            post("/login")
                .with(csrf())
                .param("username", "staff")
                .param("password", "testing-only-123"))
        .andExpect(redirectedUrl("/login?error"));
  }

  @Test
  void lockedOrChangedRoleSessionIsInvalidated() throws Exception {
    when(employees.byUsername("staff"))
        .thenReturn(Optional.of(new Employee(2, "Staff", "", "staff", "hash", "STAFF", false)));
    mvc.perform(get("/products").with(user(new EmployeePrincipal(staff))))
        .andExpect(redirectedUrl("/login?expired"));
  }

  @Test
  void invalidProductFormShowsFieldErrors() throws Exception {
    mvc.perform(
            post("/products")
                .with(user(new EmployeePrincipal(admin)))
                .with(csrf())
                .param("name", "")
                .param("price", "-1")
                .param("barcode", "has space"))
        .andExpect(status().isOk())
        .andExpect(
            model()
                .attributeHasFieldErrors(
                    "form", "name", "price", "barcode", "categoryId", "supplierId"));
    verify(catalog, never()).saveProduct(any(), any());
  }

  @Test
  void cartPreservedOnFailureAndClearedOnSuccess() throws Exception {
    var principal = new EmployeePrincipal(staff);
    var session = new org.springframework.mock.web.MockHttpSession();
    mvc.perform(
            post("/pos/items")
                .session(session)
                .with(user(principal))
                .with(csrf())
                .param("productId", "1")
                .param("quantity", "2"))
        .andExpect(redirectedUrl("/pos"));
    mvc.perform(get("/pos").session(session).with(user(principal))).andExpect(status().isOk());
    when(trading.checkout(anyMap(), any(), any()))
        .thenThrow(
            new com.nhomkha.assignment_java.exception.BusinessException("Không đủ tồn kho."));
    mvc.perform(
            post("/pos/checkout")
                .session(session)
                .with(user(principal))
                .with(csrf())
                .param("payment", "TIEN_MAT"))
        .andExpect(status().isOk());
    Assertions.assertEquals(2, ((Cart) session.getAttribute("cart")).snapshot().get(1L));
    doReturn(42L).when(trading).checkout(anyMap(), any(), any());
    mvc.perform(
            post("/pos/checkout")
                .session(session)
                .with(user(principal))
                .with(csrf())
                .param("payment", "TIEN_MAT"))
        .andExpect(redirectedUrl("/invoices/42"));
    Assertions.assertTrue(((Cart) session.getAttribute("cart")).snapshot().isEmpty());
  }

  @Test
  void errorPageDoesNotExposeSql() throws Exception {
    when(catalog.search(anyString(), any(), anyInt()))
        .thenThrow(new java.sql.SQLException("secret SQL/password"));
    mvc.perform(get("/products").with(user(new EmployeePrincipal(staff))))
        .andExpect(status().isServiceUnavailable())
        .andExpect(
            content()
                .string(
                    org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret SQL"))));
  }
}
