package com.example.sbp.jca.exchangerate;

import java.io.IOException;
import java.io.Serializable;
import com.example.sbp.jca.ExchangeRateConnection;

public class ExchangeRateConnectionImpl implements ExchangeRateConnection, Serializable {

    private final ExchangeRateManagedConnection managedConnection;
    private volatile boolean closed;

    ExchangeRateConnectionImpl(ExchangeRateManagedConnection managedConnection) {
        this.managedConnection = managedConnection;
    }

    @Override
    public synchronized String getExchangeRate(String baseCurrency) {
        if (closed) {
            throw new IllegalStateException("Connection is closed");
        }
        try {
            return managedConnection.fetchRates(baseCurrency);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch exchange rate", e);
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
    }
}
