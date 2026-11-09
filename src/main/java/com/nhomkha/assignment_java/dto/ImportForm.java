package com.nhomkha.assignment_java.dto;

import jakarta.validation.constraints.*;

public class ImportForm {
  @NotNull @Positive private Long supplierId;

  public Long getSupplierId() {
    return supplierId;
  }

  public void setSupplierId(Long value) {
    this.supplierId = value;
  }

  @NotEmpty
  @Size(max = 100)
  private java.util.List<@NotNull @jakarta.validation.Valid ImportLineForm> lines =
      new java.util.ArrayList<>();

  public java.util.List<ImportLineForm> getLines() {
    return lines;
  }

  public void setLines(java.util.List<ImportLineForm> value) {
    this.lines = value;
  }
}
