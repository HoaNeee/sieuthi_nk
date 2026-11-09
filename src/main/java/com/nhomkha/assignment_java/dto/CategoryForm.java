package com.nhomkha.assignment_java.dto;

import jakarta.validation.constraints.*;

public class CategoryForm {
  @NotBlank
  @Size(max = 150)
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  @Size(max = 500)
  private String description;

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }
}
