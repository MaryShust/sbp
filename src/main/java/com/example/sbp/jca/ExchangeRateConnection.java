package com.example.sbp.jca;

import java.io.Serializable;

public interface ExchangeRateConnection extends Serializable {
    String getExchangeRate(String baseCurrency);
    void close();
}