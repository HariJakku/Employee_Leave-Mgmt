package com.erp.aspect;

import com.erp.annotation.Auditable;
import com.erp.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Spring AOP aspect intercepting methods annotated with @Auditable
 * to record immutable audit logs.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @AfterReturning(value = "@annotation(auditable)", returning = "result")
    public void logSuccessfulAction(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            String action = auditable.action();
            String entityType = auditable.entityType();
            String description = auditable.description().isEmpty()
                ? "Executed method " + joinPoint.getSignature().getName()
                : auditable.description();

            auditLogService.logAction(action, entityType, null, description);
        } catch (Exception e) {
            log.error("Error in AuditLogAspect: {}", e.getMessage());
        }
    }
}
