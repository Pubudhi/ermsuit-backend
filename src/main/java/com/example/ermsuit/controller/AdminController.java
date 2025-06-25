package com.example.ermsuit.controller;

import com.example.ermsuit.dto.UserRegistrationRequest;
import com.example.ermsuit.dto.UserResponse;
import com.example.ermsuit.entity.AuditLog;
import com.example.ermsuit.service.AuditService;
import com.example.ermsuit.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AuditService auditService;

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createAdminUser(@Valid @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.ok(userService.createAdminUser(request));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        if (start != null && end != null) {
            return ResponseEntity.ok(auditService.getAuditLogsByDateRange(start, end));
        } else {
            // Return all logs if no date range specified
            return ResponseEntity.ok(auditService.getAuditLogsByDateRange(
                    LocalDateTime.now().minusMonths(1), LocalDateTime.now()));
        }
    }

    @GetMapping("/audit-logs/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(auditService.getAuditLogsByEntity(entityType, entityId));
    }
}
