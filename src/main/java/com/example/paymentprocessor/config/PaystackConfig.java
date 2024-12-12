package com.example.paymentprocessor.config;


import com.example.paymentprocessor.service.MockPaystackService;
import com.example.paymentprocessor.service.PayStackServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PaystackConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @Primary
    public PayStackServiceImpl paystackService(
            PaystackProperties properties,
            RestTemplate restTemplate) {
        return properties.isUseMockService() ?
                new MockPaystackService(properties, restTemplate) :
                new PayStackServiceImpl(properties, restTemplate);
    }
}