package com.apitestinghub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${app.request.timeout:30000}")
    private int requestTimeout;

    @Value("${app.request.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${app.ai.github-models.endpoint}")
    private String githubModelsEndpoint;

    @Value("${app.ai.github-models.api-key}")
    private String githubModelsApiKey;

    /**
     * General purpose WebClient for executing user API requests
     */
    @Bean("apiRequestWebClient")
    public WebClient apiRequestWebClient() {
        // Optimize connection pool settings
        ConnectionProvider provider = ConnectionProvider.builder("api-requests")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .evictInBackground(Duration.ofSeconds(30))
                .build();

        // Configure HTTP client with timeouts
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .responseTimeout(Duration.ofMillis(requestTimeout))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(requestTimeout, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(requestTimeout, TimeUnit.MILLISECONDS))
                )
                .followRedirect(true)
                .compress(true);

        // Set maximum memory size for responses
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .filter(logRequest())
                .build();
    }

    /**
     * Dedicated WebClient for GitHub Models AI service integration
     */
    @Bean("githubModelsWebClient")
    public WebClient githubModelsWebClient() {
        // Optimize connection pool settings for AI service
        ConnectionProvider provider = ConnectionProvider.builder("github-models-ai")
                .maxConnections(10)
                .maxIdleTime(Duration.ofSeconds(60))
                .maxLifeTime(Duration.ofMinutes(10))
                .pendingAcquireTimeout(Duration.ofSeconds(15))
                .evictInBackground(Duration.ofSeconds(60))
                .build();

        // Configure HTTP client with timeouts
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .responseTimeout(Duration.ofSeconds(120))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(120, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(120, TimeUnit.SECONDS))
                )
                .followRedirect(true)
                .compress(true);

        // Set memory limits for responses
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(githubModelsEndpoint)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer " + githubModelsApiKey)
                .defaultHeader("User-Agent", "API-Testing-Hub/1.0")
                .filter(logRequest())
                .build();
    }

    /**
     * Log request filter for debugging
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (clientRequest.url().toString().contains("localhost") ||
                    clientRequest.url().toString().contains("127.0.0.1")) {
                System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            }
            return Mono.just(clientRequest);
        });
    }
}
