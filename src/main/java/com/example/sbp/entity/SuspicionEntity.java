package com.example.sbp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "suspicion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspicionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "bank_bic", nullable = false, length = 11)
    private String bankBic;

    @Column(name = "duplicate_count", nullable = false)
    private Integer duplicateCount;

    @Column(name = "analysis_date", nullable = false)
    private LocalDateTime analysisDate;
}
