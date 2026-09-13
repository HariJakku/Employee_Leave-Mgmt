package com.erp.service;

import com.erp.dto.response.AdminDashboardDto;
import com.erp.dto.response.EmployeeDashboardDto;
import com.erp.dto.response.HRDashboardDto;
import com.erp.dto.response.ManagerDashboardDto;

public interface DashboardService {
    AdminDashboardDto getAdminDashboard();
    HRDashboardDto getHRDashboard();
    ManagerDashboardDto getManagerDashboard();
    EmployeeDashboardDto getEmployeeDashboard();
}
