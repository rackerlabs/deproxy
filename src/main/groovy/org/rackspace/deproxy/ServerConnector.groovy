package org.rackspace.deproxy

public interface ServerConnector {

    void setEndpoint(DeproxyEndpoint endpoint);

    void shutdown();

}