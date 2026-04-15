package com.example.sbp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Schema(description = "Запрос на регистрацию")
public class RegisterRequestDTO {

    @NotBlank(message = "Username is required")
    @Schema(
            description = "Имя пользователя",
            example = "user",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(
            description = "Пароль",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Номер телефона в формате 7XXXXXXXXXX",
            example = "79123456789",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @Pattern(regexp = "^7[0-9]{10}$", message = "Phone must be in format 7XXXXXXXXXX")
    private String phoneNumber;
}
