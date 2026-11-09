package com.nhomkha.assignment_java.config;

import com.nhomkha.assignment_java.service.EmployeeService;
import java.util.Arrays;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Run from a real terminal; never pass a password on the command line. */
public class PasswordHashTool {
  public static void main(String[] args) {
    var console = System.console();
    if (console == null)
      throw new IllegalStateException("Run in a terminal with an interactive console.");
    char[] secret = console.readPassword("Admin password (10+ characters): ");
    try {
      String value = new String(secret);
      EmployeeService.validatePassword(value);
      console.printf("%s%n", new BCryptPasswordEncoder().encode(value));
    } finally {
      Arrays.fill(secret, '\0');
    }
  }
}
