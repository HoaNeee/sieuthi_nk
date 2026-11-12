package com.nhomkha.assignment_java.service;

import com.nhomkha.assignment_java.dto.*;
import com.nhomkha.assignment_java.exception.BusinessException;
import com.nhomkha.assignment_java.model.*;
import com.nhomkha.assignment_java.repository.*;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {
  private final ProductRepository products;
  private final DirectoryRepository directory;

  public CatalogService(ProductRepository products, DirectoryRepository directory) {
    this.products = products;
    this.directory = directory;
  }

  public List<Product> search(String q, Long category, int page) throws SQLException {
    return products.search(q, category, Math.max(0, page));
  }

  public Product product(long id) throws SQLException {
    return products.find(id).orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm."));
  }

  public void saveProduct(Long id, ProductForm f) throws SQLException {
    products.save(id, f);
  }

  public List<Product> importChoices() throws SQLException {
    return products.importChoices();
  }

  public List<Category> categories() throws SQLException {
    return directory.categories();
  }

  public List<Supplier> suppliers() throws SQLException {
    return directory.suppliers();
  }

  public List<Customer> customers(String q) throws SQLException {
    return directory.customers(q);
  }

  public Category category(long id) throws SQLException {
    return categories().stream()
        .filter(x -> x.getId() == id)
        .findFirst()
        .orElseThrow(() -> new BusinessException("Không tìm thấy danh mục."));
  }

  public Supplier supplier(long id) throws SQLException {
    return suppliers().stream()
        .filter(x -> x.getId() == id)
        .findFirst()
        .orElseThrow(() -> new BusinessException("Không tìm thấy nhà cung cấp."));
  }

  public Customer customer(long id) throws SQLException {
    return customers("").stream()
        .filter(x -> x.getId() == id)
        .findFirst()
        .orElseThrow(() -> new BusinessException("Không tìm thấy khách hàng."));
  }

  public void saveCategory(Long id, CategoryForm f) throws SQLException {
    directory.saveCategory(id, f);
  }

  public void saveSupplier(Long id, SupplierForm f) throws SQLException {
    directory.saveSupplier(id, f);
  }

  public long createCustomer(CustomerForm f) throws SQLException { return directory.createCustomer(f); }

  public void saveCustomer(Long id, CustomerForm f) throws SQLException {
    directory.saveCustomer(id, f);
  }
}
