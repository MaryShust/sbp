package com.example.sbp.controller;

import com.example.sbp.dto.BankAccountRequestDTO;
import com.example.sbp.dto.BankAccountResponseDTO;
import com.example.sbp.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody BankAccountRequestDTO bankAccountRequestDTO) {
        BankAccountResponseDTO response = bankAccountService.createAccount(bankAccountRequestDTO);
        Map<String, Object> result = new HashMap<>();
        result.put("id", response.getId());
        result.put("phoneNumber", response.getPhoneNumber());
        result.put("defaultBillId", response.getDefaultBillId());
        result.put("status", "created");
        result.put("message", "Account created. Default bill is inactive - please fund it to activate");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable Long id) {
        BankAccountResponseDTO response = bankAccountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<?> getAccountByPhone(@PathVariable String phoneNumber) {
        BankAccountResponseDTO response = bankAccountService.getAccountByPhone(phoneNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountId}/activate-default")
    public ResponseEntity<?> activateDefaultBill(
            @PathVariable Long accountId,
            @RequestParam(required = true) BigDecimal startBalance
    ) {
        bankAccountService.activateDefaultBill(accountId, startBalance);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Default bill activated successfully");
        return ResponseEntity.ok(response);
    }
}