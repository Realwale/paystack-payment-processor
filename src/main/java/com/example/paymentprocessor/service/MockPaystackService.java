package com.example.paymentprocessor.service;
import com.example.paymentprocessor.config.PaystackProperties;
import com.example.paymentprocessor.data.request.PaymentRequest;
import com.example.paymentprocessor.data.request.TransferRecipientRequest;
import com.example.paymentprocessor.data.request.TransferRequest;
import com.example.paymentprocessor.data.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;


@Service
@Slf4j
public class MockPaystackService extends PayStackServiceImpl {

    public MockPaystackService(PaystackProperties properties, RestTemplate restTemplate) {
        super(properties, restTemplate);
    }
    
    @Override
    public ApiResponse<Map<String, Object>> initializeDeposit(PaymentRequest request) {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("authorization_url", "https://checkout.paystack.com/mock_url");
        mockData.put("access_code", "mock_access_code");
        mockData.put("reference", request.getReference() != null ? 
            request.getReference() : "MOCK_" + UUID.randomUUID().toString());

        return ApiResponse.<Map<String, Object>>builder()
            .success(true)
            .message("Payment initialization successful")
            .data(mockData)
            .timestamp(LocalDateTime.now())
            .build();
    }

    @Override
    public ApiResponse<Map<String, Object>> verifyTransaction(String reference) {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("status", "success");
        mockData.put("reference", reference);
        mockData.put("amount", 50000);
        mockData.put("paid_at", LocalDateTime.now().toString());
        
        Map<String, Object> customer = new HashMap<>();
        customer.put("email", "customer@example.com");
        mockData.put("customer", customer);

        return ApiResponse.<Map<String, Object>>builder()
            .success(true)
            .message("Transaction verification successful")
            .data(mockData)
            .timestamp(LocalDateTime.now())
            .build();
    }

    @Override
    public ApiResponse<Map<String, Object>> createTransferRecipient(TransferRecipientRequest request) {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("type", request.getType());
        mockData.put("name", request.getName());
        mockData.put("account_number", request.getAccount_number());
        mockData.put("bank_code", request.getBank_code());
        mockData.put("currency", request.getCurrency());
        mockData.put("recipient_code", "MOCK_RCP_" + UUID.randomUUID().toString().substring(0, 8));

        return ApiResponse.<Map<String, Object>>builder()
            .success(true)
            .message("Transfer recipient created successfully")
            .data(mockData)
            .timestamp(LocalDateTime.now())
            .build();
    }

    @Override
    public ApiResponse<Map<String, Object>> initiateTransfer(TransferRequest request) {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("reference", request.getReference() != null ? 
            request.getReference() : "MOCK_TRF_" + UUID.randomUUID().toString());
        mockData.put("amount", request.getAmount());
        mockData.put("status", "success");
        mockData.put("transfer_code", "MOCK_TRF_" + UUID.randomUUID().toString().substring(0, 8));
        mockData.put("recipient", request.getRecipient());
        mockData.put("reason", request.getReason());

        return ApiResponse.<Map<String, Object>>builder()
            .success(true)
            .message("Transfer initiated successfully")
            .data(mockData)
            .timestamp(LocalDateTime.now())
            .build();
    }

    @Override
    public ApiResponse<List<Map<String, Object>>> listBanks() {
        List<Map<String, Object>> mockBanks = new ArrayList<>();
        
        Map<String, Object> gtBank = new HashMap<>();
        gtBank.put("name", "Guaranty Trust Bank");
        gtBank.put("code", "058");
        gtBank.put("active", true);
        
        Map<String, Object> firstBank = new HashMap<>();
        firstBank.put("name", "First Bank of Nigeria");
        firstBank.put("code", "011");
        firstBank.put("active", true);
        
        mockBanks.add(gtBank);
        mockBanks.add(firstBank);

        return ApiResponse.<List<Map<String, Object>>>builder()
            .success(true)
            .message("Banks retrieved successfully")
            .data(mockBanks)
            .timestamp(LocalDateTime.now())
            .build();
    }
}