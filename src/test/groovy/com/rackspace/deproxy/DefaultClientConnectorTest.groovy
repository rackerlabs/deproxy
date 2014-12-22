package com.rackspace.deproxy

import spock.lang.Specification
import spock.lang.Unroll


class DefaultClientConnectorTest extends Specification {

    void testConstructorWithSocketParameter() {

        given: "a DefaultClientConnector using a dummy nextConnector"
        Request capturedRequest = null
        def clientConnector =
                new DefaultClientConnector([
                        'sendRequest': { request, https, host, port, params ->
                            capturedRequest = request;
                            return new Response(200)
                        }] as ClientConnector)

        and: "a simple request"
        Request request = new Request("GET", "/", ['Content-Length': "0"])

        and: "request params that don't involve adding default headers"
        RequestParams params = [sendDefaultRequestHeaders : false] as RequestParams


        when: "we send the request through the connector"
        Response response = clientConnector.sendRequest(request, false, "localhost", 80, params)

        then: "it formats the request correctly and only has the header we specified"
        capturedRequest.method == 'GET'
        capturedRequest.path == '/'
        capturedRequest.headers.size() == 1
        capturedRequest.headers.contains("Content-Length")
        capturedRequest.headers["Content-Length"] == "0"
        capturedRequest.body == "" || capturedRequest.body == null
        response.code == "200"
    }

    @Unroll("when we call sendRequest with https=#https, #host, and #port, we should get Host: #expectedValue")
    void testHostHeader() {

        given: "a DefaultClientConnector using a dummy nextConnector"
        Request capturedRequest = null
        def clientConnector =
                new DefaultClientConnector([
                        'sendRequest': { request, https, host, port, params ->
                            capturedRequest = request;
                            return new Response(200)
                        }] as ClientConnector)

        and: "a simple request and basic request params"
        Request request = new Request("GET", "/")
        RequestParams params = [sendDefaultRequestHeaders : true] as RequestParams



        when: "we send the request through the connector"
        Response response = clientConnector.sendRequest(request, https, host, port, params)

        then: "it formats the request correctly and has a Host header with the right value"
        capturedRequest.method == 'GET'
        capturedRequest.path == '/'
        capturedRequest.headers.contains("Host")
        capturedRequest.headers["Host"] == expectedValue
        response.code == "200"



        where:
        host          | port  | https | expectedValue
        "localhost"   | 80    | true  | "localhost:80"
        "localhost"   | 80    | false | "localhost"
        "localhost"   | 443   | true  | "localhost"
        "localhost"   | 443   | false | "localhost:443"
        "localhost"   | 12345 | true  | "localhost:12345"
        "localhost"   | 12345 | false | "localhost:12345"
        "example.com" | 80    | true  | "example.com:80"
        "example.com" | 80    | false | "example.com"
        "example.com" | 443   | true  | "example.com"
        "example.com" | 443   | false | "example.com:443"
        "example.com" | 12345 | true  | "example.com:12345"
        "example.com" | 12345 | false | "example.com:12345"
        "12.34.56.78" | 80    | true  | "12.34.56.78:80"
        "12.34.56.78" | 80    | false | "12.34.56.78"
        "12.34.56.78" | 443   | true  | "12.34.56.78"
        "12.34.56.78" | 443   | false | "12.34.56.78:443"
        "12.34.56.78" | 12345 | true  | "12.34.56.78:12345"
        "12.34.56.78" | 12345 | false | "12.34.56.78:12345"
    }
}
