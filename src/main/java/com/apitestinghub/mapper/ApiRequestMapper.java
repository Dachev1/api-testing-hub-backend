package com.apitestinghub.mapper;

import com.apitestinghub.dto.request.ApiRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ApiRequestMapper {

    public ApiRequest applyDefaults(ApiRequest request) {

        return new ApiRequest(
                request.method(),
                request.url(),
                request.headers() != null ? request.headers() : Map.of(),
                request.queryParams() != null ? request.queryParams() : Map.of(),
                request.body(),
                request.sessionId(),
                request.timeoutMs() != null ? request.timeoutMs() : 30000,
                request.followRedirects() != null ? request.followRedirects() : true
        );
    }

    public int getTimeoutMs(ApiRequest request) {
        return request.timeoutMs() != null ? request.timeoutMs() : 30000;
    }

    public boolean shouldFollowRedirects(ApiRequest request) {
        return request.followRedirects() != null ? request.followRedirects() : true;
    }

    public Map<String, String> getHeaders(ApiRequest request) {
        return request.headers() != null ? request.headers() : Map.of();
    }

    public Map<String, String> getQueryParams(ApiRequest request) {
        return request.queryParams() != null ? request.queryParams() : Map.of();
    }
}
