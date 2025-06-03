package com.apitestinghub.controller;

import com.apitestinghub.dto.request.ApiRequest;
import com.apitestinghub.dto.request.DocumentationGenerationRequest;
import com.apitestinghub.dto.response.ApiExecutionResponse;
import com.apitestinghub.service.ai.DocumentationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST Controller for AI Documentation features.
 */
@RestController
@RequestMapping("/ai-docs")
@Tag(name = "AI Documentation", description = "AI-powered documentation generation using OpenAI GPT-4")
@CrossOrigin(origins = "*")
public class AiDocumentationController {

    private static final Logger logger = LoggerFactory.getLogger(AiDocumentationController.class);
    private static final String SERVICE_HEALTHY_MESSAGE = "AI Documentation service is healthy and ready";

    private final DocumentationService aiDocumentationService;

    public AiDocumentationController(DocumentationService aiDocumentationService) {
        this.aiDocumentationService = aiDocumentationService;
        logger.info("AI Documentation Controller initialized");
    }

    @PostMapping("/describe")
    @Operation(summary = "Generate API Description",
            description = "Generate a concise description of what an API endpoint does")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Description generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request format"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
            @ApiResponse(responseCode = "500", description = "AI service error")
    })
    public Mono<ResponseEntity<String>> generateDescription(@Valid @RequestBody ApiRequest request) {
        logger.debug("Generating description for {} {}", request.method(), request.url());

        return aiDocumentationService.generateApiDescription(request)
                .map(this::buildResponse)
                .doOnSuccess(response -> logger.debug("Description generation completed with status: {}",
                        response.getStatusCode()))
                .onErrorResume(this::handleError);
    }

    @PostMapping("/analyze")
    @Operation(summary = "Analyze API Response",
            description = "Analyze an API response and provide detailed insights")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analysis completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid response format"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
            @ApiResponse(responseCode = "500", description = "AI service error")
    })
    public Mono<ResponseEntity<String>> analyzeResponse(@Valid @RequestBody ApiExecutionResponse response) {
        logger.debug("Analyzing response with status {} ({}ms)",
                response.statusCode(), response.responseTimeMs());

        return aiDocumentationService.analyzeResponse(response)
                .map(this::buildResponse)
                .doOnSuccess(responseEntity -> logger.debug("Analysis completed with status: {}",
                        responseEntity.getStatusCode()))
                .onErrorResume(this::handleError);
    }

    @PostMapping("/documentation")
    @Operation(summary = "Generate Full Documentation",
            description = "Generate comprehensive API documentation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documentation generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request format"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
            @ApiResponse(responseCode = "500", description = "AI service error")
    })
    public Mono<ResponseEntity<String>> generateFullDocumentation(
            @Valid @RequestBody DocumentationGenerationRequest request) {

        logger.debug("Generating full documentation for {} {}",
                request.apiRequest().method(), request.apiRequest().url());

        return aiDocumentationService.generateDocumentation(request.apiRequest(), request.apiResponse())
                .map(this::buildResponse)
                .doOnSuccess(responseEntity -> logger.debug("Documentation generation completed with status: {}",
                        responseEntity.getStatusCode()))
                .onErrorResume(this::handleError);
    }

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check the health status of AI documentation service")
    public Mono<ResponseEntity<String>> healthCheck() {
        logger.debug("AI Documentation health check requested");
        return Mono.just(ResponseEntity.ok(SERVICE_HEALTHY_MESSAGE));
    }

    private ResponseEntity<String> buildResponse(String result) {
        if (isErrorResponse(result)) {
            return ResponseEntity.status(429).body(result);
        }
        return ResponseEntity.ok(result);
    }

    private boolean isErrorResponse(String result) {
        return result.startsWith("Rate limit") ||
                result.startsWith("AI service") ||
                result.startsWith("Error") ||
                result.startsWith("API Error") ||
                result.startsWith("Authentication failed") ||
                result.startsWith("Access forbidden");
    }

    private Mono<ResponseEntity<String>> handleError(Throwable throwable) {
        logger.error("Error in AI documentation operation: {}", throwable.getMessage());
        return Mono.just(ResponseEntity.status(500)
                .body("Internal server error: " + throwable.getMessage()));
    }
}