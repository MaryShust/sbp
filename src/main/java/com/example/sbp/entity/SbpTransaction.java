package com.example.sbp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sbp_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SbpTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "sender_bill_id", nullable = false)
    private Long senderBillId;

    @Column(name = "sender_bank_bic", nullable = false)
    private String senderBankBic;

    @Column(name = "receiver_bill_id", nullable = false)
    private Long receiverBillId;

    @Column(name = "receiver_bank_bic", nullable = false)
    private String receiverBankBic;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "commission")
    private BigDecimal commission = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "message")
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        transactionId = generateTransactionId();
        createdAt = LocalDateTime.now();
        status = TransactionStatus.PENDING;
    }

    private String generateTransactionId() {
        return "SBP" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    public enum TransactionStatus {
        PENDING, SUCCESS, FAILED, CANCELLED
    }
}