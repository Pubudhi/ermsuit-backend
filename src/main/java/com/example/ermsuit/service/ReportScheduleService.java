package com.example.ermsuit.service;

import com.example.ermsuit.dto.ReportRequest;
import com.example.ermsuit.dto.ReportScheduleRequest;
import com.example.ermsuit.entity.Report;
import com.example.ermsuit.entity.ReportSchedule;
import com.example.ermsuit.entity.ReportTemplate;
import com.example.ermsuit.entity.User;
import com.example.ermsuit.exception.ResourceNotFoundException;
import com.example.ermsuit.repository.ReportScheduleRepository;
import com.example.ermsuit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportScheduleService {

    private final ReportScheduleRepository reportScheduleRepository;
    private final UserRepository userRepository;
    private final ReportTemplateService reportTemplateService;
    private final ReportGenerationService reportGenerationService;
    private final AuditService auditService;

    public ReportSchedule createSchedule(ReportScheduleRequest request) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get template
        ReportTemplate template = reportTemplateService.getTemplateById(request.getReportTemplateId());
        
        // Calculate next run time based on cron expression
        LocalDateTime nextRun = calculateNextRunTime(request.getCronExpression());
        
        // Create schedule entity
        ReportSchedule schedule = ReportSchedule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .cronExpression(request.getCronExpression())
                .frequency(request.getFrequency())
                .nextRun(nextRun)
                .active(request.isActive())
                .user(currentUser)
                .reportTemplate(template)
                .build();
        
        // Save to database
        ReportSchedule savedSchedule = reportScheduleRepository.save(schedule);
        
        // Log the event
        auditService.logEvent("SCHEDULE_CREATED", 
                "Report schedule created: " + request.getName(), 
                "ReportSchedule", 
                savedSchedule.getId(), 
                currentUser.getUsername());
        
        return savedSchedule;
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void processScheduledReports() {
        log.info("Processing scheduled reports...");
        
        LocalDateTime now = LocalDateTime.now();
        List<ReportSchedule> dueSchedules = reportScheduleRepository.findByNextRunBeforeAndActiveTrue(now);
        
        for (ReportSchedule schedule : dueSchedules) {
            try {
                log.info("Executing scheduled report: {}", schedule.getName());
                
                // Generate the report
                ReportRequest reportRequest = ReportRequest.builder()
                        .name(schedule.getName() + " - " + now)
                        .description("Scheduled report: " + schedule.getDescription())
                        .type(schedule.getReportTemplate().getType())
                        .templateId(schedule.getReportTemplate().getId())
                        // Note: This is a simplification. In a real system, you'd need to specify
                        // which data source to use for each scheduled report
                        .dataSourceId(1L) // Placeholder
                        .build();
                
                // Set security context for the report generation
                // In a real system, you might use a system user or the schedule owner
                SecurityContextHolder.getContext().setAuthentication(
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                schedule.getUser().getUsername(), null, schedule.getUser().getAuthorities()));
                
                Report generatedReport = reportGenerationService.generateReport(reportRequest);
                
                // Update schedule with last run time and calculate next run
                schedule.setLastRun(now);
                schedule.setNextRun(calculateNextRunTime(schedule.getCronExpression()));
                reportScheduleRepository.save(schedule);
                
                // Log the event
                auditService.logEvent("SCHEDULED_REPORT_GENERATED", 
                        "Scheduled report generated: " + schedule.getName(), 
                        "Report", 
                        generatedReport.getId(), 
                        schedule.getUser().getUsername());
                
            } catch (Exception e) {
                log.error("Error executing scheduled report: {}", schedule.getName(), e);
                
                // Log the error
                auditService.logEvent("SCHEDULED_REPORT_ERROR", 
                        "Error generating scheduled report: " + schedule.getName() + " - " + e.getMessage(), 
                        "ReportSchedule", 
                        schedule.getId(), 
                        schedule.getUser().getUsername());
            }
        }
    }

    private LocalDateTime calculateNextRunTime(String cronExpression) {
        try {
            org.springframework.scheduling.support.CronExpression cron = 
                    org.springframework.scheduling.support.CronExpression.parse(cronExpression);
            LocalDateTime now = LocalDateTime.now();
            return cron.next(now);
        } catch (Exception e) {
            log.error("Error parsing cron expression: {}", cronExpression, e);
            // Return a default time (1 day from now) if parsing fails
            return LocalDateTime.now().plusDays(1);
        }
    }

    public ReportSchedule getScheduleById(Long id) {
        return reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
    }

    public List<ReportSchedule> getAllSchedules() {
        return reportScheduleRepository.findAll();
    }

    public List<ReportSchedule> getSchedulesByCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return reportScheduleRepository.findByUser(currentUser);
    }

    public ReportSchedule toggleScheduleActive(Long id, boolean active) {
        ReportSchedule schedule = getScheduleById(id);
        schedule.setActive(active);
        
        ReportSchedule updatedSchedule = reportScheduleRepository.save(schedule);
        
        // Log the event
        String action = active ? "SCHEDULE_ACTIVATED" : "SCHEDULE_DEACTIVATED";
        auditService.logEvent(action, 
                "Report schedule " + (active ? "activated" : "deactivated") + ": " + schedule.getName(), 
                "ReportSchedule", 
                updatedSchedule.getId(), 
                null);
        
        return updatedSchedule;
    }
}
