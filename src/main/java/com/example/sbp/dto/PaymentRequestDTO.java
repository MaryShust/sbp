package com.example.sbp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {

    @NotBlank(message = "Sender account is required")
    private String senderAccount;

    @NotBlank(message = "Receiver identifier is required")
    private String receiverIdentifier; // phone number or account number

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "1000000", message = "Amount cannot exceed 1,000,000")
    private BigDecimal amount;

    private String message;
}