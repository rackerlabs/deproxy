package com.rackspace.deproxy

import spock.lang.Specification
import spock.lang.Unroll


class DefaultClientConnectorTest extends Specification {

    void "when we call sendRequest, we should get the standard default request headers"() {

        given: "a DefaultClientConnector using a dummy nextConnector"
        Request capturedRequest = null
        def clientConnector =
                new DefaultClientConnector([
                        'sendRequest': { request, https, host, port, params ->
                            capturedRequest = request;
                            return new Response(200)
                        }] as ClientConnector)

        and: "a simple request"
        Request request = new Request("GET", "/")



        when: "we send the request through the connector"
        RequestParams params = new RequestParams()
        Response response = clientConnector.sendRequest(request, false, "localhost", 80, params)

        then: "it formats the request correctly and includes the standard default request parameters"
        capturedRequest.method == 'GET'
        capturedRequest.path == '/'
        capturedRequest.headers.size() == 4
        capturedRequest.headers.contains("Host")
        capturedRequest.headers["Host"] == "localhost"
        capturedRequest.headers.contains("Accept")
        capturedRequest.headers["Accept"] == "*/*"
        capturedRequest.headers.contains("Accept-Encoding")
        capturedRequest.headers["Accept-Encoding"] == "identity, gzip, compress, deflate, *;q=0"
        capturedRequest.headers.contains("User-Agent")
        capturedRequest.headers["User-Agent"] == Deproxy.VERSION_STRING
        capturedRequest.body == "" || capturedRequest.body == null
        response.code == "200"
    }

    @Unroll
    void "when we call sendRequest with https=#https, #host, and #port, we should get Host: #expectedValue"() {

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



        when: "we send the request through the connector"
        def params = new RequestParams()
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
