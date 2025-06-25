package com.example.ermsuit.repository;

import com.example.ermsuit.entity.ReportSchedule;
import com.example.ermsuit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {
    List<ReportSchedule> findByUser(User user);
    List<ReportSchedule> findByActiveTrue();
    List<ReportSchedule> findByNextRunBeforeAndActiveTrue(LocalDateTime dateTime);
}
