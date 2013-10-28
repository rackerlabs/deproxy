package org.rackspace.deproxy

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 */

public interface ClientConnector {
    Response sendRequest(Request request, boolean https, host, port, RequestParams params);
}