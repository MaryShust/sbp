package com.example.sbp.service;

import com.example.sbp.dto.PaymentRequestDTO;
import com.example.sbp.dto.PaymentResponseDTO;
import com.example.sbp.entity.BankAccount;
import com.example.sbp.entity.Bill;
import com.example.sbp.entity.SbpTransaction;
import com.example.sbp.repository.BankAccountRepository;
import com.example.sbp.repository.BillRepository;
import com.example.sbp.repository.SbpTransactionRepository;
import com.example.sbp.exception.*;
import com.example.sbp.security.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final SecurityService securityService;
    private final BillRepository billRepository;
    private final BankAccountRepository accountRepository;
    private final SbpTransactionRepository transactionRepository;
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005"); // 0.5%
    private static final BigDecimal MIN_COMMISSION = new BigDecimal("10");
    private static final BigDecimal MAX_COMMISSION = new BigDecimal("1000");

    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        log.info("Processing SBP payment: {}", request);
        securityService.checkPrivilegeCreatePayment(request.getSenderBillId());

        if (request.getMessage().trim().length() > 100) {
            throw new MessageFormatException("Message must not exceed 100 characters");
        }


        Bill senderBill = billRepository.findById(request.getSenderBillId())
                .orElseThrow(() -> new BillNotFoundException("Не найден счет отправителя по id: " + request.getSenderBillId()));


        BankAccount senderAccount = accountRepository.findById(senderBill.getAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException("Аккаунт не найден с id: " + senderBill.getAccountId()));



        if (!senderAccount.getIsActive()) {
            throw new BillInactiveException("Аккаунт отправителя не активен");
        }
        if (!senderBill.getIsActive()) {
            throw new BillInactiveException("Счет отправителя не активен");
        }


        Bill receiverBill = findReceiverBill(request.getReceiverIdentifier());

        // Проверять аккаунт не нужно, так как если он заблочен или на него наложен арест, то деньжата уйдут приставам
        if (!receiverBill.getIsActive()) {
            throw new BillInactiveException("Счет получателя не активен");
        }


        BankAccount receiverAccount = accountRepository.findById(receiverBill.getAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException("Аккаунт не найден с id: " + senderBill.getAccountId()));



        BigDecimal commission;
        if (senderAccount.getId().equals(receiverAccount.getId())) {
            commission = new BigDecimal(0);
        } else {
            commission = calculateCommission(request.getAmount());
        }

        // Проверить достаточность средств
        BigDecimal totalAmount = request.getAmount().add(commission);
        if (senderBill.getBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на счете отправителя");
        }

        log.info("TEST MY CODE");
        SbpTransaction transaction = createTransaction(
                senderBill,
                receiverBill,
                senderAccount.getBankBic(),
                receiverAccount.getBankBic(),
                request,
                commission
        );
        log.info("TEST MY CODE 2");


        updateBalances(senderBill, receiverBill, request.getAmount(), commission);

        // Обновить статус транзакции
        transaction.setStatus(SbpTransaction.TransactionStatus.SUCCESS);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        log.info("SBP payment completed successfully: {}", transaction.getTransactionId());

        return convertToResponseDTO(transaction);
    }

    private Bill findReceiverBill(String identifier) {
        // Сначала пробуем найти как ID счета
        Optional<Bill> billById = tryFindBillById(identifier);

        // Если нашли по ID - возвращаем
        return billById.orElseGet(() -> findDefaultBillByPhone(identifier));
    }

    private Optional<Bill> tryFindBillById(String identifier) {
        try {
            Long billId = Long.parseLong(identifier);
            return billRepository.findById(billId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Bill findDefaultBillByPhone(String identifier) {
        BankAccount account = accountRepository.findByPhoneNumber(identifier)
                .orElseThrow(() -> new BankAccountNotFoundException(
                        "Аккаунт не найден по телефону: " + identifier));

        return billRepository.findById(account.getDefaultBillId())
                .orElseThrow(() -> new BillNotFoundException(
                        "Дефолтный счет не найден для аккаунта с телефоном: " + identifier));
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

    private SbpTransaction createTransaction(
            Bill senderBill,
            Bill receiverBill,
            String senderBankBic,
            String receiverBankBic,
            PaymentRequestDTO request,
            BigDecimal commission
    ) {
        log.info("TEST MY CODE 3");
        SbpTransaction transaction = SbpTransaction.builder()
                .senderBillId(senderBill.getId())
                .senderBankBic(senderBankBic)
                .receiverBillId(receiverBill.getId())
                .receiverBankBic(receiverBankBic)
                .amount(request.getAmount())
                .commission(commission)
                .status(SbpTransaction.TransactionStatus.PENDING)
                .message(request.getMessage())
                .build();
        log.info("TEST MY CODE 4");
        return transactionRepository.save(transaction);
    }

    private void updateBalances(
            Bill senderBill,
            Bill receiverBill,
            BigDecimal amount,
            BigDecimal commission
    ) {
        // Списать с отправителя
        senderBill.setBalance(senderBill.getBalance()
                .subtract(amount)
                .subtract(commission));
        billRepository.save(senderBill);

        // Зачислить получателю
        receiverBill.setBalance(receiverBill.getBalance().add(amount));
        billRepository.save(receiverBill);
    }

    private PaymentResponseDTO convertToResponseDTO(SbpTransaction transaction) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setTransactionId(transaction.getTransactionId());
        response.setStatus(transaction.getStatus().toString());
        response.setSenderBillId(transaction.getSenderBillId());
        response.setReceiverBillId(transaction.getReceiverBillId());
        response.setAmount(transaction.getAmount());
        response.setCommission(transaction.getCommission());
        response.setMessage(transaction.getMessage());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setCompletedAt(transaction.getCompletedAt());

        return response;
    }

    public PaymentResponseDTO getTransactionStatus(String transactionId) {
        securityService.checkPrivilegeReadPaymentStatus(transactionId);

        SbpTransaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Транзакция не найдена по id"));

        return convertToResponseDTO(transaction);
    }
}