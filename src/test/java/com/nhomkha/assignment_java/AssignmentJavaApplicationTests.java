package com.nhomkha.assignment_java;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "DB_PASSWORD=test-only-not-a-real-password")
class AssignmentJavaApplicationTests {

  @Test
  void contextLoads() {}
}
