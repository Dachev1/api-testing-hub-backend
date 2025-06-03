package com.apitestinghub.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiExecutionResponse(
    int statusCode,
    String statusText,
    Map<String, String> headers,
    String body,
    long responseTimeMs,
    LocalDateTime timestamp,
    String requestId,
    boolean success
) { }
