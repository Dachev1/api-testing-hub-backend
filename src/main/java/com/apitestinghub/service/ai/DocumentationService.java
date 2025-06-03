package com.apitestinghub.service.ai;

import com.apitestinghub.dto.request.ApiRequest;
import com.apitestinghub.dto.response.ApiExecutionResponse;
import com.apitestinghub.service.ai.ClientService;
import com.apitestinghub.service.ai.PromptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service for AI-powered documentation generation.
 * Orchestrates AI operations using specialized services.
 */
@Service
public class DocumentationService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentationService.class);

    private final ClientService clientService;
    private final PromptService promptService;

    public DocumentationService(ClientService clientService, PromptService promptService) {
        this.clientService = clientService;
        this.promptService = promptService;
        logger.info("AI Documentation Service initialized");
    }

    @Cacheable(value = "ai-documentation",
            key = "#request.url() + '_' + #request.method()",
            unless = "#result == null or #result.contains('error')")
    public Mono<String> generateDocumentation(ApiRequest request, ApiExecutionResponse response) {
        logger.debug("Generating documentation for {} {}", request.method(), request.url());

        String prompt = promptService.buildDocumentationPrompt(request, response);

        return clientService.executeAiRequest(prompt, "documentation generation")
                .doOnSuccess(result -> logger.info("Documentation generated for {} {}",
                        request.method(), request.url()))
                .doOnError(error -> logger.error("Failed to generate documentation for {} {}: {}",
                        request.method(), request.url(), error.getMessage()));
    }

    @Cacheable(value = "ai-descriptions",
            key = "#request.url() + '_' + #request.method()",
            unless = "#result == null or #result.contains('error')")
    public Mono<String> generateApiDescription(ApiRequest request) {
        logger.debug("Generating description for {} {}", request.method(), request.url());

        String prompt = promptService.buildDescriptionPrompt(request);

        return clientService.executeAiRequest(prompt, "description generation")
                .doOnSuccess(result -> logger.info("Description generated for {} {}",
                        request.method(), request.url()))
                .doOnError(error -> logger.error("Failed to generate description for {} {}: {}",
                        request.method(), request.url(), error.getMessage()));
    }

    @Cacheable(value = "ai-analysis",
            key = "#response.statusCode() + '_' + #response.responseTimeMs() + '_' + T(java.util.Objects).hash(#response.body())",
            unless = "#result == null or #result.contains('error')")
    public Mono<String> analyzeResponse(ApiExecutionResponse response) {
        logger.debug("Analyzing response - Status: {}, Time: {}ms",
                response.statusCode(), response.responseTimeMs());

        String prompt = promptService.buildAnalysisPrompt(response);

        return clientService.executeAiRequest(prompt, "response analysis")
                .doOnSuccess(result -> logger.info("Analysis completed for status {} ({}ms)",
                        response.statusCode(), response.responseTimeMs()))
                .doOnError(error -> logger.error("Failed to analyze response (status {}): {}",
                        response.statusCode(), error.getMessage()));
    }
}