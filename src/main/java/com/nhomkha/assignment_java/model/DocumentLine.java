package com.nhomkha.assignment_java.model;

public class DocumentLine {
  private String product;
  private int quantity;
  private java.math.BigDecimal price;

  public DocumentLine() {}

  public DocumentLine(String product, int quantity, java.math.BigDecimal price) {
    this.product = product;
    this.quantity = quantity;
    this.price = price;
  }

  public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public java.math.BigDecimal getPrice() {
    return price;
  }

  public void setPrice(java.math.BigDecimal price) {
    this.price = price;
  }

  public java.math.BigDecimal getAmount() {
    return price.multiply(java.math.BigDecimal.valueOf(quantity));
  }
}
