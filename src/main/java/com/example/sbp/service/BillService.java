package com.example.sbp.service;

import com.example.sbp.dto.BillCreateRequestDTO;
import com.example.sbp.dto.BillResponseDTO;
import com.example.sbp.entity.BankAccount;
import com.example.sbp.entity.Bill;
import com.example.sbp.repository.BankAccountRepository;
import com.example.sbp.repository.BillRepository;
import com.example.sbp.exception.BankAccountNotFoundException;
import com.example.sbp.exception.BillNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillService {

    private final BillRepository billRepository;
    private final BankAccountRepository accountRepository;

    @Transactional
    public BillResponseDTO createBill(BillCreateRequestDTO billDTO) {
        log.info("Creating new bill for account ID: {}", billDTO.getAccountId());

        // Проверка на наличие аккаунта
        BankAccount account = accountRepository.findById(billDTO.getAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException("Аккаунт не найден по id: " + billDTO.getAccountId()));

        Bill bill = Bill.builder()
                .accountId(billDTO.getAccountId())
                .balance(BigDecimal.ZERO)
                .isActive(true) // активный так как явно уже не первый
                .build();

        bill = billRepository.save(bill);

        // Обновление аккаунта
        account.getAllBillIds().add(bill.getId());
        accountRepository.save(account);

        log.info("Bill created with ID: {} (active)", bill.getId());

        return mapToResponseDTO(bill);
    }

    @Transactional
    public BillResponseDTO getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new BillNotFoundException("Счет не найден по id: " + id));
        return mapToResponseDTO(bill);
    }

    @Transactional
    public BillResponseDTO getDefaultBillByAccountId(Long accountId) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Аккаунт не найден по id: " + accountId));

        Bill bill = billRepository.findById(account.getDefaultBillId())
                .orElseThrow(() -> new BillNotFoundException("Счет не найден по id: " + account.getDefaultBillId()));

        return mapToResponseDTO(bill);
    }

    private BillResponseDTO mapToResponseDTO(Bill bill) {
        BillResponseDTO dto = new BillResponseDTO();
        dto.setId(bill.getId());
        dto.setAccountId(bill.getAccountId());
        dto.setBalance(bill.getBalance());
        dto.setIsActive(bill.getIsActive());
        dto.setCreatedAt(bill.getCreatedAt());
        dto.setUpdatedAt(bill.getUpdatedAt());
        return dto;
    }
}