package com.example.sbp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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

    @Column(name = "sender_bank_bic", nullable = false, length = 11)
    @Size(min = 8, max = 11, message = "Bank BIC must be between 8 and 11 characters")
    private String senderBankBic;

    @Column(name = "receiver_bill_id", nullable = false)
    private Long receiverBillId;

    @Column(name = "receiver_bank_bic", nullable = false, length = 11)
    @Size(min = 8, max = 11, message = "Bank BIC must be between 8 and 11 characters")
    private String receiverBankBic;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "commission")
    private BigDecimal commission = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "message", length = 100)  // Добавлено ограничение длины
    @Size(max = 100, message = "Message must not exceed 100 characters")  // Валидация
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

        // Обрезаем сообщение если оно превышает 100 символов
        if (message != null && message.length() > 100) {
            message = message.substring(0, 100);
        }
    }

    private String generateTransactionId() {
        return "SBP" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    public enum TransactionStatus {
        PENDING, SUCCESS, FAILED, CANCELLED
    }
}