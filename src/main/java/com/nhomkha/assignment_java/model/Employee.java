package com.nhomkha.assignment_java.model;

public class Employee {
  private long id;
  private String name;
  private String title;
  private String username;
  private String passwordHash;
  private String role;
  private boolean active;

  public Employee() {}

  public Employee(
      long id,
      String name,
      String title,
      String username,
      String passwordHash,
      String role,
      boolean active) {
    this.id = id;
    this.name = name;
    this.title = title;
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = role;
    this.active = active;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
