package com.example.sbp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@Schema(description = "Запрос на создание аккаунта")
public class BankAccountRequestDTO {

    @Schema(description = "Номер телефона в формате 7XXXXXXXXXX",
            example = "79123456789",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^7[0-9]{10}$", message = "Phone must be in format 7XXXXXXXXXX")
    private String phoneNumber;

    @Schema(description = "Имя владельца",
            example = "Иван Петров",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @Schema(description = "БИК банка",
            example = "044525555",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Bank BIC is required")
    @Pattern(regexp = "^[0-9]{9}$", message = "BIC must be 9 digits")
    private String bankBic;
}