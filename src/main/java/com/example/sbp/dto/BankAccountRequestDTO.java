package com.example.sbp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class BankAccountRequestDTO {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^7[0-9]{10}$", message = "Phone must be in format 7XXXXXXXXXX")
    private String phoneNumber;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotBlank(message = "Bank BIC is required")
    @Pattern(regexp = "^[0-9]{9}$", message = "BIC must be 9 digits")
    private String bankBic;
}