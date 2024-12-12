package com.example.paymentprocessor.service;

import com.example.paymentprocessor.constant.TransactionStatus;
import com.example.paymentprocessor.constant.TransactionType;
import com.example.paymentprocessor.data.request.PaymentRequest;
import com.example.paymentprocessor.data.request.TransferRequest;
import com.example.paymentprocessor.data.response.ApiResponse;
import com.example.paymentprocessor.data.response.TransactionDTO;
import com.example.paymentprocessor.exception.PaymentException;
import com.example.paymentprocessor.model.Transaction;
import com.example.paymentprocessor.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class PaymentService {
    private final PayStackServiceImpl paystackService;
    private final TransactionRepository transactionRepository;

    public PaymentService(PayStackServiceImpl paystackService, TransactionRepository transactionRepository) {
        this.paystackService = paystackService;
        this.transactionRepository = transactionRepository;
    }

    public ApiResponse<Map<String, Object>> initializeDeposit(PaymentRequest request) {
        if (request.getReference() == null) {
            request.setReference("DEP_" + UUID.randomUUID().toString());
        }

        Transaction transaction = Transaction.builder()
            .reference(request.getReference())
            .type(TransactionType.DEPOSIT)
            .status(TransactionStatus.PENDING)
            .amount(request.getAmount())
            .email(request.getEmail())
            .build();
        
        transactionRepository.save(transaction);

        try {
            ApiResponse<Map<String, Object>> response = paystackService.initializeDeposit(request);
            return response;
        } catch (PaymentException e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw e;
        }
    }

    public ApiResponse<Map<String, Object>> verifyTransaction(String reference) {
        try {
            ApiResponse<Map<String, Object>> paystackResponse = paystackService.verifyTransaction(reference);

            if (paystackResponse.isSuccess()) {
                Map<String, Object> data = paystackResponse.getData();

                Transaction transaction = transactionRepository.findByReference(reference)
                        .orElseGet(() -> {
                            return Transaction.builder()
                                    .reference(reference)
                                    .type(TransactionType.DEPOSIT)
                                    .build();
                        });

                // Update transaction details
                updateTransactionFromPaystackResponse(transaction, data);
                transactionRepository.save(transaction);
            }

            return paystackResponse;

        } catch (PaymentException e) {
            transactionRepository.findByReference(reference)
                    .ifPresent(transaction -> {
                        transaction.setStatus(TransactionStatus.FAILED);
                        transactionRepository.save(transaction);
                    });
            throw e;
        }
    }

    public ApiResponse<Map<String, Object>> initiateWithdrawal(TransferRequest request) {
        if (StringUtils.isEmpty(request.getReference())) {
            request.setReference("WIT_" + UUID.randomUUID().toString());
        }

        Transaction transaction = Transaction.builder()
                .reference(request.getReference())
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.PENDING)
                .amount(new BigDecimal(request.getAmount()))
                .email(request.getEmail())
                .recipientCode(request.getRecipient())
                .reason(request.getReason())
                .build();

        transactionRepository.save(transaction);

        try {
            ApiResponse<Map<String, Object>> response = paystackService.initiateTransfer(request);
            return response;
        } catch (PaymentException e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw e;
        }
    }

    public List<TransactionDTO> getUserTransactions(String email) {
        return transactionRepository.findByEmail(email).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        return TransactionDTO.builder()
            .reference(transaction.getReference())
            .type(transaction.getType())
            .status(transaction.getStatus())
            .amount(transaction.getAmount())
            .email(transaction.getEmail())
            .createdAt(transaction.getCreatedAt())
            .build();
    }

    private void updateTransactionFromPaystackResponse(Transaction transaction, Map<String, Object> paystackData) {
        String status = (String) paystackData.getOrDefault("status", "failed");
        Number amountInKobo = (Number) paystackData.getOrDefault("amount", 0);

        String email = null;
        if (paystackData.get("customer") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> customer = (Map<String, Object>) paystackData.get("customer");
            email = (String) customer.getOrDefault("email", "");
        }

        transaction.setStatus("success".equalsIgnoreCase(status) ?
                TransactionStatus.SUCCESS : TransactionStatus.FAILED);
        transaction.setAmount(new BigDecimal(amountInKobo.toString()));

        if (email != null && !email.isEmpty()) {
            transaction.setEmail(email);
        }
    }
}