package com.apitestinghub.service;

import com.apitestinghub.dto.request.ApiRequest;
import com.apitestinghub.dto.response.ApiExecutionResponse;
import com.apitestinghub.exception.ApiRequestException;
import com.apitestinghub.mapper.ApiRequestMapper;
import com.apitestinghub.mapper.ApiResponseMapper;
import com.apitestinghub.util.RequestValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Service for executing API requests and URL validation.
 *
 * Professional Spring Boot service following standard practices.
 */
@Service
public class ApiRequestService {

    private final WebClient webClient;
    private final RequestValidator validator;
    private final ApiRequestMapper requestMapper;
    private final ApiResponseMapper responseMapper;

    public ApiRequestService(@Qualifier("apiRequestWebClient") WebClient webClient,
                             RequestValidator validator,
                             ApiRequestMapper requestMapper,
                             ApiResponseMapper responseMapper) {
        this.webClient = webClient;
        this.validator = validator;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
    }

    /**
     * Execute an API request and return the response.
     */
    public Mono<ApiExecutionResponse> executeRequest(ApiRequest request) {
        validator.validateRequest(request);

        String requestId = UUID.randomUUID().toString();
        String fullUrl = buildUrlWithParams(request);
        long startTime = System.currentTimeMillis();

        return sendHttpRequest(request, fullUrl)
                .map(response -> mapSuccessResponse(response, startTime, requestId))
                .onErrorResume(WebClientResponseException.class,
                        ex -> handleWebClientError(ex, startTime, requestId))
                .onErrorResume(Exception.class, this::handleUnexpectedError);
    }

    /**
     * Validate if a URL is safe and accessible.
     */
    public Mono<Boolean> validateUrl(String url) {
        if (!validator.isSafeUrl(url)) {
            return Mono.just(false);
        }

        return webClient.head()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false);
    }

    // Private helper methods

    private String buildUrlWithParams(ApiRequest request) {
        Map<String, String> params = requestMapper.getQueryParams(request);
        if (params.isEmpty()) return request.url();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(request.url());
        params.forEach(builder::queryParam);
        return builder.toUriString();
    }

    private Mono<ResponseEntity<String>> sendHttpRequest(ApiRequest request, String url) {
        return webClient.method(HttpMethod.valueOf(request.method().toUpperCase()))
                .uri(url)
                .headers(headers -> requestMapper.getHeaders(request).forEach(headers::add))
                .bodyValue(request.body() != null ? request.body() : "")
                .retrieve()
                .toEntity(String.class);
    }

    private ApiExecutionResponse mapSuccessResponse(ResponseEntity<String> response, long startTime, String requestId) {
        long duration = System.currentTimeMillis() - startTime;
        return responseMapper.mapFromResponseEntity(response, duration, requestId);
    }

    private Mono<ApiExecutionResponse> handleWebClientError(WebClientResponseException ex, long startTime, String requestId) {
        long duration = System.currentTimeMillis() - startTime;
        return Mono.just(responseMapper.mapFromException(ex, duration, requestId));
    }

    private Mono<ApiExecutionResponse> handleUnexpectedError(Throwable ex) {
        return Mono.error(new ApiRequestException(
                "Request failed: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        ));
    }
}