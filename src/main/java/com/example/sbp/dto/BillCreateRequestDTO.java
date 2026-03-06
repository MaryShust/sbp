package com.example.sbp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class BillCreateRequestDTO {

    @NotNull(message = "Account ID is required")
    private Long accountId;
}