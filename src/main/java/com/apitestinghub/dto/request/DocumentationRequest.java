package com.apitestinghub.dto.request;

import com.apitestinghub.dto.response.ApiExecutionResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocumentationRequest(
        @Valid ApiRequest request,
        @Valid ApiExecutionResponse response
) { }
