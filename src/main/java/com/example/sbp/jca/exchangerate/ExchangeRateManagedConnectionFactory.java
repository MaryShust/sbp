package com.example.sbp.jca.exchangerate;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Set;

public class ExchangeRateManagedConnectionFactory implements ManagedConnectionFactory, Serializable {

    private PrintWriter logWriter;

//    public ExchangeRateManagedConnectionFactory() {
//    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return new ExchangeRateConnectionFactory(this);
    }

    @Override
    public Object createConnectionFactory(jakarta.resource.spi.ConnectionManager cm) throws ResourceException {
        return new ExchangeRateConnectionFactory(cm, this);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxrInfo) throws ResourceException {
        return new ExchangeRateManagedConnection();
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxrInfo) throws ResourceException {
        for (Object obj : connectionSet) {
            if (obj instanceof ExchangeRateManagedConnection) {
                return (ManagedConnection) obj;
            }
        }
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }
}
