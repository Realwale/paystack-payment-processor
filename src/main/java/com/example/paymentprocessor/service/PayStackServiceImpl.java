package com.example.paymentprocessor.service;

import com.example.paymentprocessor.config.PaystackProperties;
import com.example.paymentprocessor.data.request.PaymentRequest;
import com.example.paymentprocessor.data.request.TransferRecipientRequest;
import com.example.paymentprocessor.data.request.TransferRequest;
import com.example.paymentprocessor.data.response.ApiResponse;
import com.example.paymentprocessor.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PayStackServiceImpl implements PayStackService {

    private final String apiKey;
    private final RestTemplate restTemplate;
    private final String BASE_URL = "https://api.paystack.co";

    public PayStackServiceImpl(PaystackProperties properties, RestTemplate restTemplate) {
        this.apiKey = properties.getApiKey();
        this.restTemplate = restTemplate;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.debug("Created headers: {}", headers);
        return headers;
    }

    @Override
    public ApiResponse<Map<String, Object>> initializeDeposit(PaymentRequest request) {
        try {
            String url = BASE_URL + "/transaction/initialize";
            HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, createHeaders());
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Payment initialization successful")
                    .data((Map<String, Object>) response.getBody().get("data"))
                    .timestamp(LocalDateTime.now())
                    .build();
            }
            
            throw new PaymentException("Failed to initialize payment");
        } catch (RestClientException e) {
            log.error("Payment initialization failed", e);
            throw new PaymentException("Payment initialization failed: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> verifyTransaction(String reference) {
        try {
            String url = BASE_URL + "/transaction/verify/" + reference;
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                boolean status = (boolean) responseBody.get("status");

                if (status) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    return ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .message("Transaction verification successful")
                            .data(data)
                            .timestamp(LocalDateTime.now())
                            .build();
                } else {
                    String message = (String) responseBody.get("message");
                    throw new PaymentException(message);
                }
            }

            throw new PaymentException("Unable to verify transaction");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentException("Transaction not found or invalid reference");
            }
            log.error("Transaction verification failed", e);
            throw new PaymentException("Transaction verification failed: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Transaction verification failed", e);
            throw new PaymentException("Transaction verification failed: " + e.getMessage());
        }
    }


    @Override
    public ApiResponse<Map<String, Object>> initiateTransfer(TransferRequest request) {
        try {
            String url = BASE_URL + "/transfer";
            HttpEntity<TransferRequest> entity = new HttpEntity<>(request, createHeaders());
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Transfer initiated successfully")
                    .data((Map<String, Object>) response.getBody().get("data"))
                    .timestamp(LocalDateTime.now())
                    .build();
            }
            
            throw new PaymentException("Transfer initiation failed");
        } catch (RestClientException e) {
            log.error("Transfer initiation failed", e);
            throw new PaymentException("Transfer initiation failed: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> createTransferRecipient(TransferRecipientRequest request) {
        try {
            String url = BASE_URL + "/transferrecipient";
            HttpEntity<TransferRecipientRequest> entity = new HttpEntity<>(request, createHeaders());

            log.info("Making request to Paystack: URL={}, Request={}", url, request);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            log.info("Received response from Paystack: Status={}, Body={}",
                    response.getStatusCode(), response.getBody());

            if ((response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED)
                    && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                boolean status = (boolean) responseBody.get("status");

                if (status) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    return ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .message((String) responseBody.get("message"))
                            .data(data)
                            .timestamp(LocalDateTime.now())
                            .build();
                } else {
                    String message = responseBody.get("message") != null ?
                            (String) responseBody.get("message") : "Failed to create transfer recipient";
                    log.error("Paystack error response: {}", responseBody);
                    throw new PaymentException(message);
                }
            }

            log.error("Unexpected response from Paystack: {}", response);
            throw new PaymentException("Failed to create transfer recipient");
        } catch (HttpClientErrorException e) {
            log.error("HTTP error when creating transfer recipient. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException("Failed to create transfer recipient: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error creating transfer recipient", e);
            throw new PaymentException("Failed to create transfer recipient: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<Map<String, Object>>> listBanks() {
        try {
            String url = BASE_URL + "/bank";
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
                return ApiResponse.<List<Map<String, Object>>>builder()
                        .success(true)
                        .message("Banks retrieved successfully")
                        .data(data)
                        .timestamp(LocalDateTime.now())
                        .build();
            }

            throw new PaymentException("Failed to retrieve banks list");
        } catch (RestClientException e) {
            log.error("Failed to retrieve banks list", e);
            throw new PaymentException("Failed to retrieve banks list: " + e.getMessage());
        }
    }
}