package com.example.sbp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
public class AccountDTO {

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^[0-9]{20}$", message = "Account number must be 20 digits")
    private String accountNumber;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^7[0-9]{10}$", message = "Phone must be in format 7XXXXXXXXXX")
    private String phoneNumber;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    private BigDecimal initialBalance;

    @NotBlank(message = "Bank BIC is required")
    @Pattern(regexp = "^[0-9]{9}$", message = "BIC must be 9 digits")
    private String bankBic;
}