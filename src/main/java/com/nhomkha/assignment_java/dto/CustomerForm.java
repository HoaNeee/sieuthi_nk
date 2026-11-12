package com.nhomkha.assignment_java.dto;

import jakarta.validation.constraints.*;

public class CustomerForm {
  @NotBlank
  @Size(max = 150)
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  @NotBlank
  @Size(max = 20)
  private String phone;

  public String getPhone() {
    return phone;
  }

  public void setPhone(String value) {
    this.phone = value;
  }
}
