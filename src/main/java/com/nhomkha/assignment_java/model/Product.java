package com.nhomkha.assignment_java.model;

public class Product {
  private long id;
  private String barcode;
  private String name;
  private java.math.BigDecimal price;
  private int stock;
  private java.time.LocalDate expiry;
  private long categoryId;
  private long supplierId;
  private String categoryName;
  private String supplierName;

  public Product() {}

  public Product(
      long id,
      String barcode,
      String name,
      java.math.BigDecimal price,
      int stock,
      java.time.LocalDate expiry,
      long categoryId,
      long supplierId,
      String categoryName,
      String supplierName) {
    this.id = id;
    this.barcode = barcode;
    this.name = name;
    this.price = price;
    this.stock = stock;
    this.expiry = expiry;
    this.categoryId = categoryId;
    this.supplierId = supplierId;
    this.categoryName = categoryName;
    this.supplierName = supplierName;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public java.math.BigDecimal getPrice() {
    return price;
  }

  public void setPrice(java.math.BigDecimal price) {
    this.price = price;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
  }

  public java.time.LocalDate getExpiry() {
    return expiry;
  }

  public void setExpiry(java.time.LocalDate expiry) {
    this.expiry = expiry;
  }

  public long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(long categoryId) {
    this.categoryId = categoryId;
  }

  public long getSupplierId() {
    return supplierId;
  }

  public void setSupplierId(long supplierId) {
    this.supplierId = supplierId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public String getSupplierName() {
    return supplierName;
  }

  public void setSupplierName(String supplierName) {
    this.supplierName = supplierName;
  }
}
