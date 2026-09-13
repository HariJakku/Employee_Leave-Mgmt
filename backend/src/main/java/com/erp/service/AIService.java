package com.erp.service;

import com.erp.dto.response.AIChatResponseDto;
import com.erp.dto.response.StructuredLeaveSuggestionDto;

public interface AIService {
    AIChatResponseDto chatWithLeaveAssistant(String prompt);
    StructuredLeaveSuggestionDto parseLeaveRequest(String prompt);
    AIChatResponseDto queryHRAssistant(String query);
    AIChatResponseDto generateEmployeeSummary(Long employeeId);
}
