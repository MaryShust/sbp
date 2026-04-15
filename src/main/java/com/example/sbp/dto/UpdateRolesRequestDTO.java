package com.example.sbp.dto;

import com.example.sbp.security.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на изменение ролей пользователя")
public class UpdateRolesRequestDTO {

    @NotBlank(message = "Username is required")
    @Schema(description = "Имя пользователя", example = "user123")
    private String username;

    @NotEmpty(message = "At least one role is required")
    @Schema(description = "Набор ролей", example = "[\"USER\", \"MANAGER\"]")
    private Set<Role> roles;
}
