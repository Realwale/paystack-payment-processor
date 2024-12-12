package com.example.paymentprocessor.service;

import com.example.paymentprocessor.data.request.PaymentRequest;
import com.example.paymentprocessor.data.request.TransferRecipientRequest;
import com.example.paymentprocessor.data.request.TransferRequest;
import com.example.paymentprocessor.data.response.ApiResponse;

import java.util.List;
import java.util.Map;

public interface PayStackService {
    ApiResponse<Map<String, Object>> initializeDeposit(PaymentRequest request);
    ApiResponse<Map<String, Object>> verifyTransaction(String reference);
    ApiResponse<Map<String, Object>> createTransferRecipient(TransferRecipientRequest request);
    ApiResponse<Map<String, Object>> initiateTransfer(TransferRequest request);
    ApiResponse<List<Map<String, Object>>> listBanks();
}