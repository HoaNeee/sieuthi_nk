package com.nhomkha.assignment_java;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class AssignmentJavaApplication {

  public static void main(String[] args) {
    SpringApplication.run(AssignmentJavaApplication.class, args);
  }


  @Bean
  CommandLineRunner checkOracle(JdbcTemplate jdbcTemplate) {
    return args -> {
      Integer count = jdbcTemplate.queryForObject("select count(*) from SAN_PHAM", Integer.class);
      System.out.println("Number of products in the database: " + count);
    };
  }
}
