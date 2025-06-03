package com.apitestinghub.mapper;

import com.apitestinghub.dto.response.ApiExecutionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Map;


@Component
public class ApiResponseMapper {

    public ApiExecutionResponse mapFromResponseEntity(ResponseEntity<String> responseEntity, long responseTimeMs, String requestId) {
        return new ApiExecutionResponse(
                responseEntity.getStatusCode().value(),
                responseEntity.getStatusCode().toString(),
                responseEntity.getHeaders().toSingleValueMap(),
                responseEntity.getBody(),
                responseTimeMs,
                LocalDateTime.now(),
                requestId,
                isSuccessStatusCode(responseEntity.getStatusCode().value())
        );
    }

    public ApiExecutionResponse mapFromException(WebClientResponseException exception,
                                                 long responseTimeMs,
                                                 String requestId) {
        return new ApiExecutionResponse(
                exception.getStatusCode().value(),
                exception.getStatusText(),
                exception.getHeaders().toSingleValueMap(),
                exception.getResponseBodyAsString(),
                responseTimeMs,
                LocalDateTime.now(),
                requestId,
                false
        );
    }

    public ApiExecutionResponse mapFromGeneralException(Exception exception,
                                                        long responseTimeMs,
                                                        String requestId) {
        return new ApiExecutionResponse(
                500,
                "Internal Server Error",
                Map.of(),
                "Request failed: " + exception.getMessage(),
                responseTimeMs,
                LocalDateTime.now(),
                requestId,
                false
        );
    }

    public boolean isSuccessStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    public ApiExecutionResponse applyDefaults(ApiExecutionResponse response) {
        return new ApiExecutionResponse(
                response.statusCode(),
                response.statusText(),
                response.headers() != null ? response.headers() : Map.of(),
                response.body(),
                response.responseTimeMs(),
                response.timestamp() != null ? response.timestamp() : LocalDateTime.now(),
                response.requestId(),
                response.success()
        );
    }
}
