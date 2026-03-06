package com.example.sbp.repository;

import com.example.sbp.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByAccountId(Long accountId);

    @Query("SELECT b FROM Bill b WHERE b.id = :billId AND b.isActive = true")
    Optional<Bill> findActiveById(@Param("billId") Long billId);
}