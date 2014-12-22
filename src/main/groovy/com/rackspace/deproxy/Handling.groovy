package com.rackspace.deproxy

/**
 * An object representing a request received by an endpoint and the
 * response it returns.
 */
class Handling {

    Endpoint endpoint
    Request request
    Response response
    String connection

    public Handling(Endpoint endpoint, Request request, Response response, String connection) {
        this.endpoint = endpoint;
        this.request = request;
        this.response = response;
        this.connection = connection;
    }

    String toString() {
        sprintf('Handling(endpoint=%s, request=%s, response=%s, connection=%s)',
                endpoint,
                request,
                response,
                connection)
    }

}
