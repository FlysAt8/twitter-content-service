package com.twitter.contentservice.config;

import com.twitter.contentservice.client.BearerTokenForwardingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class UserServiceClientConfig {

    @Bean
    public RestClient userServiceRestClient(@Value("${user-service.url}") String baseUrl,
                                            @Value("${user-service.connect-timeout-ms}") long connectTimeoutMs,
                                            @Value("${user-service.read-timeout-ms}") long readTimeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(new BearerTokenForwardingInterceptor())
                .build();
    }
}
