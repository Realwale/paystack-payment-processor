package com.example.paymentprocessor.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "paystack")
@Component
@Data
public class PaystackProperties {

    @Value("${paystack.api.key}")
    private String apiKey;
    private boolean useMockService = false;
}