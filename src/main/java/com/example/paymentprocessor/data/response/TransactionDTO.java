package com.example.paymentprocessor.data.response;

import com.example.paymentprocessor.constant.TransactionStatus;
import com.example.paymentprocessor.constant.TransactionType;
import lombok.Builder;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDTO {
    private String reference;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private String email;
    private LocalDateTime createdAt;
}