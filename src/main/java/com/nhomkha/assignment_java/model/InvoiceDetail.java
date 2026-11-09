package com.nhomkha.assignment_java.model;

public class InvoiceDetail {
  private long id;
  private long invoiceId;
  private long productId;
  private String productName;
  private int quantity;
  private java.math.BigDecimal unitPrice;

  public InvoiceDetail() {}

  public InvoiceDetail(
      long id,
      long invoiceId,
      long productId,
      String productName,
      int quantity,
      java.math.BigDecimal unitPrice) {
    this.id = id;
    this.invoiceId = invoiceId;
    this.productId = productId;
    this.productName = productName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getInvoiceId() {
    return invoiceId;
  }

  public void setInvoiceId(long invoiceId) {
    this.invoiceId = invoiceId;
  }

  public long getProductId() {
    return productId;
  }

  public void setProductId(long productId) {
    this.productId = productId;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public java.math.BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(java.math.BigDecimal unitPrice) {
    this.unitPrice = unitPrice;
  }

  public java.math.BigDecimal getAmount() {
    return unitPrice.multiply(java.math.BigDecimal.valueOf(quantity));
  }
}
