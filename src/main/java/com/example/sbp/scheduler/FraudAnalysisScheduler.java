package com.example.sbp.scheduler;

import com.example.sbp.entity.PreSuspicionEntity;
import com.example.sbp.entity.SuspicionEntity;
import com.example.sbp.kafka.dto.RiskLevel;
import com.example.sbp.repository.BankAccountRepository;
import com.example.sbp.repository.PreSuspicionRepository;
import com.example.sbp.repository.SuspicionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudAnalysisScheduler {

    private final PreSuspicionRepository preSuspicionRepository;
    private final SuspicionRepository suspicionRepository;
    private final BankAccountRepository accountRepository;

    @Scheduled(fixedRate = 120000)
    public void analyzeSuspiciousTransactions() {
        log.info("Начало анализа мошенничества (каждые 2 минуты)");


        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusMinutes(4);

        List<PreSuspicionEntity> events = preSuspicionRepository.findByEventTimeBetween(startTime, now);

        List<PreSuspicionEntity> criticalHighEvents = events.stream()
                .filter(e -> e.getRiskLevel() == RiskLevel.CRITICAL
                        || e.getRiskLevel() == RiskLevel.HIGH)
                .toList();

        Map<String, List<PreSuspicionEntity>> groupedByBankAndAccount = criticalHighEvents.stream()
                .collect(Collectors.groupingBy(e -> e.getReceiverBankBic() + ":" + e.getReceiverAccountId()));

        int duplicatesFound = 0;

        for (Map.Entry<String, List<PreSuspicionEntity>> entry : groupedByBankAndAccount.entrySet()) {
            List<PreSuspicionEntity> group = entry.getValue();

            if (group.size() >= 2) {
                saveSuspicionRecord(group, now);
                duplicatesFound++;
            }
        }

        log.info("Анализ мошенничества завершен: обнаружено {} дропперов", duplicatesFound);
    }

    private void saveSuspicionRecord(List<PreSuspicionEntity> group, LocalDateTime analysisDate) {
        PreSuspicionEntity first = group.get(0);
        Long accountId = first.getReceiverAccountId();
        String bankBic = first.getReceiverBankBic();
        int duplicateCount = group.size();
        LocalDateTime since = analysisDate.minusMinutes(4);

        accountRepository.findById(accountId).ifPresent(account -> {
            String userName = account.getOwnerName();

            Optional<SuspicionEntity> existing = suspicionRepository.findRecentByUserAndAccountAndBank(
                    userName, accountId, bankBic, since);

            if (existing.isPresent()) {
                SuspicionEntity entity = existing.get();
                if (duplicateCount > entity.getDuplicateCount()) {
                    suspicionRepository.updateDuplicateCount(entity.getId(), duplicateCount);
                }
            } else {
                SuspicionEntity suspicion = SuspicionEntity.builder()
                        .userName(userName)
                        .accountId(accountId)
                        .bankBic(bankBic)
                        .duplicateCount(duplicateCount)
                        .analysisDate(analysisDate)
                        .build();

                suspicionRepository.save(suspicion);
            }
        });
    }
}
