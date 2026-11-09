package com.nhomkha.assignment_java.model;

public class DocumentHeader {
  private long id;
  private java.time.LocalDateTime date;
  private String employee;
  private String partner;
  private String payment;
  private java.math.BigDecimal total;

  public DocumentHeader() {}

  public DocumentHeader(
      long id,
      java.time.LocalDateTime date,
      String employee,
      String partner,
      String payment,
      java.math.BigDecimal total) {
    this.id = id;
    this.date = date;
    this.employee = employee;
    this.partner = partner;
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

  public String getEmployee() {
    return employee;
  }

  public void setEmployee(String employee) {
    this.employee = employee;
  }

  public String getPartner() {
    return partner;
  }

  public void setPartner(String partner) {
    this.partner = partner;
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
