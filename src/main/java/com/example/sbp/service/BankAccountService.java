package com.example.sbp.service;

import com.example.sbp.dto.BankAccountRequestDTO;
import com.example.sbp.dto.BankAccountResponseDTO;
import com.example.sbp.entity.BankAccount;
import com.example.sbp.entity.Bill;
import com.example.sbp.exception.*;
import com.example.sbp.repository.BankAccountRepository;
import com.example.sbp.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountService {

    private final BankAccountRepository accountRepository;
    private final BillRepository billRepository;

    public BankAccountResponseDTO createAccount(BankAccountRequestDTO bankAccountRequestDTO) {
        log.info("Creating new account with phone: {}", bankAccountRequestDTO.getPhoneNumber());

        validateOwnerName(bankAccountRequestDTO.getOwnerName());
        validateBankBic(bankAccountRequestDTO.getBankBic());
        validatePhoneNumber(bankAccountRequestDTO.getPhoneNumber());

        // Проверка уникальности на телефон
        if (accountRepository.existsByPhoneNumber(bankAccountRequestDTO.getPhoneNumber())) {
            throw new BankAccountAlreadyExistsException("Номер телефона уже существует");
        }


        BankAccount account = BankAccount.builder()
                .phoneNumber(bankAccountRequestDTO.getPhoneNumber())
                .ownerName(bankAccountRequestDTO.getOwnerName())
                .bankBic(bankAccountRequestDTO.getBankBic())
                .isActive(true)
                .allBillIds(new ArrayList<>())
                .build();

        account = accountRepository.save(account);
        log.info("Account saved with ID: {}", account.getId());


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

    public BankAccountResponseDTO getAccountById(Long id) {
        BankAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException("Аккаунт не найден с id: " + id));
        return mapToResponseDTO(account);
    }

    public BankAccountResponseDTO getAccountByPhone(String phoneNumber) {
        BankAccount account = accountRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BankAccountNotFoundException("Аккаунт не найден с телефоном: " + phoneNumber));
        return mapToResponseDTO(account);
    }

    public void activateDefaultBill(Long accountId, BigDecimal startBalance) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Аккаунт не найден с id: " + accountId));

        Bill defaultBill = billRepository.findById(account.getDefaultBillId())
                .orElseThrow(() -> new BillNotFoundException("Дефолтный счет не найден"));

        // Баланс должен быть положительным
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

    public void validateOwnerName(String ownerName) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new OwnerNameFormatException("Owner name cannot be empty");
        }

        String trimmed = ownerName.trim();

        if (trimmed.length() > 100) {
            throw new OwnerNameFormatException(
                    String.format("Owner name must not exceed 100 characters, current length: %d",
                            trimmed.length())
            );
        }
    }

    public void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new PhoneNumberFormatException("Phone number cannot be empty");
        }

        String trimmed = phoneNumber.trim();

        if (trimmed.length() > 11) {
            throw new PhoneNumberFormatException(
                    String.format("Phone number must not exceed 11 characters, current length: %d",
                            trimmed.length())
            );
        }

        // Проверка формата (международный формат)
        if (!trimmed.matches("^7[0-9]{10}$")) {
            throw new PhoneNumberFormatException(
                    "Invalid phone number format. Supported formats: +79991234567"
            );
        }
    }

    public void validateBankBic(String bankBic) {
        if (bankBic == null || bankBic.isBlank()) {
            throw new BankBicFormatException("Bank BIC cannot be empty");
        }

        String trimmed = bankBic.trim();

        // Проверка длины (BIC должен быть 8 или 11 символов)
        int length = trimmed.length();
        if (length < 8 || length > 11) {
            throw new BankBicFormatException(
                    String.format("Bank BIC must be 8 or 11 characters, current length: %d", length)
            );
        }

        // Проверка на запрещенные символы в BIC
        if (trimmed.contains(" ")) {
            throw new BankBicFormatException("Bank BIC cannot contain spaces");
        }
    }
}