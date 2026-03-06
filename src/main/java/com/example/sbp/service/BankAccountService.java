package com.example.sbp.service;

import com.example.sbp.dto.BankAccountRequestDTO;
import com.example.sbp.dto.BankAccountResponseDTO;
import com.example.sbp.entity.BankAccount;
import com.example.sbp.entity.Bill;
import com.example.sbp.exception.BankAccountAlreadyExistsException;
import com.example.sbp.exception.BillNotFoundException;
import com.example.sbp.repository.BankAccountRepository;
import com.example.sbp.repository.BillRepository;
import com.example.sbp.exception.BankAccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountService {

    private final BankAccountRepository accountRepository;
    private final BillRepository billRepository;

    @Transactional
    public BankAccountResponseDTO createAccount(BankAccountRequestDTO bankAccountRequestDTO) {
        log.info("Creating new account with phone: {}", bankAccountRequestDTO.getPhoneNumber());

        // Проверка уникальности на телефон
        if (accountRepository.existsByPhoneNumber(bankAccountRequestDTO.getPhoneNumber())) {
            throw new BankAccountAlreadyExistsException("Номер телефона уже существует");
        }

        // Создание аккаунта
        BankAccount account = BankAccount.builder()
                .phoneNumber(bankAccountRequestDTO.getPhoneNumber())
                .ownerName(bankAccountRequestDTO.getOwnerName())
                .bankBic(bankAccountRequestDTO.getBankBic())
                .isActive(true)
                .allBillIds(new ArrayList<>())
                .build();

        account = accountRepository.save(account);
        log.info("Account saved with ID: {}", account.getId());

        // Создание дефолтного счета
        Bill defaultBill = Bill.builder()
                .accountId(account.getId())
                .balance(BigDecimal.ZERO)
                .isActive(false)  // дефолтный счет требуется в дальнейшем активировать
                .build();

        defaultBill = billRepository.save(defaultBill);
        log.info("Default bill created with ID: {} (inactive)", defaultBill.getId());

        // Обновление всего и вся
        account.setDefaultBillId(defaultBill.getId());
        account.getAllBillIds().add(defaultBill.getId());
        account = accountRepository.save(account);

        return mapToResponseDTO(account);
    }

    @Transactional
    public BankAccountResponseDTO getAccountById(Long id) {
        BankAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException("Аккаунт не найден с id: " + id));
        return mapToResponseDTO(account);
    }

    @Transactional
    public BankAccountResponseDTO getAccountByPhone(String phoneNumber) {
        BankAccount account = accountRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BankAccountNotFoundException("Аккаунт не найден с телефоном: " + phoneNumber));
        return mapToResponseDTO(account);
    }

    @Transactional
    public void activateDefaultBill(Long accountId, BigDecimal startBalance) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Аккаунт не найден с id: " + accountId));

        Bill defaultBill = billRepository.findById(account.getDefaultBillId())
                .orElseThrow(() -> new BillNotFoundException("Дефолтный счет не найден"));

        // Check if bill has been funded (balance > 0)
        if (defaultBill.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            defaultBill.setIsActive(true);
            defaultBill.setBalance(startBalance);
            billRepository.save(defaultBill);
            log.info("Default bill {} activated for account {}", defaultBill.getId(), accountId);
        }
    }

    private BankAccountResponseDTO mapToResponseDTO(BankAccount account) {
        BankAccountResponseDTO dto = new BankAccountResponseDTO();
        dto.setId(account.getId());
        dto.setPhoneNumber(account.getPhoneNumber());
        dto.setOwnerName(account.getOwnerName());
        dto.setBankBic(account.getBankBic());
        dto.setIsActive(account.getIsActive());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        dto.setDefaultBillId(account.getDefaultBillId());
        dto.setAllBillIds(account.getAllBillIds());
        return dto;
    }
}