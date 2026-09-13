package com.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatRequestDto {

    @NotBlank(message = "Prompt message cannot be empty")
    private String prompt;

    private String contextType; // "LEAVE_ASSISTANT" | "HR_QUERY"
}
