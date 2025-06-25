package com.example.ermsuit.controller;

import com.example.ermsuit.dto.ReportScheduleRequest;
import com.example.ermsuit.entity.ReportSchedule;
import com.example.ermsuit.service.ReportScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report-schedules")
@RequiredArgsConstructor
public class ReportScheduleController {

    private final ReportScheduleService reportScheduleService;

    @PostMapping
    public ResponseEntity<ReportSchedule> createSchedule(@Valid @RequestBody ReportScheduleRequest request) {
        return ResponseEntity.ok(reportScheduleService.createSchedule(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportSchedule> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(reportScheduleService.getScheduleById(id));
    }

    @GetMapping
    public ResponseEntity<List<ReportSchedule>> getAllSchedules() {
        return ResponseEntity.ok(reportScheduleService.getAllSchedules());
    }

    @GetMapping("/my-schedules")
    public ResponseEntity<List<ReportSchedule>> getMySchedules() {
        return ResponseEntity.ok(reportScheduleService.getSchedulesByCurrentUser());
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ReportSchedule> toggleScheduleActive(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(reportScheduleService.toggleScheduleActive(id, active));
    }
}
