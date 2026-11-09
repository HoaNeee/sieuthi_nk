package com.nhomkha.assignment_java.dto;

import jakarta.validation.constraints.*;

public class ProductForm {
  @NotBlank
  @Size(max = 150)
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  @Size(max = 80)
  @Pattern(regexp = "[^\\s]*", message = "Barcode không được chứa khoảng trắng")
  private String barcode;

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String value) {
    this.barcode = value;
  }

  @NotNull
  @DecimalMin("0")
  @Digits(integer = 13, fraction = 2)
  private java.math.BigDecimal price;

  public java.math.BigDecimal getPrice() {
    return price;
  }

  public void setPrice(java.math.BigDecimal value) {
    this.price = value;
  }

  @NotNull @Positive private Long categoryId;

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long value) {
    this.categoryId = value;
  }

  @NotNull @Positive private Long supplierId;

  public Long getSupplierId() {
    return supplierId;
  }

  public void setSupplierId(Long value) {
    this.supplierId = value;
  }

  @org.springframework.format.annotation.DateTimeFormat(
      iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
  private java.time.LocalDate expiry;

  public java.time.LocalDate getExpiry() {
    return expiry;
  }

  public void setExpiry(java.time.LocalDate value) {
    this.expiry = value;
  }
}
