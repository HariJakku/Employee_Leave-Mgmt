package com.erp.service.impl;

import com.erp.dto.response.AIChatResponseDto;
import com.erp.dto.response.StructuredLeaveSuggestionDto;
import com.erp.entity.*;
import com.erp.enums.AttendanceStatus;
import com.erp.exception.ResourceNotFoundException;
import com.erp.repository.*;
import com.erp.security.SecurityUtils;
import com.erp.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {

    @Value("${app.ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Value("${app.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String geminiBaseUrl;

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public AIChatResponseDto chatWithLeaveAssistant(String userPrompt) {
        User currentUser = securityUtils.getCurrentUser();
        Employee employee = currentUser.getEmployee();

        int year = LocalDate.now().getYear();
        List<LeaveBalance> balances = employee != null
            ? leaveBalanceRepository.findActiveBalancesByEmployeeAndYear(employee.getId(), year)
            : List.of();

        List<LeaveType> leaveTypes = leaveTypeRepository.findByIsActiveTrue();

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Current Date: ").append(LocalDate.now()).append("\n");
        if (employee != null) {
            contextBuilder.append("Employee Name: ").append(employee.getFullName()).append("\n");
            contextBuilder.append("Current Year: ").append(year).append("\n");
            contextBuilder.append("Employee Leave Balances:\n");
            for (LeaveBalance b : balances) {
                contextBuilder.append(String.format("- %s: %s allocated, %s used, %s remaining available\n",
                    b.getLeaveType().getName(), b.getAllocatedDays(), b.getUsedDays(), b.getRemainingDays()));
            }
        }
        contextBuilder.append("Company Leave Policies:\n");
        for (LeaveType lt : leaveTypes) {
            contextBuilder.append(String.format("- %s: max %d days/year, %s, notice period: %d days. Description: %s\n",
                lt.getName(), lt.getMaxDaysPerYear(), lt.getIsPaid() ? "Paid" : "Unpaid", lt.getMinNoticeDays(), lt.getDescription()));
        }

        String systemInstruction = "You are an intelligent, polite AI Leave Assistant for an enterprise ERP system. " +
            "Answer the employee's questions accurately using the provided ground-truth database records. " +
            "If they ask about their remaining balance or policy, cite the exact numbers from the context.";

        String reply = callGeminiOrFallback(systemInstruction, contextBuilder.toString(), userPrompt, () -> {
            // Intelligent fallback with real DB balance
            String lower = userPrompt.toLowerCase();
            if (employee != null && (lower.contains("remaining") || lower.contains("balance") || lower.contains("how many"))) {
                for (LeaveBalance b : balances) {
                    if (lower.contains(b.getLeaveType().getName().toLowerCase())) {
                        return String.format("You currently have **%s days** of %s remaining for %d (%s allocated, %s used, %s pending).",
                            b.getRemainingDays(), b.getLeaveType().getName(), year, b.getAllocatedDays(), b.getUsedDays(), b.getPendingDays());
                    }
                }
                StringBuilder sb = new StringBuilder("Here is your current leave balance breakdown:\n");
                for (LeaveBalance b : balances) {
                    sb.append(String.format("- **%s**: %s remaining (out of %s)\n",
                        b.getLeaveType().getName(), b.getRemainingDays(), b.getAllocatedDays()));
                }
                return sb.toString();
            }
            return "Company policy provides Casual Leave (12 days), Sick Leave (10 days), and Earned Leave (15 days) per calendar year. Leaves require advance manager approval.";
        });

        return AIChatResponseDto.builder()
            .reply(reply)
            .modelUsed(hasValidApiKey() ? geminiModel : "RuleEngine-Fallback")
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StructuredLeaveSuggestionDto parseLeaveRequest(String naturalLanguageText) {
        List<LeaveType> leaveTypes = leaveTypeRepository.findByIsActiveTrue();
        LocalDate today = LocalDate.now();

        // Default heuristic parse
        Long matchedTypeId = leaveTypes.isEmpty() ? null : leaveTypes.get(0).getId();
        String matchedTypeName = leaveTypes.isEmpty() ? "Casual Leave" : leaveTypes.get(0).getName();

        String lower = naturalLanguageText.toLowerCase();
        for (LeaveType lt : leaveTypes) {
            if (lower.contains(lt.getName().toLowerCase()) ||
               (lt.getName().toLowerCase().contains("sick") && lower.contains("sick")) ||
               (lt.getName().toLowerCase().contains("casual") && (lower.contains("vacation") || lower.contains("personal")))) {
                matchedTypeId = lt.getId();
                matchedTypeName = lt.getName();
                break;
            }
        }

        LocalDate startDate = today.plusDays(1);
        LocalDate endDate = today.plusDays(3);

        if (lower.contains("tomorrow")) {
            startDate = today.plusDays(1);
            endDate = today.plusDays(1);
        } else if (lower.contains("next week")) {
            startDate = today.plusDays(7);
            endDate = today.plusDays(9);
        }

        int days = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        if (hasValidApiKey()) {
            try {
                String prompt = String.format("""
                    Extract structured leave parameters from the following employee request:
                    "%s"
                    
                    Today is %s.
                    Available leave types: %s
                    
                    Output strictly a JSON object with keys:
                    {"leaveTypeName": string, "startDate": "YYYY-MM-DD", "endDate": "YYYY-MM-DD", "reason": string}
                    Do not wrap in markdown or backticks.
                    """, naturalLanguageText, today,
                    leaveTypes.stream().map(LeaveType::getName).collect(Collectors.joining(", ")));

                String raw = callGeminiRaw(prompt);
                if (raw != null) {
                    String cleanJson = raw.replaceAll("```json", "").replaceAll("```", "").trim();
                    JsonNode root = objectMapper.readTree(cleanJson);
                    String extractedName = root.path("leaveTypeName").asText(matchedTypeName);
                    String sDate = root.path("startDate").asText(startDate.toString());
                    String eDate = root.path("endDate").asText(endDate.toString());
                    String parsedReason = root.path("reason").asText(naturalLanguageText);

                    for (LeaveType lt : leaveTypes) {
                        if (lt.getName().equalsIgnoreCase(extractedName)) {
                            matchedTypeId = lt.getId();
                            matchedTypeName = lt.getName();
                            break;
                        }
                    }

                    LocalDate parsedStart = LocalDate.parse(sDate);
                    LocalDate parsedEnd = LocalDate.parse(eDate);
                    int calculatedDays = (int) ChronoUnit.DAYS.between(parsedStart, parsedEnd) + 1;

                    return StructuredLeaveSuggestionDto.builder()
                        .leaveTypeId(matchedTypeId)
                        .leaveTypeName(matchedTypeName)
                        .startDate(parsedStart)
                        .endDate(parsedEnd)
                        .totalDays(calculatedDays)
                        .reason(parsedReason)
                        .explanation("Extracted accurately using Gemini AI. Please verify before submitting.")
                        .build();
                }
            } catch (Exception e) {
                log.warn("Gemini parse failed, using fallback heuristic: {}", e.getMessage());
            }
        }

        return StructuredLeaveSuggestionDto.builder()
            .leaveTypeId(matchedTypeId)
            .leaveTypeName(matchedTypeName)
            .startDate(startDate)
            .endDate(endDate)
            .totalDays(days)
            .reason(naturalLanguageText)
            .explanation("Draft parsed from your description. Review and adjust dates before confirming.")
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AIChatResponseDto queryHRAssistant(String query) {
        LocalDate today = LocalDate.now();
        long totalEmp = employeeRepository.count();
        long onLeaveToday = leaveRequestRepository.findCurrentlyOnLeave(today).size();
        long pendingApprovals = leaveRequestRepository.countByStatus(com.erp.enums.LeaveStatus.PENDING)
            + leaveRequestRepository.countByStatus(com.erp.enums.LeaveStatus.MANAGER_APPROVED);

        List<Department> departments = departmentRepository.findAll();
        StringBuilder context = new StringBuilder();
        context.append(String.format("Total workforce: %d. Currently on leave today: %d. Total pending leave approvals: %d.\n",
            totalEmp, onLeaveToday, pendingApprovals));
        context.append("Department headcount:\n");
        for (Department d : departments) {
            long count = departmentRepository.countEmployeesByDepartmentId(d.getId());
            context.append(String.format("- %s: %d employees\n", d.getName(), count));
        }

        String system = "You are an executive HR Intelligence AI assistant for company leadership. " +
            "Summarize HR analytics, departmental capacity, and leave trends clearly and concisely.";

        String reply = callGeminiOrFallback(system, context.toString(), query, () ->
            String.format("Currently, **%d employees** are on leave today out of a workforce of **%d**. There are **%d pending leave approvals** awaiting review. Operational coverage remains stable across all departments.",
                onLeaveToday, totalEmp, pendingApprovals)
        );

        return AIChatResponseDto.builder()
            .reply(reply)
            .modelUsed(hasValidApiKey() ? geminiModel : "RuleEngine-Fallback")
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AIChatResponseDto generateEmployeeSummary(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        int year = LocalDate.now().getYear();
        List<LeaveBalance> balances = leaveBalanceRepository.findActiveBalancesByEmployeeAndYear(employeeId, year);
        long leavesThisYear = leaveRequestRepository.countCurrentLeavesByEmployee(employeeId);

        StringBuilder context = new StringBuilder();
        context.append(String.format("Employee: %s (Code: %s)\n", emp.getFullName(), emp.getEmployeeCode()));
        context.append(String.format("Department: %s, Designation: %s\n",
            emp.getDepartment() != null ? emp.getDepartment().getName() : "None", emp.getDesignation()));
        context.append(String.format("Joining Date: %s, Status: %s\n", emp.getJoiningDate(), emp.getStatus()));
        context.append("Leave balances for ").append(year).append(":\n");
        for (LeaveBalance b : balances) {
            context.append(String.format("- %s: %s remaining out of %s\n",
                b.getLeaveType().getName(), b.getRemainingDays(), b.getAllocatedDays()));
        }

        String system = "You are an executive HR consultant. Provide a professional, concise 2-paragraph performance and tenure summary for this employee based on their job role and records.";

        String reply = callGeminiOrFallback(system, context.toString(), "Generate HR executive summary", () ->
            String.format("%s serves as %s in the %s department. Since joining on %s, the employee has maintained an %s standing with %s leave records filed this period.",
                emp.getFullName(), emp.getDesignation(),
                emp.getDepartment() != null ? emp.getDepartment().getName() : "General",
                emp.getJoiningDate(), emp.getStatus(), leavesThisYear)
        );

        return AIChatResponseDto.builder()
            .reply(reply)
            .modelUsed(hasValidApiKey() ? geminiModel : "RuleEngine-Fallback")
            .build();
    }

    private boolean hasValidApiKey() {
        return geminiApiKey != null && !geminiApiKey.isBlank() && !geminiApiKey.equals("test-key");
    }

    private String callGeminiOrFallback(String systemInstruction, String context, String userPrompt, java.util.function.Supplier<String> fallback) {
        if (!hasValidApiKey()) {
            return fallback.get();
        }

        try {
            String combinedPrompt = String.format("%s\n\nContext Data:\n%s\n\nUser Question:\n%s",
                systemInstruction, context, userPrompt);
            String raw = callGeminiRaw(combinedPrompt);
            return raw != null ? raw : fallback.get();
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            return fallback.get();
        }
    }

    private String callGeminiRaw(String textPrompt) {
        try {
            WebClient client = WebClient.builder()
                .baseUrl(geminiBaseUrl)
                .build();

            Map<String, Object> body = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", textPrompt)
                    ))
                )
            );

            String uri = String.format("/models/%s:generateContent?key=%s", geminiModel, geminiApiKey);

            String response = client.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                return candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            }
        } catch (Exception e) {
            log.error("Gemini raw request error: {}", e.getMessage());
        }
        return null;
    }
}
