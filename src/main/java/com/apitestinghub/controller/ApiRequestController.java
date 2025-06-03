package com.apitestinghub.controller;

import com.apitestinghub.dto.request.ApiRequest;
import com.apitestinghub.dto.response.ApiExecutionResponse;
import com.apitestinghub.service.ApiRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/requests")
public class ApiRequestController {
    private static final Logger logger = LoggerFactory.getLogger(ApiRequestController.class);
    private final ApiRequestService apiRequestService;

    @Autowired
    public ApiRequestController(ApiRequestService apiRequestService) {
        this.apiRequestService = apiRequestService;
    }

    @PostMapping("/execute")
    @Operation(summary = "Execute API Request", description = "Execute an HTTP request through the CORS proxy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request executed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<ApiExecutionResponse>> executeRequest(@Valid @RequestBody ApiRequest request) {
        logger.debug("Executing request: {} {}", request.method(), request.url());

        return apiRequestService
                .executeRequest(request)
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> {
                    logger.error("Error executing request: {}", ex.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate URL", description = "Check if a URL is safe and accessible")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL validation result"),
            @ApiResponse(responseCode = "400", description = "Invalid URL format")
    })
    public Mono<ResponseEntity<Boolean>> validateUrl(@RequestParam String url) {
        logger.debug("Validating URL: {}", url);

        return apiRequestService
                .validateUrl(url)
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> {
                    logger.error("Error validating URL: {}", ex.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check if the API request service is healthy")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("API request service is healthy");
    }
}
