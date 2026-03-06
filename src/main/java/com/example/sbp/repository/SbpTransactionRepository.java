package com.example.sbp.repository;

import com.example.sbp.entity.SbpTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SbpTransactionRepository extends JpaRepository<SbpTransaction, Long> {
    Optional<SbpTransaction> findByTransactionId(String transactionId);
    List<SbpTransaction> findBySenderBillId(Long senderBillId);
    List<SbpTransaction> findByReceiverBillId(Long receiverBillId);

    @Query("SELECT t FROM SbpTransaction t WHERE " +
            "t.senderBillId = :billId OR t.receiverBillId = :billId " +
            "ORDER BY t.createdAt DESC")
    List<SbpTransaction> findTransactionsByBillId(@Param("billId") Long billId);
}