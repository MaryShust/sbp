package com.example.sbp.service;

import com.example.sbp.dto.PaymentRequestDTO;
import com.example.sbp.dto.PaymentResponseDTO;
import com.example.sbp.entity.BankAccount;
import com.example.sbp.entity.SbpTransaction;
import com.example.sbp.repository.BankAccountRepository;
import com.example.sbp.repository.SbpTransactionRepository;
import com.example.sbp.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final BankAccountRepository accountRepository;
    private final SbpTransactionRepository transactionRepository;
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005"); // 0.5%
    private static final BigDecimal MIN_COMMISSION = new BigDecimal("10");
    private static final BigDecimal MAX_COMMISSION = new BigDecimal("1000");

    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        log.info("Processing SBP payment: {}", request);

        // Найти счет отправителя
        BankAccount sender = accountRepository.findByAccountNumber(request.getSenderAccount())
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));

        if (!sender.getIsActive()) {
            throw new AccountInactiveException("Sender account is inactive");
        }

        // Найти счет получателя
        BankAccount receiver = findReceiverAccount(request.getReceiverIdentifier());

        // Рассчитать комиссию
        BigDecimal commission;
        if (sender.getId().equals(receiver.getId())) {
            commission = new BigDecimal(0);
        } else {
            commission = calculateCommission(request.getAmount());
        }

        // Проверить достаточность средств
        BigDecimal totalAmount = request.getAmount().add(commission);
        if (sender.getBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // Выполнить перевод
        SbpTransaction transaction = createTransaction(sender, receiver, request, commission);

        // Обновить балансы
        updateBalances(sender, receiver, request.getAmount(), commission);

        // Обновить статус транзакции
        transaction.setStatus(SbpTransaction.TransactionStatus.SUCCESS);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        log.info("SBP payment completed successfully: {}", transaction.getTransactionId());

        return convertToResponseDTO(transaction);
    }

    private BankAccount findReceiverAccount(String identifier) {
        // Пытаемся найти по номеру телефона
        return accountRepository.findByPhoneNumber(identifier)
                .or(() -> accountRepository.findByAccountNumber(identifier))
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));
    }

    private BigDecimal calculateCommission(BigDecimal amount) {
        BigDecimal commission = amount.multiply(COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // Проверка минимальной и максимальной комиссии
        if (commission.compareTo(MIN_COMMISSION) < 0) {
            return MIN_COMMISSION;
        }
        if (commission.compareTo(MAX_COMMISSION) > 0) {
            return MAX_COMMISSION;
        }
        return commission;
    }

    private SbpTransaction createTransaction(BankAccount sender, BankAccount receiver,
                                             PaymentRequestDTO request, BigDecimal commission) {
        SbpTransaction transaction = new SbpTransaction();
        transaction.setSenderAccount(sender);
        transaction.setReceiverAccount(receiver);
        transaction.setAmount(request.getAmount());
        transaction.setCommission(commission);
        transaction.setMessage(request.getMessage());

        return transactionRepository.save(transaction);
    }

    private void updateBalances(BankAccount sender, BankAccount receiver,
                                BigDecimal amount, BigDecimal commission) {
        // Списать с отправителя
        BigDecimal senderNewBalance = sender.getBalance()
                .subtract(amount)
                .subtract(commission);
        sender.setBalance(senderNewBalance);
        accountRepository.save(sender);

        // Зачислить получателю
        BigDecimal receiverNewBalance = receiver.getBalance()
                .add(amount);
        receiver.setBalance(receiverNewBalance);
        accountRepository.save(receiver);
    }

    private PaymentResponseDTO convertToResponseDTO(SbpTransaction transaction) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setTransactionId(transaction.getTransactionId());
        response.setStatus(transaction.getStatus().toString());
        response.setSenderAccount(transaction.getSenderAccount().getAccountNumber());
        response.setReceiverAccount(transaction.getReceiverAccount().getAccountNumber());
        response.setAmount(transaction.getAmount());
        response.setCommission(transaction.getCommission());
        response.setMessage(transaction.getMessage());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setCompletedAt(transaction.getCompletedAt());

        return response;
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO getTransactionStatus(String transactionId) {
        SbpTransaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        return convertToResponseDTO(transaction);
    }
}