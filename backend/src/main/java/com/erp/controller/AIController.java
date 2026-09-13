package com.erp.controller;

import com.erp.dto.request.AIChatRequestDto;
import com.erp.dto.response.AIChatResponseDto;
import com.erp.dto.response.StructuredLeaveSuggestionDto;
import com.erp.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Operations", description = "Endpoints for LLM-assisted leave advisory, natural language form extraction, and HR analytics")
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    @Operation(summary = "AI Leave Assistant Chat", description = "Answers leave balance and company policy questions grounded in database records")
    public ResponseEntity<AIChatResponseDto> chatWithLeaveAssistant(@Valid @RequestBody AIChatRequestDto request) {
        return ResponseEntity.ok(aiService.chatWithLeaveAssistant(request.getPrompt()));
    }

    @PostMapping("/generate-leave-request")
    @Operation(summary = "Parse natural language into structured leave request", description = "Transforms natural descriptions into a structured leave draft with dates and days calculation")
    public ResponseEntity<StructuredLeaveSuggestionDto> parseLeaveRequest(@Valid @RequestBody AIChatRequestDto request) {
        return ResponseEntity.ok(aiService.parseLeaveRequest(request.getPrompt()));
    }

    @PostMapping("/hr-query")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "AI HR Analytics Query", description = "Answers executive questions about workforce presence, leaves, and departmental distribution (HR/Admin only)")
    public ResponseEntity<AIChatResponseDto> queryHRAssistant(@Valid @RequestBody AIChatRequestDto request) {
        return ResponseEntity.ok(aiService.queryHRAssistant(request.getPrompt()));
    }

    @GetMapping("/employee-summary/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "AI Employee Profile Summary", description = "Generates an executive tenure and activity summary for an employee (HR/Admin only)")
    public ResponseEntity<AIChatResponseDto> generateEmployeeSummary(@PathVariable Long employeeId) {
        return ResponseEntity.ok(aiService.generateEmployeeSummary(employeeId));
    }
}
