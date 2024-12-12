package com.example.paymentprocessor.data.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Builder
public class PaymentRequest {
    @NotNull
    @Email
    private String email;
    
    @NotNull
    @Min(100)
    private BigDecimal amount;
    
    private String reference;
    private String callback_url;
}