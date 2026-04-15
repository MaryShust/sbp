package com.example.sbp.dto;

import com.example.sbp.security.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о пользователе")
public class UserResponseDTO {

    @Schema(description = "Имя пользователя", example = "user123")
    private String username;

    @Schema(description = "Роли пользователя", example = "[\"USER\", \"MANAGER\"]")
    private Set<Role> roles;
}
