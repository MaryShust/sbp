package com.example.sbp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", unique = true, nullable = false, length = 11)
    @Size(max = 11, message = "Phone number must not exceed 11 characters")
    private String phoneNumber;

    @Column(name = "owner_name", nullable = false, length = 100)
    @Size(max = 100, message = "Owner name must not exceed 100 characters")
    private String ownerName;

    @Column(name = "bank_bic", nullable = false, length = 11)
    @Size(min = 8, max = 11, message = "Bank BIC must be between 8 and 11 characters")
    private String bankBic;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "default_bill_id")
    private Long defaultBillId;

    @ElementCollection
    @CollectionTable(name = "account_bills",
            joinColumns = @JoinColumn(name = "account_id"))
    @Column(name = "bill_id")
    private List<Long> allBillIds = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}