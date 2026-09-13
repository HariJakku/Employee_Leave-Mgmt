package com.erp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to automatically record audit logs via Spring AOP.
 * Usage:
 *   @Auditable(action = "EMPLOYEE_CREATED", entityType = "Employee")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action();
    String entityType() default "";
    String description() default "";
}
