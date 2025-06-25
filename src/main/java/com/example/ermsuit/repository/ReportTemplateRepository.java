package com.example.ermsuit.repository;

import com.example.ermsuit.entity.Report;
import com.example.ermsuit.entity.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {
    List<ReportTemplate> findByType(Report.ReportType type);
    Optional<ReportTemplate> findByName(String name);
}
