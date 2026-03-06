package com.example.sbp.controller;

import com.example.sbp.dto.BillCreateRequestDTO;
import com.example.sbp.dto.BillResponseDTO;
import com.example.sbp.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ResponseEntity<?> createBill(@Valid @RequestBody BillCreateRequestDTO billDTO) {
        BillResponseDTO response = billService.createBill(billDTO);
        Map<String, Object> result = new HashMap<>();
        result.put("id", response.getId());
        result.put("accountId", response.getAccountId());
        result.put("isActive", response.getIsActive());
        result.put("status", "created");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBillById(@PathVariable Long id) {
        BillResponseDTO response = billService.getBillById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}/default")
    public ResponseEntity<?> getDefaultBillByAccountId(@PathVariable Long accountId) {
        BillResponseDTO response = billService.getDefaultBillByAccountId(accountId);
        return ResponseEntity.ok(response);
    }
}