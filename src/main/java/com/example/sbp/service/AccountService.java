package com.example.sbp.service;

import com.example.sbp.dto.AccountDTO;
import com.example.sbp.entity.BankAccount;
import com.example.sbp.exception.AccountNotFoundException;
import com.example.sbp.repository.BankAccountRepository;
import com.example.sbp.exception.AccountAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BankAccountRepository accountRepository;

    @Transactional
    public BankAccount createAccount(AccountDTO accountDTO) {
        // Проверка уникальности
        if (accountRepository.existsByAccountNumber(accountDTO.getAccountNumber())) {
            throw new AccountAlreadyExistsException("Account number already exists");
        }

        if (accountRepository.existsByPhoneNumber(accountDTO.getPhoneNumber())) {
            throw new AccountAlreadyExistsException("Phone number already registered");
        }

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountDTO.getAccountNumber());
        account.setPhoneNumber(accountDTO.getPhoneNumber());
        account.setOwnerName(accountDTO.getOwnerName());
        account.setBalance(accountDTO.getInitialBalance() != null ?
                accountDTO.getInitialBalance() : BigDecimal.ZERO);
        account.setBankBic(accountDTO.getBankBic());

        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public BankAccount getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(String accountNumber) {
        BankAccount account = getAccountByNumber(accountNumber);
        return account.getBalance();
    }
}