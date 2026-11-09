package com.nhomkha.assignment_java;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nhomkha.assignment_java.repository.DocumentRepository;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DocumentMappingTests {
  ResultSet result;
  PreparedStatement statement;
  DocumentRepository repository;

  @BeforeEach
  void setup() throws Exception {
    DataSource ds = mock(DataSource.class);
    Connection connection = mock(Connection.class);
    statement = mock(PreparedStatement.class);
    result = mock(ResultSet.class);
    when(ds.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(result);
    when(result.next()).thenReturn(true, false);
    when(result.getTimestamp(2)).thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 10, 0)));
    repository = new DocumentRepository(ds);
  }

  @Test
  void invoiceKeepsNullableCustomerAndEmployeeId() throws Exception {
    when(result.getLong(1)).thenReturn(10L);
    when(result.getLong(7)).thenReturn(2L);
    when(result.getLong(8)).thenReturn(0L);
    when(result.wasNull()).thenReturn(true);
    when(result.getString(3)).thenReturn("Thu ngân");
    when(result.getString(5)).thenReturn("TIEN_MAT");
    var invoice = repository.invoices(2L, 10L).getFirst();
    assertNull(invoice.getCustomerId());
    assertEquals(2, invoice.getEmployeeId());
    assertEquals("Thu ngân", invoice.getEmployeeName());
    assertEquals("TIEN_MAT", invoice.getPayment());
    verify(statement).setObject(1, 2L, Types.NUMERIC);
  }

  @Test
  void stockImportMapsSupplierAndCalculatedTotal() throws Exception {
    when(result.getLong(6)).thenReturn(1L);
    when(result.getLong(7)).thenReturn(3L);
    when(result.getString(4)).thenReturn("Nhà cung cấp");
    when(result.getBigDecimal(5)).thenReturn(new BigDecimal("50.00"));
    var stockImport = repository.imports(5L).getFirst();
    assertEquals(1, stockImport.getEmployeeId());
    assertEquals(3, stockImport.getSupplierId());
    assertEquals("Nhà cung cấp", stockImport.getSupplierName());
    assertEquals(new BigDecimal("50.00"), stockImport.getTotal());
  }

  @Test
  void invoiceDetailKeepsIdsAndSalePrice() throws Exception {
    when(result.getLong(4)).thenReturn(11L);
    when(result.getLong(5)).thenReturn(12L);
    when(result.getLong(6)).thenReturn(13L);
    when(result.getInt(2)).thenReturn(2);
    when(result.getBigDecimal(3)).thenReturn(new BigDecimal("12.35"));
    var line = repository.invoiceLines(12L, 2L).getFirst();
    assertEquals(11, line.getId());
    assertEquals(12, line.getInvoiceId());
    assertEquals(13, line.getProductId());
    assertEquals(new BigDecimal("24.70"), line.getAmount());
  }

  @Test
  void importDetailKeepsIdsAndPurchasePrice() throws Exception {
    when(result.getLong(4)).thenReturn(21L);
    when(result.getLong(5)).thenReturn(22L);
    when(result.getLong(6)).thenReturn(23L);
    when(result.getInt(2)).thenReturn(3);
    when(result.getBigDecimal(3)).thenReturn(new BigDecimal("8.50"));
    var line = repository.importLines(22L).getFirst();
    assertEquals(21, line.getId());
    assertEquals(22, line.getStockImportId());
    assertEquals(23, line.getProductId());
    assertEquals(new BigDecimal("8.50"), line.getPurchasePrice());
    assertEquals(new BigDecimal("25.50"), line.getAmount());
  }
}
