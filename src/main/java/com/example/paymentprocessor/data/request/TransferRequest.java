package com.example.paymentprocessor.data.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferRequest {
    @NotNull
    @Min(100)
    private Integer amount;
    
    @NotNull
    private String recipient;
    
    @NotNull
    private String reason;
    
    private String reference;

    @NotNull
    @Email
    private String email;
}