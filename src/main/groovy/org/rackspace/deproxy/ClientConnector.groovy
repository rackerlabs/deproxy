package org.rackspace.deproxy

public interface ClientConnector {
    Response sendRequest(Request request, boolean https, host, port, RequestParams params);
}