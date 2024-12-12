package com.example.paymentprocessor.service;

import com.example.paymentprocessor.constant.TransactionStatus;
import com.example.paymentprocessor.constant.TransactionType;
import com.example.paymentprocessor.data.request.PaymentRequest;
import com.example.paymentprocessor.data.request.TransferRequest;
import com.example.paymentprocessor.data.response.ApiResponse;
import com.example.paymentprocessor.data.response.TransactionDTO;
import com.example.paymentprocessor.exception.InsufficientFundsException;
import com.example.paymentprocessor.exception.PaymentException;
import com.example.paymentprocessor.model.Transaction;
import com.example.paymentprocessor.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    private final WalletService walletService;

    public PaymentService(PayStackServiceImpl paystackService, TransactionRepository transactionRepository, WalletService walletService) {
        this.paystackService = paystackService;
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
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
            log.info("Verifying transaction with reference: {}", reference);
            ApiResponse<Map<String, Object>> paystackResponse = paystackService.verifyTransaction(reference);

            if (paystackResponse.isSuccess()) {
                Map<String, Object> data = paystackResponse.getData();
                String status = (String) data.getOrDefault("status", "failed");
                log.info("Transaction status from Paystack: {}", status);

                Transaction transaction = transactionRepository.findByReference(reference)
                        .orElseGet(() -> {
                            log.info("Creating new transaction record for reference: {}", reference);
                            return Transaction.builder()
                                    .reference(reference)
                                    .type(TransactionType.DEPOSIT)
                                    .build();
                        });

                // Store old status to check if this is a new success
                TransactionStatus oldStatus = transaction.getStatus();

                updateTransactionFromPaystackResponse(transaction, data);
                log.info("Updated transaction status: {}, amount: {}, email: {}",
                        transaction.getStatus(), transaction.getAmount(), transaction.getEmail());

                // Changed condition to check old status instead
                if ("success".equalsIgnoreCase(status) &&
                        (oldStatus == null || oldStatus != TransactionStatus.SUCCESS)) {
                    log.info("Processing successful transaction for wallet credit");

                    String email = transaction.getEmail();
                    if (email != null && !email.isEmpty()) {
                        // Amount is already in Naira, no need to convert again
                        BigDecimal amountInNaira = transaction.getAmount();
                        log.info("Crediting wallet with amount: {} Naira", amountInNaira);

                        try {
                            walletService.creditWallet(email, amountInNaira);
                            log.info("Successfully credited wallet");
                        } catch (Exception e) {
                            log.error("Error crediting wallet: ", e);
                            throw e;
                        }
                    } else {
                        log.warn("No email found in transaction, skipping wallet credit");
                    }
                } else {
                    log.info("Transaction not eligible for wallet credit. Status: {}, Previous Status: {}",
                            status, oldStatus);
                }

                Transaction savedTransaction = transactionRepository.save(transaction);
                log.info("Saved transaction with ID: {}", savedTransaction.getId());
            } else {
                log.warn("Paystack verification was not successful");
            }

            return paystackResponse;

        } catch (PaymentException e) {
            log.error("Payment exception during verification: ", e);
            transactionRepository.findByReference(reference)
                    .ifPresent(transaction -> {
                        transaction.setStatus(TransactionStatus.FAILED);
                        transactionRepository.save(transaction);
                    });
            throw e;
        }
    }

    public ApiResponse<Map<String, Object>> initiateWithdrawal(TransferRequest request) {
        try {
            // Amount comes in Naira from the request
            BigDecimal withdrawalAmount = new BigDecimal(request.getAmount());

            // Check balance and debit wallet (amount in Naira)
            walletService.debitWallet(request.getEmail(), withdrawalAmount);

            // Create transaction record (storing amount in Naira)
            Transaction transaction = Transaction.builder()
                    .reference(request.getReference() != null ?
                            request.getReference() : "WIT_" + UUID.randomUUID().toString())
                    .type(TransactionType.WITHDRAWAL)
                    .status(TransactionStatus.PENDING)
                    .amount(withdrawalAmount)  // Store in Naira
                    .email(request.getEmail())
                    .recipientCode(request.getRecipient())
                    .build();

            transactionRepository.save(transaction);

            try {
                // Update the amount in the existing request to kobo for Paystack
                request.setAmount(withdrawalAmount.multiply(new BigDecimal(100)).intValue());  // Convert to kobo

                ApiResponse<Map<String, Object>> response = paystackService.initiateTransfer(request);

                if (!response.isSuccess()) {
                    // Rollback in Naira
                    walletService.creditWallet(request.getEmail(), withdrawalAmount);
                    transaction.setStatus(TransactionStatus.FAILED);
                    transactionRepository.save(transaction);
                } else {
                    transaction.setStatus(TransactionStatus.SUCCESS);
                    transactionRepository.save(transaction);
                }

                return response;

            } catch (Exception e) {
                // Rollback in Naira
                walletService.creditWallet(request.getEmail(), withdrawalAmount);
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                throw e;
            }

        } catch (InsufficientFundsException e) {
            return ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
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
        // Store amount in Naira in the transaction
        transaction.setAmount(new BigDecimal(amountInKobo.toString()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));

        if (email != null && !email.isEmpty()) {
            transaction.setEmail(email);
        }
    }


    public ApiResponse<Map<String, Object>> checkBalance(String email) {
        BigDecimal balance = walletService.getBalance(email);
        Map<String, Object> data = new HashMap<>();
        data.put("balance", balance);
        data.put("email", email);

        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Balance retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}