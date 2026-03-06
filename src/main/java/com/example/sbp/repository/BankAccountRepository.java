package com.example.sbp.repository;

import com.example.sbp.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    Optional<BankAccount> findByPhoneNumber(String phoneNumber);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByPhoneNumber(String phoneNumber);

//    @Query("SELECT ba FROM BankAccount ba WHERE ba.phoneNumber = :phone OR ba.accountNumber = :identifier")
//    Optional<BankAccount> findByPhoneOrAccount(
//            @Param("phone") String phone,
//            @Param("identifier") String identifier
//    );

    Optional<BankAccount> findByPhoneNumberOrAccountNumber(
            String phoneNumber,
            String accountNumber
    );
}