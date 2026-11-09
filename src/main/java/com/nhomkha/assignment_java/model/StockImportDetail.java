package com.nhomkha.assignment_java.model;

public class StockImportDetail {
  private long id;
  private long stockImportId;
  private long productId;
  private String productName;
  private int quantity;
  private java.math.BigDecimal purchasePrice;

  public StockImportDetail() {}

  public StockImportDetail(
      long id,
      long stockImportId,
      long productId,
      String productName,
      int quantity,
      java.math.BigDecimal purchasePrice) {
    this.id = id;
    this.stockImportId = stockImportId;
    this.productId = productId;
    this.productName = productName;
    this.quantity = quantity;
    this.purchasePrice = purchasePrice;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getStockImportId() {
    return stockImportId;
  }

  public void setStockImportId(long stockImportId) {
    this.stockImportId = stockImportId;
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

  public java.math.BigDecimal getPurchasePrice() {
    return purchasePrice;
  }

  public void setPurchasePrice(java.math.BigDecimal purchasePrice) {
    this.purchasePrice = purchasePrice;
  }

  public java.math.BigDecimal getAmount() {
    return purchasePrice.multiply(java.math.BigDecimal.valueOf(quantity));
  }
}
