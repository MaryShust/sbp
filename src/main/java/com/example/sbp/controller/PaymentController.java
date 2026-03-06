package com.example.sbp.controller;

import com.example.sbp.dto.PaymentRequestDTO;
import com.example.sbp.dto.PaymentResponseDTO;
import com.example.sbp.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/sbp")
    public ResponseEntity<?> processSbpPayment(@Valid @RequestBody PaymentRequestDTO request) {
        PaymentResponseDTO response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}/status")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String transactionId) {
        PaymentResponseDTO response = paymentService.getTransactionStatus(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "SBP Payment Service");
        return ResponseEntity.ok(response);
    }
}