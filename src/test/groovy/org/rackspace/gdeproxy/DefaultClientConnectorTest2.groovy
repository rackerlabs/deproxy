package org.rackspace.gdeproxy

import spock.lang.Specification
import spock.lang.Unroll


class DefaultClientConnectorTest2 extends Specification {

    Socket client
    Socket server

    void testConstructorWithSocketParameter() {

        given: "a client socket and a server socket"
        (client, server) = LocalSocketPair.createLocalSocketPair()

        client.soTimeout = 100 // in milliseconds
        server.soTimeout = 2000

        and: "a DefaultClientConnector using the provided client socket"
        DefaultClientConnector clientConnector = new DefaultClientConnector(client)

        and: "a simple request"
        Request request = new Request("GET", "/", ['Content-Length': "0"])

        and: "request params that don't involve adding default headers"
        RequestParams params = [sendDefaultRequestHeaders : false] as RequestParams


        when: "we send the request through the connector"
        try {

            Response response = clientConnector.sendRequest(request, false, "localhost", server.getLocalPort(), params)

        } catch (SocketTimeoutException ignored) {
            // read times out, as expected
        }

        and: "read the request that the connector sent from the server-side socket"
        String requestLine = LineReader.readLine(server.inputStream)
        HeaderCollection headers = HeaderCollection.fromStream(server.inputStream)
        String body = BodyReader.readBody(server.inputStream, headers)


        then: "it formats the request correctly and only has the header we specified"
        requestLine == "GET / HTTP/1.1"
        headers.size() == 1
        headers.contains("Content-Length")
        headers["Content-Length"] == "0"
        body == "" || body == null
    }

    @Unroll("when we call sendRequest with https=#https, #host, and #port, we should get Host: #expectedValue")
    void testHostHeader() {

        given: "a client socket and a server socket"
        (client, server) = LocalSocketPair.createLocalSocketPair()

        client.soTimeout = 100 // in milliseconds
        server.soTimeout = 2000

        and: "a DefaultClientConnector using the provided client socket"
        DefaultClientConnector clientConnector = new DefaultClientConnector(client)

        and: "a simple request and basic request params"
        Request request = new Request("GET", "/")
        RequestParams params = [sendDefaultRequestHeaders : true] as RequestParams



        when: "we send the request through the connector"
        try {

            // we're explicitly setting the https, host, and port parameters.
            // the connector was created with a client socket, however. it
            // will use the socket instead of trying to open a new connection,
            // and just use the parameters for the Host header.
            Response response = clientConnector.sendRequest(request, https, host, port, params)

        } catch (SocketTimeoutException ignored) {

            // we're expecting the connector to send the request, and then
            // wait for a server response. since there is no server in this
            // case, it will timeout while waiting. then we just read the
            // request from the server side of the socket.
        }

        and: "read the request that the connector sent from the server-side socket"
        String requestLine = LineReader.readLine(server.inputStream)
        HeaderCollection headers = HeaderCollection.fromStream(server.inputStream)



        then: "it formats the request correctly and has a Host header with the right value"
        requestLine == "GET / HTTP/1.1"
        headers.contains("Host")
        headers["Host"] == expectedValue

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

    def cleanup() {
        if (client) {
            client.close()
        }
        if (server) {
            server.close()
        }
    }
}
