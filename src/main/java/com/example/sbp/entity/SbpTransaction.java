package com.example.sbp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sbp_transactions")
@Data
@Setter
@Getter
public class SbpTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "sender_account_id", nullable = false)
    private BankAccount senderAccount;

    @ManyToOne
    @JoinColumn(name = "receiver_account_id", nullable = false)
    private BankAccount receiverAccount;

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