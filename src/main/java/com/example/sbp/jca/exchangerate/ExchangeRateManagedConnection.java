package com.example.sbp.jca.exchangerate;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionMetaData;
import jakarta.resource.spi.LocalTransaction;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

public class ExchangeRateManagedConnection implements ManagedConnection, LocalTransaction, Serializable {

    private volatile boolean destroyed;
    private PrintWriter logWriter;

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxrInfo) throws ResourceException {
        if (destroyed) {
            throw new ResourceException("Connection is destroyed");
        }
        return new ExchangeRateConnectionImpl(this);
    }

    @Override
    public void destroy() {
        destroyed = true;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
    }

    @Override
    public ManagedConnectionMetaData getMetaData() {
        return new ExchangeRateManagedConnectionMetaData();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return this;
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        return null;
    }

    @Override
    public void begin() throws ResourceException {
    }

    @Override
    public void commit() throws ResourceException {
    }

    @Override
    public void rollback() throws ResourceException {
    }

    String fetchRates(String baseCurrency) throws IOException, InterruptedException {
        String url = String.format("https://open.er-api.com/v6/latest/%s", baseCurrency.toUpperCase());
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
