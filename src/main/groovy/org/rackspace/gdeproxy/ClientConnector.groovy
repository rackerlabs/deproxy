package org.rackspace.gdeproxy

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 */

public interface ClientConnector {
    Response sendRequest(Request request, boolean https, host, port, RequestParams params);
}