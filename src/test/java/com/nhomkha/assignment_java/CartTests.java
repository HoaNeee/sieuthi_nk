package com.nhomkha.assignment_java;

import static org.junit.jupiter.api.Assertions.*;

import com.nhomkha.assignment_java.exception.BusinessException;
import com.nhomkha.assignment_java.model.Cart;
import org.junit.jupiter.api.Test;

class CartTests {
  @Test
  void mergesProductsAndRejectsInvalidQuantity() {
    var cart = new Cart(1);
    cart.add(2, 3);
    cart.add(2, 2);
    assertEquals(5, cart.snapshot().get(2L));
    assertThrows(BusinessException.class, () -> cart.add(2, 0));
    cart.update(2, 1);
    assertEquals(1, cart.snapshot().get(2L));
  }

  @Test
  void sessionsAreIndependentAndSnapshotsCannotMutateCart() {
    var a = new Cart(1);
    var b = new Cart(2);
    a.add(1, 2);
    a.snapshot().clear();
    assertEquals(1, a.snapshot().size());
    assertTrue(b.snapshot().isEmpty());
  }

  @Test
  void quantityOverflowRejected() {
    var a = new Cart(1);
    a.add(1, 999999999);
    assertThrows(BusinessException.class, () -> a.add(1, 1));
  }
}
