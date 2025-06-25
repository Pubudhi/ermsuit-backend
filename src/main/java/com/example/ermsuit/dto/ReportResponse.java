package com.example.ermsuit.dto;

import com.example.ermsuit.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {
    private Long id;
    private String name;
    private String description;
    private String type;
    private String filePath;
    private LocalDateTime createdAt;
    private String createdBy;
    private String dataSource;
    private String templateName;
    
    public static ReportResponse fromEntity(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .name(report.getName())
                .description(report.getDescription())
                .type(report.getType().name())
                .filePath(report.getFilePath())
                .createdAt(report.getCreatedAt())
                .createdBy(report.getUser().getUsername())
                .dataSource(report.getDataSource())
                .templateName(report.getTemplateName())
                .build();
    }
}
