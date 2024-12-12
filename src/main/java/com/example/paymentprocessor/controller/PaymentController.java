package com.example.paymentprocessor.controller;


import com.example.paymentprocessor.data.request.PaymentRequest;
import com.example.paymentprocessor.data.request.TransferRecipientRequest;
import com.example.paymentprocessor.data.request.TransferRequest;
import com.example.paymentprocessor.data.response.ApiResponse;
import com.example.paymentprocessor.data.response.TransactionDTO;
import com.example.paymentprocessor.service.PaymentService;
import com.example.paymentprocessor.service.PayStackServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@Validated
@Tag(name = "Payment Operations", description = "APIs for handling payments, deposits and withdrawals")
public class PaymentController {
    private final PaymentService paymentService;
    private final PayStackServiceImpl paystackService;

    public PaymentController(PaymentService paymentService, PayStackServiceImpl paystackService) {
        this.paymentService = paymentService;
        this.paystackService = paystackService;
    }

    @PostMapping("/deposit/initialize")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initializeDeposit(
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.initializeDeposit(request));
    }

    @GetMapping("/verify/{reference}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyTransaction(
            @PathVariable String reference) {
        return ResponseEntity.ok(paymentService.verifyTransaction(reference));
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initiateWithdrawal(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(paymentService.initiateWithdrawal(request));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getUserTransactions(
            @PathVariable @Email String email) {
        List<TransactionDTO> transactions = paymentService.getUserTransactions(email);
        return ResponseEntity.ok(ApiResponse.<List<TransactionDTO>>builder()
            .success(true)
            .message("Transactions retrieved successfully")
            .data(transactions)
            .timestamp(LocalDateTime.now())
            .build());
    }

    @PostMapping("/recipients")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTransferRecipient(
            @Valid @RequestBody TransferRecipientRequest request) {
        return ResponseEntity.ok(paystackService.createTransferRecipient(request));
    }

    @GetMapping("/banks")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listBanks() {
        return ResponseEntity.ok(paystackService.listBanks());
    }
}