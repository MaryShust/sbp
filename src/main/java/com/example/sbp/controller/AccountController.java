package com.example.sbp.controller;

import com.example.sbp.dto.AccountDTO;
import com.example.sbp.entity.BankAccount;
import com.example.sbp.service.AccountService;
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
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        BankAccount account = accountService.createAccount(accountDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("id", account.getId());
        response.put("accountNumber", account.getAccountNumber());
        response.put("status", "created");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getAccount(@PathVariable String accountNumber) {
        BankAccount account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<?> getBalance(@PathVariable String accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        Map<String, Object> response = new HashMap<>();
        response.put("accountNumber", accountNumber);
        response.put("balance", balance);
        response.put("currency", "RUB");
        return ResponseEntity.ok(response);
    }
}