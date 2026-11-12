package com.nhomkha.assignment_java.dto;

import jakarta.validation.constraints.*;

public class EmployeeForm {
  @NotBlank
  @Size(max = 150)
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  @Size(max = 100)
  private String title;

  public String getTitle() {
    return title;
  }

  public void setTitle(String value) {
    this.title = value;
  }

  @NotBlank
  @Pattern(regexp = "[a-zA-Z0-9._-]{3,80}")
  private String username;

  public String getUsername() {
    return username;
  }

  public void setUsername(String value) {
    this.username = value;
  }

  @Size(max = 72)
  private String password;

  public String getPassword() {
    return password;
  }

  public void setPassword(String value) {
    this.password = value;
  }

  @NotNull
  @Pattern(regexp = "ADMIN|THU_NGAN")
  private String role;

  public String getRole() {
    return role;
  }

  public void setRole(String value) {
    this.role = value;
  }

  private boolean active = true;

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean value) {
    this.active = value;
  }
}
