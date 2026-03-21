package com.example.sbp.service;

import com.example.sbp.dto.BillCreateRequestDTO;
import com.example.sbp.dto.BillResponseDTO;
import com.example.sbp.entity.BankAccount;
import com.example.sbp.entity.Bill;
import com.example.sbp.exception.BillInactiveException;
import com.example.sbp.exception.BillNotBelongAccountExeption;
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
    public BillResponseDTO replenishBill(Long accountId, Long billId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть положительной");
        }

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException("Счет не найден по id: " + billId));

        BankAccount account = accountRepository.findById(bill.getAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException(
                        "Аккаунт не найден для счета id: " + billId + ". Счет не принадлежит пользователю"));

        if (!account.getId().equals(accountId)) {
            throw new BillNotBelongAccountExeption("Счет не принадлежит аккаунту");
        }

        if (!account.getIsActive()) {
            throw new BillInactiveException("Аккаунт отправителя не активен");
        }

        if (!bill.getIsActive()) {
            bill.setIsActive(true);
            log.info("Bill {} activated", bill.getId());
        }

        // Пополнение счета
        BigDecimal newBalance = bill.getBalance().add(amount);
        bill.setBalance(newBalance);

        billRepository.save(bill);
        log.info("Bill {} replenished by {}. New balance: {}", bill.getId(), amount, newBalance);

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