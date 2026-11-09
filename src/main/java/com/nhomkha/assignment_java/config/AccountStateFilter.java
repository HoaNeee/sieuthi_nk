package com.nhomkha.assignment_java.config;

import com.nhomkha.assignment_java.repository.EmployeeRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Invalidates stale roles, locked accounts and sessions after a password reset. */
public class AccountStateFilter extends OncePerRequestFilter {
  private final EmployeeRepository repo;

  public AccountStateFilter(EmployeeRepository repo) {
    this.repo = repo;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof EmployeePrincipal principal) {
      try {
        var e = repo.byUsername(principal.getUsername());
        if (e.isEmpty()
            || !e.get().isActive()
            || e.get().getId() != principal.getId()
            || !e.get().getRole().equals(principal.isAdmin() ? "ADMIN" : "STAFF")
            || !e.get().getPasswordHash().equals(principal.getCredentialVersion())) {
          SecurityContextHolder.clearContext();
          if (request.getSession(false) != null) request.getSession(false).invalidate();
          response.sendRedirect(request.getContextPath() + "/login?expired");
          return;
        }
      } catch (SQLException ex) {
        response.sendError(503);
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
