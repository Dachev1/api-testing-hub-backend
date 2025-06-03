package com.apitestinghub.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/**
 * Service responsible for HTTP communication with GitHub Models AI API.
 */
@Service
public class ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
    private static final String AI_ERROR_MESSAGE = "AI service temporarily unavailable. Please try again later.";
    private static final String RATE_LIMIT_MESSAGE = "Rate limit exceeded. Please wait before making more requests.";

    private final WebClient githubModelsWebClient;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.github-models.model}")
    private String model;

    @Value("${app.ai.github-models.max-tokens}")
    private int maxTokens;

    @Value("${app.ai.github-models.temperature}")
    private double temperature;

    public ClientService(@Qualifier("githubModelsWebClient") WebClient githubModelsWebClient,
                           ObjectMapper objectMapper) {
        this.githubModelsWebClient = githubModelsWebClient;
        this.objectMapper = objectMapper;
        logger.info("AI Client Service initialized - Model: {}", model);
    }

    public Mono<String> executeAiRequest(String prompt, String operationType) {
        Map<String, Object> requestBody = buildApiRequest(prompt);
        long startTime = System.currentTimeMillis();

        return githubModelsWebClient
                .post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(buildRetrySpec(operationType))
                .map(this::extractContentFromResponse)
                .doOnSuccess(result -> logOperationComplete(operationType, startTime))
                .onErrorResume(WebClientResponseException.class, ex -> handleHttpError(ex, operationType))
                .onErrorReturn(AI_ERROR_MESSAGE);
    }

    private Map<String, Object> buildApiRequest(String prompt) {
        return Map.of(
                "model", model,
                "messages", new Object[]{
                        Map.of("role", "system", "content", buildSystemPrompt()),
                        Map.of("role", "user", "content", prompt)
                },
                "max_tokens", maxTokens,
                "temperature", temperature,
                "stream", false,
                "top_p", 1.0,
                "frequency_penalty", 0.0,
                "presence_penalty", 0.0
        );
    }

    private String buildSystemPrompt() {
        return "You are a professional API documentation expert and analyst. " +
                "Provide clear, comprehensive, and actionable insights. " +
                "Focus on practical information that developers can immediately use. " +
                "Be concise but thorough, and structure your responses clearly.";
    }

    private Retry buildRetrySpec(String operationType) {
        return Retry.backoff(3, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(10))
                .filter(this::isRetryableError)
                .doBeforeRetry(retrySignal ->
                        logger.warn("Retrying {} (attempt {}): {}",
                                operationType, retrySignal.totalRetries() + 1,
                                retrySignal.failure().getMessage()));
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            return statusCode >= 500 || statusCode == 429;
        }
        return false;
    }

    private Mono<String> handleHttpError(WebClientResponseException ex, String operationType) {
        int statusCode = ex.getStatusCode().value();
        String message = switch (statusCode) {
            case 429 -> {
                logger.warn("Rate limit exceeded for {}", operationType);
                yield RATE_LIMIT_MESSAGE;
            }
            case 401 -> {
                logger.error("Authentication failed for {}: Invalid GitHub token", operationType);
                yield "Authentication failed. Please check your GitHub Models API token.";
            }
            case 403 -> {
                logger.error("Access forbidden for {}: Check token permissions", operationType);
                yield "Access forbidden. Please check your GitHub Models token permissions.";
            }
            case 404 -> {
                logger.error("API endpoint not found for {}", operationType);
                yield "GitHub Models API endpoint not found. Please check configuration.";
            }
            default -> {
                logger.error("HTTP error {} for {}: {}", statusCode, operationType, ex.getMessage());
                yield String.format("API error (%d): %s", statusCode, ex.getMessage());
            }
        };
        return Mono.just(message);
    }

    private String extractContentFromResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);

            if (rootNode.has("error")) {
                String errorMessage = rootNode.get("error").get("message").asText();
                logger.error("GitHub Models API error: {}", errorMessage);
                return "API Error: " + errorMessage;
            }

            JsonNode choices = rootNode.path("choices");
            if (choices.isEmpty()) {
                logger.error("No choices in API response");
                return "No response generated by AI model";
            }

            String content = choices.get(0).path("message").path("content").asText().trim();

            if (content.isEmpty()) {
                logger.warn("Empty content received from AI model");
                return "Empty response from AI model";
            }

            logTokenUsage(rootNode);
            return content;

        } catch (Exception e) {
            logger.error("Error parsing AI response: {}", e.getMessage(), e);
            return "Error parsing AI response: " + e.getMessage();
        }
    }

    private void logTokenUsage(JsonNode rootNode) {
        if (rootNode.has("usage")) {
            JsonNode usage = rootNode.get("usage");
            logger.debug("Token usage - Prompt: {}, Completion: {}, Total: {}",
                    usage.path("prompt_tokens").asInt(),
                    usage.path("completion_tokens").asInt(),
                    usage.path("total_tokens").asInt());
        }
    }

    private void logOperationComplete(String operationType, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("{} completed in {}ms", operationType, duration);
    }
}