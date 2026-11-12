package com.nhomkha.assignment_java;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "DB_PASSWORD=test-only-not-a-real-password")
class AssignmentJavaApplicationTests {
  @org.springframework.test.context.bean.override.mockito.MockitoBean(name = "checkOracle")
  org.springframework.boot.CommandLineRunner startupCheck;

  @Test
  void contextLoads() {}
}
