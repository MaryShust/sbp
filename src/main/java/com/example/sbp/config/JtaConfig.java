package com.example.sbp.config;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class JtaConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    /**
     * Atomikos XA DataSource для работы с JTA транзакциями.
     */
    @Bean(name = "dataSource", initMethod = "init", destroyMethod = "close")
    @Primary
    public AtomikosDataSourceBean dataSource() {
        AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
        ds.setUniqueResourceName("postgresDB");
        ds.setXaDataSourceClassName("org.postgresql.xa.PGXADataSource");

        Properties xaProperties = new Properties();
        xaProperties.setProperty("url", datasourceUrl);
        xaProperties.setProperty("user", datasourceUsername);
        xaProperties.setProperty("password", datasourcePassword);
        ds.setXaProperties(xaProperties);

        ds.setMinPoolSize(5);
        ds.setMaxPoolSize(20);
        ds.setBorrowConnectionTimeout(30);
        ds.setMaxIdleTime(60);
        ds.setTestQuery("SELECT 1");

        return ds;
    }

    /**
     * Atomikos UserTransactionManager - основная реализация JTA TransactionManager.
     */
    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager atomikosTransactionManager() {
        UserTransactionManager utm = new UserTransactionManager();
        utm.setForceShutdown(false);
        return utm;
    }

    /**
     * UserTransaction для управления транзакциями программно.
     */
    @Bean(name = "atomikosUserTransaction")
    @DependsOn("atomikosTransactionManager")
    public UserTransaction userTransaction() throws Exception {
        return atomikosTransactionManager();
    }

    /**
     * TransactionManager для интеграции с Spring.
     */
    @Bean(name = "atomikosJtaTransactionManager")
    @DependsOn("atomikosTransactionManager")
    public TransactionManager jtaTransactionManagerBean() throws Exception {
        return atomikosTransactionManager();
    }

    /**
     * JTA Transaction Manager - основной бин для управления транзакциями в Spring.
     */
    @Bean(name = "transactionManager")
    @Primary
    @DependsOn("atomikosTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("atomikosUserTransaction") UserTransaction userTransaction,
            @Qualifier("atomikosJtaTransactionManager") TransactionManager transactionManager) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setUserTransaction(userTransaction);
        jtaTransactionManager.setTransactionManager(transactionManager);
        return jtaTransactionManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    /**
     * JTA Platform для интеграции Atomikos с Hibernate.
     */
    @Bean
    @DependsOn("atomikosTransactionManager")
    public AtomikosJtaPlatform atomikosJtaPlatform(
            @Qualifier("atomikosUserTransaction") UserTransaction userTransaction,
            @Qualifier("atomikosJtaTransactionManager") TransactionManager transactionManager) {
        return new AtomikosJtaPlatform(transactionManager, userTransaction);
    }
}
