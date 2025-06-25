package com.example.ermsuit.controller;

import com.example.ermsuit.entity.Report;
import com.example.ermsuit.entity.ReportTemplate;
import com.example.ermsuit.service.ReportTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/report-templates")
@RequiredArgsConstructor
public class ReportTemplateController {

    private final ReportTemplateService reportTemplateService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportTemplate> createTemplate(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("type") Report.ReportType type,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(reportTemplateService.createTemplate(name, description, type, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportTemplate> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(reportTemplateService.getTemplateById(id));
    }

    @GetMapping
    public ResponseEntity<List<ReportTemplate>> getAllTemplates() {
        return ResponseEntity.ok(reportTemplateService.getAllTemplates());
    }

    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<ReportTemplate>> getTemplatesByType(@PathVariable Report.ReportType type) {
        return ResponseEntity.ok(reportTemplateService.getTemplatesByType(type));
    }
}
