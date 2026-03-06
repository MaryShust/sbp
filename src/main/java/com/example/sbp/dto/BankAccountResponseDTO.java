package com.example.sbp.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BankAccountResponseDTO {
    private Long id;
    private String phoneNumber;
    private String ownerName;
    private String bankBic;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long defaultBillId;
    private List<Long> allBillIds;
}