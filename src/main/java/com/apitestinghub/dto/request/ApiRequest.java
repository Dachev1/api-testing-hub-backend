package com.apitestinghub.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiRequest(
        @NotBlank(message = "HTTP method is required")
        @Pattern(regexp = "GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS", message = "Invalid HTTP method")
        String method,

        @NotBlank(message = "URL is required")
        String url,

        Map<String, String> headers,

        Map<String, String> queryParams,

        String body,

        @NotNull(message = "Session ID is required")
        String sessionId,

        Integer timeoutMs,

        Boolean followRedirects
) { }
