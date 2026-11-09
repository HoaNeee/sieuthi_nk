package com.nhomkha.assignment_java.dto;

import jakarta.validation.constraints.*;

public class ImportLineForm {
  @NotNull @Positive private Long productId;

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long value) {
    this.productId = value;
  }

  @NotNull
  @Min(1)
  @Max(999999999)
  private Integer quantity;

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer value) {
    this.quantity = value;
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
}
