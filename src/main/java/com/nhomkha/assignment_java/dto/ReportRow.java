package com.nhomkha.assignment_java.dto;

public class ReportRow {
  private String label;
  private java.math.BigDecimal amount;
  private long quantity;

  public ReportRow() {}

  public ReportRow(String label, java.math.BigDecimal amount, long quantity) {
    this.label = label;
    this.amount = amount;
    this.quantity = quantity;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public java.math.BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(java.math.BigDecimal amount) {
    this.amount = amount;
  }

  public long getQuantity() {
    return quantity;
  }

  public void setQuantity(long quantity) {
    this.quantity = quantity;
  }
}
