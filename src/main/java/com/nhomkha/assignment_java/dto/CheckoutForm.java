package com.nhomkha.assignment_java.dto;

import jakarta.validation.constraints.*;

public class CheckoutForm {
  @Positive private Long customerId;

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long value) {
    this.customerId = value;
  }

  @NotNull
  @Pattern(regexp = "TIEN_MAT|CHUYEN_KHOAN")
  private String payment;

  public String getPayment() {
    return payment;
  }

  public void setPayment(String value) {
    this.payment = value;
  }
}
