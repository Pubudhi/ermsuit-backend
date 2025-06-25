package com.example.ermsuit.dto;

import com.example.ermsuit.entity.Report;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {
    
    @NotBlank(message = "Report name is required")
    private String name;
    
    @NotBlank(message = "Report description is required")
    private String description;
    
    @NotNull(message = "Report type is required")
    private Report.ReportType type;
    
    @NotNull(message = "Data source ID is required")
    private Long dataSourceId;
    
    @NotNull(message = "Template ID is required")
    private Long templateId;
}
