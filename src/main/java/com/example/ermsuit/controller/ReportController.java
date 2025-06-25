package com.example.ermsuit.controller;

import com.example.ermsuit.dto.ReportRequest;
import com.example.ermsuit.dto.ReportResponse;
import com.example.ermsuit.entity.Report;
import com.example.ermsuit.service.ReportGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportGenerationService reportGenerationService;

    @PostMapping
    public ResponseEntity<ReportResponse> generateReport(@Valid @RequestBody ReportRequest request) throws Exception {
        Report report = reportGenerationService.generateReport(request);
        return ResponseEntity.ok(ReportResponse.fromEntity(report));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable Long id) {
        Report report = reportGenerationService.getReportById(id);
        return ResponseEntity.ok(ReportResponse.fromEntity(report));
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        List<ReportResponse> reports = reportGenerationService.getAllReports().stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/my-reports")
    public ResponseEntity<List<ReportResponse>> getMyReports() {
        List<ReportResponse> reports = reportGenerationService.getReportsByCurrentUser().stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long id) {
        Report report = reportGenerationService.getReportById(id);
        Path path = Paths.get(report.getFilePath());
        Resource resource = new FileSystemResource(path);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName().toString() + "\"")
                .body(resource);
    }
}
