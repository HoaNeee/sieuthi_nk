package com.nhomkha.assignment_java.service;

import com.nhomkha.assignment_java.dto.ReportRow;
import com.nhomkha.assignment_java.exception.BusinessException;
import com.nhomkha.assignment_java.repository.ReportRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
  private final ReportRepository repo;

  public ReportService(ReportRepository repo) {
    this.repo = repo;
  }

  public void validate(LocalDate from, LocalDate to) {
    if (from.isAfter(to) || from.getYear() < 1900 || to.getYear() > 9998)
      throw new BusinessException("Khoảng ngày báo cáo không hợp lệ.");
  }

  public List<ReportRow> revenue(LocalDate from, LocalDate to) throws SQLException {
    validate(from, to);
    return repo.revenue(from, to);
  }

  public List<ReportRow> sales(LocalDate from, LocalDate to) throws SQLException {
    validate(from, to);
    return repo.sales(from, to);
  }

  public List<ReportRow> stock() throws SQLException {
    return repo.stock();
  }
}
