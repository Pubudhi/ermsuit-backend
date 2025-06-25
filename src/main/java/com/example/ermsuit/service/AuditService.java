package com.example.ermsuit.service;

import com.example.ermsuit.entity.AuditLog;
import com.example.ermsuit.entity.User;
import com.example.ermsuit.repository.AuditLogRepository;
import com.example.ermsuit.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public void logEvent(String action, String details, String entityType, Long entityId, String username) {
        User user = null;
        
        // If username is provided, use it
        if (username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            user = userOpt.orElse(null);
        } else {
            // Otherwise try to get from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                    !authentication.getPrincipal().equals("anonymousUser")) {
                if (authentication.getPrincipal() instanceof User) {
                    user = (User) authentication.getPrincipal();
                } else if (authentication.getName() != null) {
                    Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
                    user = userOpt.orElse(null);
                }
            }
        }

        String ipAddress = getClientIpAddress();

        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .details(details)
                .entityType(entityType)
                .entityId(entityId)
                .user(user)
                .ipAddress(ipAddress)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAuditLogsByUser(User user) {
        return auditLogRepository.findByUser(user);
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByCreatedAtBetween(start, end);
    }

    public List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Ignore and return unknown
        }
        return "unknown";
    }
}
