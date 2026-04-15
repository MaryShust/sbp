package com.example.sbp.controller;

import com.example.sbp.dto.LoginRequestDTO;
import com.example.sbp.dto.RegisterRequestDTO;
import com.example.sbp.dto.UpdateRolesRequestDTO;
import com.example.sbp.dto.UserResponseDTO;
import com.example.sbp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for user authentication and management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Аутентификация пользователя", description = "Аутентификация пользователя и возвращает JWT-токен. Предыдущие токены становятся недействительными.")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        String jwt = authService.login(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );
        return ResponseEntity.ok(Map.of("token", jwt));
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя", description = "Создаёт нового пользователя с указанными данными и возвращает JWT-токен.")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        String jwt = authService.register(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getPhoneNumber()
        );
        return ResponseEntity.ok(Map.of("token", jwt));
    }

    @GetMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('USER_MANAGE_ROLES')")
    @Operation(summary = "Получить информацию по пользователю", description = "Возвращает информацию по имени (только у админа)")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable String username) {
        UserResponseDTO user = authService.getUser(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/roles")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('USER_MANAGE_ROLES')")
    @Operation(summary = "Обновляет роли пользователей", description = "Обновляет роли для конкретного пользователя. Все токены пользователя становятся недействительными.")
    public ResponseEntity<UserResponseDTO> updateUserRoles(@Valid @RequestBody UpdateRolesRequestDTO request) {
        UserResponseDTO user = authService.updateUserRoles(request.getUsername(), request.getRoles());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    @Operation(summary = "Разлогин, аннулировать все токены пользователя", description = "Делает все текущие токены пользователя недействительными.")
    public ResponseEntity<Map<String, Object>> logout(@PathVariable String username) {
        int newTokenVersion = authService.logout(username);
        return ResponseEntity.ok(Map.of(
                "username", username,
                "tokenVersion", newTokenVersion,
                "message", "All tokens invalidated"
        ));
    }
}