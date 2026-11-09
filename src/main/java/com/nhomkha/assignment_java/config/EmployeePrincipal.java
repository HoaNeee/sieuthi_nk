package com.nhomkha.assignment_java.config;

import com.nhomkha.assignment_java.model.Employee;
import org.springframework.security.core.userdetails.User;

public class EmployeePrincipal extends User {
  private final long id;
  private final String credentialVersion;

  public EmployeePrincipal(Employee e) {
    super(
        e.getUsername(),
        e.getPasswordHash(),
        e.isActive(),
        true,
        true,
        true,
        java.util.List.of(
            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                "ROLE_" + e.getRole())));
    id = e.getId();
    credentialVersion = e.getPasswordHash();
  }

  public long getId() {
    return id;
  }

  public String getCredentialVersion() {
    return credentialVersion;
  }

  public boolean isAdmin() {
    return getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }
}
