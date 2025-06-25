package com.example.ermsuit.service;

import com.example.ermsuit.entity.Report;
import com.example.ermsuit.entity.ReportTemplate;
import com.example.ermsuit.entity.User;
import com.example.ermsuit.exception.ResourceNotFoundException;
import com.example.ermsuit.repository.ReportTemplateRepository;
import com.example.ermsuit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportTemplateService {

    private final ReportTemplateRepository reportTemplateRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;

    public ReportTemplate createTemplate(String name, String description, Report.ReportType type, MultipartFile file) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Store the template file
        String templatePath = fileStorageService.storeTemplateFile(file);

        // Create template entity
        ReportTemplate template = ReportTemplate.builder()
                .name(name)
                .description(description)
                .type(type)
                .templatePath(templatePath)
                .createdBy(currentUser)
                .build();

        // Save to database
        ReportTemplate savedTemplate = reportTemplateRepository.save(template);
        
        // Log the event
        auditService.logEvent("TEMPLATE_CREATED", 
                "Report template created: " + name, 
                "ReportTemplate", 
                savedTemplate.getId(), 
                currentUser.getUsername());
        
        return savedTemplate;
    }

    public ReportTemplate getTemplateById(Long id) {
        return reportTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report template not found with id: " + id));
    }

    public List<ReportTemplate> getAllTemplates() {
        return reportTemplateRepository.findAll();
    }

    public List<ReportTemplate> getTemplatesByType(Report.ReportType type) {
        return reportTemplateRepository.findByType(type);
    }
}
