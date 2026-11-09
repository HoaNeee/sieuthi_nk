package com.nhomkha.assignment_java.model;

public class Customer {
  private long id;
  private String name;
  private String phone;
  private int points;

  public Customer() {}

  public Customer(long id, String name, String phone, int points) {
    this.id = id;
    this.name = name;
    this.phone = phone;
    this.points = points;
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

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }
}
