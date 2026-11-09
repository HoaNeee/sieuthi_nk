package com.nhomkha.assignment_java.dto;

import jakarta.validation.constraints.*;

public class SupplierForm {
  @NotBlank
  @Size(max = 150)
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  @Size(max = 20)
  private String phone;

  public String getPhone() {
    return phone;
  }

  public void setPhone(String value) {
    this.phone = value;
  }

  @Size(max = 300)
  private String address;

  public String getAddress() {
    return address;
  }

  public void setAddress(String value) {
    this.address = value;
  }

  @Email
  @Size(max = 150)
  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String value) {
    this.email = value;
  }
}
