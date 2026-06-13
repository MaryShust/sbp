package com.example.sbp.entity;

import com.example.sbp.kafka.dto.RiskLevel;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "preSuspicion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreSuspicionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "receiver_account_id", nullable = false)
    private Long receiverAccountId;

    @Column(name = "receiver_bank_bic", length = 11)
    private String receiverBankBic;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "risk_level")
    private RiskLevel riskLevel;
}
