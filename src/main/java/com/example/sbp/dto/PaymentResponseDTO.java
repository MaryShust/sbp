package com.example.sbp.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Setter
@Getter
public class PaymentResponseDTO {
    private String transactionId;
    private String status;
    private Long senderBillId;
    private Long receiverBillId;
    private BigDecimal amount;
    private BigDecimal commission;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}