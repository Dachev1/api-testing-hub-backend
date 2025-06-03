package com.apitestinghub.service.ai;

import com.apitestinghub.dto.request.ApiRequest;
import com.apitestinghub.dto.response.ApiExecutionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service responsible for formatting and managing AI prompts.
 */
@Service
public class PromptService {

    private static final int MAX_BODY_LENGTH = 5000;

    private final ObjectMapper objectMapper;

    @Value("${app.ai.prompts.documentation}")
    private String documentationPrompt;

    @Value("${app.ai.prompts.description}")
    private String descriptionPrompt;

    @Value("${app.ai.prompts.analysis}")
    private String analysisPrompt;

    public PromptService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildDocumentationPrompt(ApiRequest request, ApiExecutionResponse response) {
        return String.format(documentationPrompt,
                request.method(), request.url(), formatHeaders(request.headers()),
                formatQueryParams(request.queryParams()), formatBody(request.body()),
                response.statusCode(), response.statusText(), formatHeaders(response.headers()),
                formatBody(response.body()), response.responseTimeMs());
    }

    public String buildDescriptionPrompt(ApiRequest request) {
        return String.format(descriptionPrompt,
                request.method(), request.url(), formatHeaders(request.headers()),
                formatQueryParams(request.queryParams()), formatBody(request.body()));
    }

    public String buildAnalysisPrompt(ApiExecutionResponse response) {
        return String.format(analysisPrompt,
                response.statusCode(), response.statusText(), formatHeaders(response.headers()),
                formatBody(response.body()), response.responseTimeMs());
    }

    private String formatHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) return "{}";
        return objectMapper.valueToTree(headers).toString();
    }

    private String formatQueryParams(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) return "{}";
        return objectMapper.valueToTree(queryParams).toString();
    }

    private String formatBody(String body) {
        if (body == null || body.trim().isEmpty()) return "(empty)";
        return body.length() > MAX_BODY_LENGTH ?
                body.substring(0, MAX_BODY_LENGTH - 3) + "..." : body;
    }
}