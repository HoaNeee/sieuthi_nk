package com.nhomkha.assignment_java.model;

public class Invoice {
  private long id;
  private java.time.LocalDateTime date;
  private long employeeId;
  private Long customerId;
  private String employeeName;
  private String customerName;
  private String payment;
  private java.math.BigDecimal total;

  public Invoice() {}

  public Invoice(
      long id,
      java.time.LocalDateTime date,
      long employeeId,
      Long customerId,
      String employeeName,
      String customerName,
      String payment,
      java.math.BigDecimal total) {
    this.id = id;
    this.date = date;
    this.employeeId = employeeId;
    this.customerId = customerId;
    this.employeeName = employeeName;
    this.customerName = customerName;
    this.payment = payment;
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

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long customerId) {
    this.customerId = customerId;
  }

  public String getEmployeeName() {
    return employeeName;
  }

  public void setEmployeeName(String employeeName) {
    this.employeeName = employeeName;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getPayment() {
    return payment;
  }

  public void setPayment(String payment) {
    this.payment = payment;
  }

  public java.math.BigDecimal getTotal() {
    return total;
  }

  public void setTotal(java.math.BigDecimal total) {
    this.total = total;
  }
}
