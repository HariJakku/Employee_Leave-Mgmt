package com.erp.controller;

import com.erp.dto.response.AdminDashboardDto;
import com.erp.dto.response.EmployeeDashboardDto;
import com.erp.dto.response.HRDashboardDto;
import com.erp.dto.response.ManagerDashboardDto;
import com.erp.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboards", description = "Role-based aggregate metrics and real-time dashboard analytics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admin dashboard metrics and charts data")
    public ResponseEntity<AdminDashboardDto> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/hr")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get HR operational dashboard metrics")
    public ResponseEntity<HRDashboardDto> getHRDashboard() {
        return ResponseEntity.ok(dashboardService.getHRDashboard());
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get manager team overview and pending approvals")
    public ResponseEntity<ManagerDashboardDto> getManagerDashboard() {
        return ResponseEntity.ok(dashboardService.getManagerDashboard());
    }

    @GetMapping("/employee")
    @Operation(summary = "Get employee self-service dashboard summary")
    public ResponseEntity<EmployeeDashboardDto> getEmployeeDashboard() {
        return ResponseEntity.ok(dashboardService.getEmployeeDashboard());
    }
}
