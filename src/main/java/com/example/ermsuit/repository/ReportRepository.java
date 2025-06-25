package com.example.ermsuit.repository;

import com.example.ermsuit.entity.Report;
import com.example.ermsuit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUser(User user);
    List<Report> findByUserAndType(User user, Report.ReportType type);
}
