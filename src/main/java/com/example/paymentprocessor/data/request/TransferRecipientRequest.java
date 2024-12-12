package com.example.paymentprocessor.data.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transfer recipient creation request")
public class TransferRecipientRequest {

    @NotNull(message = "Type is required")
    @Schema(description = "Type of recipient (nuban for Nigerian bank accounts)", example = "nuban")
    private String type;

    @NotNull(message = "Name is required")
    @Schema(description = "Account holder's name", example = "John Doe")
    private String name;

    @NotNull(message = "Account number is required")
    @Pattern(regexp = "\\d{10}", message = "Account number must be 10 digits")
    @Schema(description = "Bank account number", example = "0001234567")
    private String account_number;

    @NotNull(message = "Bank code is required")
    @Schema(description = "Bank code", example = "058")
    private String bank_code;

    @Schema(description = "Currency of the account", example = "NGN", defaultValue = "NGN")
    private String currency = "NGN";

    @Schema(description = "Description for the recipient", example = "Employee salary account")
    private String description;
}