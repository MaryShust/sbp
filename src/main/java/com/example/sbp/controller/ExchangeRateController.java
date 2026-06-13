package com.example.sbp.controller;

import com.example.sbp.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exchange-rate")
@RequiredArgsConstructor
@Tag(name = "ExchangeRate", description = "Курс валют")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping("/rate")
    public ResponseEntity<?> getRate(
            @RequestParam(defaultValue = "USD") String base,
            @RequestParam String target
    ) {
        return ResponseEntity.ok(exchangeRateService.getRate(base, target));
    }
}
