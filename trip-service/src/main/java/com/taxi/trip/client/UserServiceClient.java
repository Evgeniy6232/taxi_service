package com.taxi.trip.client;

import com.taxi.trip.security.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestClient restClient = RestClient.create();
    private final JwtUtil jwtUtil;
    private final String userServiceUrl;

    private String serviceToken;

    public UserServiceClient(JwtUtil jwtUtil,
                             @Value("${user-service.url}") String userServiceUrl) {
        this.jwtUtil = jwtUtil;
        this.userServiceUrl = userServiceUrl;
    }

    @PostConstruct
    private void initToken() {
        this.serviceToken = jwtUtil.generateServiceToken();
    }

    public boolean passengerExists(Long passengerId) {
        return restClient.get()
                .uri(userServiceUrl + "/passengers/{id}", passengerId)
                .header("Authorization", "Bearer " + serviceToken)
                .exchange((req, res) -> {
                    log.info("passengerExists({}) → status {}", passengerId, res.getStatusCode());
                    return res.getStatusCode().is2xxSuccessful();
                });
    }

    public List<Long> getFreeDrivers() {
        return restClient.get()
                .uri(userServiceUrl + "/drivers/free")
                .header("Authorization", "Bearer " + serviceToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> getDriver(Long driverId) {
        return restClient.get()
                .uri(userServiceUrl + "/drivers/{id}", driverId)
                .header("Authorization", "Bearer " + serviceToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});    // эта тема ведь позволяет сохранить информацию о типе в метаданных класса,
                                                                // которую Spring может прочитать в рантайме. Благодаря анонимному классу {}
    }

    public void updateDriverStatus(Long driverId, String status) {
        restClient.patch()
                .uri(userServiceUrl + "/drivers/{id}/status", driverId)
                .header("Authorization", "Bearer " + serviceToken)
                .header("Content-Type", "application/json")
                .body(Map.of("status", status))
                .retrieve()
                .toBodilessEntity();
    }
}
