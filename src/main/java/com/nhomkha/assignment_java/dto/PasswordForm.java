package com.nhomkha.assignment_java.dto;

import jakarta.validation.constraints.*;

public class PasswordForm {
  @NotBlank private String currentPassword;

  public String getCurrentPassword() {
    return currentPassword;
  }

  public void setCurrentPassword(String value) {
    this.currentPassword = value;
  }

  @NotBlank
  @Size(min = 10, max = 72)
  private String newPassword;

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String value) {
    this.newPassword = value;
  }
}
