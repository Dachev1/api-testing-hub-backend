package com.apitestinghub.dto.request;

import com.apitestinghub.dto.response.ApiExecutionResponse;
import jakarta.validation.Valid;

public record DocumentationGenerationRequest(
        @Valid ApiRequest apiRequest,
        @Valid ApiExecutionResponse apiResponse
) {}