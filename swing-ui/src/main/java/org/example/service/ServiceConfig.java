package org.example.service;


import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class ServiceConfig {
    private static final String BASE_URL = "http://localhost:8080/api";
    private static RestTemplate restTemplate;
    private static Long currentUserId;

    public static RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
            interceptors.add((request, body, execution) -> {
                if (currentUserId != null) {
                    request.getHeaders().add("User-Id", currentUserId.toString());
                }
                return execution.execute(request, body);
            });
            restTemplate.setInterceptors(interceptors);
        }
        return restTemplate;
    }

    public static void setCurrentUserId(Long userId) {
        currentUserId = userId;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }
}



