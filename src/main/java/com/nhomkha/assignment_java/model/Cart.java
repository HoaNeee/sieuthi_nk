package com.nhomkha.assignment_java.model;

import com.nhomkha.assignment_java.exception.BusinessException;
import java.io.Serializable;
import java.util.*;

/** Access and checkout synchronize on this session-scoped object, never a global cart. */
public class Cart implements Serializable {
  private final Map<Long, Integer> items = new TreeMap<>();
  private final long employeeId;

  public Cart(long employeeId) {
    this.employeeId = employeeId;
  }

  public long getEmployeeId() {
    return employeeId;
  }

  public synchronized Map<Long, Integer> snapshot() {
    return new TreeMap<>(items);
  }

  public synchronized void add(long id, int quantity) {
    valid(id, quantity);
    if (!items.containsKey(id) && items.size() >= 100)
      throw new BusinessException("Giỏ tối đa 100 sản phẩm.");
    long total = (long) items.getOrDefault(id, 0) + quantity;
    if (total > 999999999) throw new BusinessException("Số lượng vượt giới hạn.");
    items.put(id, (int) total);
  }

  public synchronized void update(long id, int quantity) {
    valid(id, quantity);
    if (!items.containsKey(id)) throw new BusinessException("Sản phẩm chưa có trong giỏ.");
    items.put(id, quantity);
  }

  public synchronized void remove(long id) {
    items.remove(id);
  }

  public synchronized void clear() {
    items.clear();
  }

  private void valid(long id, int qty) {
    if (id <= 0 || qty <= 0 || qty > 999999999)
      throw new BusinessException("Số lượng phải từ 1 đến 999999999.");
  }
}
