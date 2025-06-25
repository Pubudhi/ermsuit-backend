package com.example.ermsuit.dto;

import com.example.ermsuit.entity.ReportSchedule;
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
public class ReportScheduleRequest {
    
    @NotBlank(message = "Schedule name is required")
    private String name;
    
    @NotBlank(message = "Schedule description is required")
    private String description;
    
    @NotBlank(message = "Cron expression is required")
    private String cronExpression;
    
    @NotNull(message = "Frequency is required")
    private ReportSchedule.ScheduleFrequency frequency;
    
    @NotNull(message = "Report template ID is required")
    private Long reportTemplateId;
    

    @Builder.Default
    private boolean active = true;
}
