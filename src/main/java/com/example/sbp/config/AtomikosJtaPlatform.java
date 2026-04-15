package com.example.sbp.config;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;

/**
 * Интеграция Atomikos с Hibernate 6.x.
 * Реализует JtaPlatform для корректной работы JTA транзакций.
 */
public class AtomikosJtaPlatform implements JtaPlatform {

    private static final long serialVersionUID = 1L;

    private final TransactionManager transactionManager;
    private final UserTransaction userTransaction;

    public AtomikosJtaPlatform(TransactionManager transactionManager, UserTransaction userTransaction) {
        this.transactionManager = transactionManager;
        this.userTransaction = userTransaction;
    }

    @Override
    public TransactionManager retrieveTransactionManager() {
        return transactionManager;
    }

    @Override
    public UserTransaction retrieveUserTransaction() {
        return userTransaction;
    }

    @Override
    public int getCurrentStatus() {
        try {
            return transactionManager != null ? transactionManager.getStatus() : Status.STATUS_NO_TRANSACTION;
        } catch (Exception e) {
            return Status.STATUS_NO_TRANSACTION;
        }
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        try {
            if (transactionManager != null && transactionManager.getTransaction() != null) {
                transactionManager.getTransaction().registerSynchronization(synchronization);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to register synchronization", e);
        }
    }

    @Override
    public boolean canRegisterSynchronization() {
        try {
            return transactionManager != null
                && transactionManager.getTransaction() != null
                && transactionManager.getStatus() == Status.STATUS_ACTIVE;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object getTransactionIdentifier(Transaction transaction) {
        return transaction != null ? transaction.hashCode() : null;
    }
}
