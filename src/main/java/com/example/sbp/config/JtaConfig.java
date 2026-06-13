//package com.example.sbp.config;
//
//import jakarta.transaction.TransactionManager;
//import jakarta.transaction.UserTransaction;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//import org.springframework.transaction.jta.JtaTransactionManager;
//
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//
//@Slf4j
//@Configuration
//@EnableTransactionManagement
//public class JtaConfig {
//
//    @Bean
//    public UserTransaction userTransaction() throws NamingException {
//        InitialContext ctx = new InitialContext();
//        UserTransaction ut = (UserTransaction) ctx.lookup("java:jboss/UserTransaction");
//        log.info("UserTransaction: {}", ut);
//        return ut;
//    }
//
//    @Bean
//    public TransactionManager transactionManager() throws NamingException {
//        InitialContext ctx = new InitialContext();
//        TransactionManager tm = (TransactionManager) ctx.lookup("java:jboss/TransactionManager");
//        log.info("TransactionManager: {}", tm);
//        return tm;
//    }
//
//    @Bean(name = "transactionManager")
//    @Primary
//    public PlatformTransactionManager jtaTransactionManager() throws NamingException {
//
//        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
//        jtaTransactionManager.setUserTransaction(userTransaction());
//        jtaTransactionManager.setTransactionManager(transactionManager());
//        log.info("JtaTransactionManager создан для WildFly JTA");
//        return jtaTransactionManager;
//    }
//}
