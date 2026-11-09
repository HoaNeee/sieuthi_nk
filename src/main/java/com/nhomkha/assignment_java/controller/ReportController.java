package com.nhomkha.assignment_java.controller;

import com.nhomkha.assignment_java.service.ReportService;
import java.sql.SQLException;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ReportController {
  private final ReportService reports;

  public ReportController(ReportService reports) {
    this.reports = reports;
  }

  @GetMapping("/reports")
  String reports(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      Model m)
      throws SQLException {
    if (to == null) to = LocalDate.now();
    if (from == null) from = to.withDayOfMonth(1);
    reports.validate(from, to);
    m.addAttribute("from", from);
    m.addAttribute("to", to);
    m.addAttribute("revenue", reports.revenue(from, to));
    m.addAttribute("sales", reports.sales(from, to));
    m.addAttribute("stock", reports.stock());
    return "reports/index";
  }
}
