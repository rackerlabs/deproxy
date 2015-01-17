package com.rackspace.deproxy

public interface ServerConnector {

    int port();

    void shutdown();

}