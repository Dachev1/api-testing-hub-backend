package com.apitestinghub.util;

import com.apitestinghub.dto.request.ApiRequest;
import com.apitestinghub.exception.ApiRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * Validator component for API request.
 * <p>
 * Provides validation methods for API request data.
 */
@Component
public class RequestValidator {

    private static final Set<String> ALLOWED_METHODS = Set.of(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "PATCH",
            "HEAD",
            "OPTIONS");

    private static final Set<String> UNSAFE_PROTOCOLS = Set.of(
            "file",
            "ftp",
            "javascript",
            "data"
    );

    public void validateRequest(ApiRequest request) {

        if (request.method() == null) {
            throw new ApiRequestException("Request cannot be null");
        }

        validateMethod(request.method());
        validateUrl(request.url());
        validateTimeout(request.timeoutMs());
    }

    public boolean isSafeUrl(String urlString) {
        try {
            validateUrl(urlString);
            return true;
        } catch (ApiRequestException e) {
            return false;
        }
    }

    private void validateMethod(String method) {

        if (method == null || method.trim().isEmpty()) {
            throw new ApiRequestException("HTTP is required ");
        }

        if (!ALLOWED_METHODS.contains(method.toUpperCase())) {
            throw new ApiRequestException("Invalid HTTP method: " + method + ". Allowed methods: " + ALLOWED_METHODS);
        }
    }

    private void validateUrl(String urlString) {

        if (urlString == null || urlString.trim().isEmpty()) {
            throw new ApiRequestException("URL is required");
        }

        try {
            URL url = new URL(urlString);

            // Check for unsafe protocols
            String protocol = url.getProtocol().toLowerCase();

            if (UNSAFE_PROTOCOLS.contains(protocol)) {
                throw new ApiRequestException("Unsafe protocol: " + protocol);
            }

            // Ensure is's HTTP or HTTPS
            if (!protocol.equals("http") && !protocol.equals("https")) {
                throw new ApiRequestException(  "Only HTTP and HTTPS protocols are allowed");
            }
        } catch (MalformedURLException e) {
            throw new ApiRequestException("Invalid URL format: " + e.getMessage());
        }
    }

    private void validateTimeout(Integer timeoutMs) {
        if (timeoutMs == null) {
            return;
        }

        if (timeoutMs <= 0) {
            throw new ApiRequestException("Timeout must be a positive integer");
        }

        // 5 minutes max
        if (timeoutMs > 300000) {
            throw new ApiRequestException("Timeout must be less than 5 minutes");
        }
    }
}





























