package com.nhomkha.assignment_java.model;

public class StockImport {
  private long id;
  private java.time.LocalDateTime date;
  private long employeeId;
  private long supplierId;
  private String employeeName;
  private String supplierName;
  private java.math.BigDecimal total;

  public StockImport() {}

  public StockImport(
      long id,
      java.time.LocalDateTime date,
      long employeeId,
      long supplierId,
      String employeeName,
      String supplierName,
      java.math.BigDecimal total) {
    this.id = id;
    this.date = date;
    this.employeeId = employeeId;
    this.supplierId = supplierId;
    this.employeeName = employeeName;
    this.supplierName = supplierName;
    this.total = total;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public java.time.LocalDateTime getDate() {
    return date;
  }

  public void setDate(java.time.LocalDateTime date) {
    this.date = date;
  }

  public long getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(long employeeId) {
    this.employeeId = employeeId;
  }

  public long getSupplierId() {
    return supplierId;
  }

  public void setSupplierId(long supplierId) {
    this.supplierId = supplierId;
  }

  public String getEmployeeName() {
    return employeeName;
  }

  public void setEmployeeName(String employeeName) {
    this.employeeName = employeeName;
  }

  public String getSupplierName() {
    return supplierName;
  }

  public void setSupplierName(String supplierName) {
    this.supplierName = supplierName;
  }

  public java.math.BigDecimal getTotal() {
    return total;
  }

  public void setTotal(java.math.BigDecimal total) {
    this.total = total;
  }
}
