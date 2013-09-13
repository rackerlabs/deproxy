package org.rackspace.gdeproxy

import spock.lang.Specification
import spock.lang.Unroll

class DefaultClientConnectorTest2 extends Specification {

    Socket client
    Socket server

    @Unroll("when we call sendRequest with https=#https, #host, and #port, we should get Host: #expectedValue")
    void testConstructorWithSocketParameter() {

        given:
        String responseString = ("HTTP/1.1 200 OK\r\n" +
                "Server: StaticTcpServer\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n")

        (client, server) = LocalSocketPair.createLocalSocketPair()

        client.soTimeout = 2000 // in milliseconds
        server.soTimeout = 2000

        String serverSideRequest;

        DefaultClientConnector clientConnector = new DefaultClientConnector(client)
        Request request = new Request("GET", "/")
        RequestParams params = [sendDefaultRequestHeaders : true] as RequestParams

        def t = Thread.startDaemon("response") {
            serverSideRequest = StaticTcpServer.handleOneRequestTimeout(server, responseString, 200)
        }

        // we're explicitly setting the https, host, and port parameters.
        // the connector was created with a client socket, however.
        // it will use the socket instead of trying to open a new connection,
        // and just use the parameters for the Host header.
        Response response = clientConnector.sendRequest(request, https, host, port, params)
        t.join()

        expect:
        serverSideRequest != null
        serverSideRequest != ""
        serverSideRequest instanceof String

        when:
        StringReader reader = new StringReader(serverSideRequest)
        String requestLine = LineReader.readLine(reader)
        HeaderCollection headers = HeaderCollection.fromReadable(reader)

        then:
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
