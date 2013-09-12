
package org.rackspace.gdeproxy

import spock.lang.Ignore
import spock.lang.Specification

class RouteHandlerTest extends Specification {

    Deproxy deproxy
    int port

    def setup() {
        PortFinder pf = new PortFinder()
        port = pf.getNextOpenPort()

        deproxy = new Deproxy()
        deproxy.addEndpoint(port, "server", "localhost", this.&handler)
    }

    Response handler(Request request) {

        return new Response(606, "Spoiler", null, "Snape Kills Dumbledore!")
    }

    def testRoute() {

        given: "set up the route handler and the request"
        def router = Handlers.Route("localhost", port)
        Request request = new Request("METHOD", "/path/to/resource", ["Name": "Value"], "this is the body")

        when: "sending the request via the router"
        Response response = router(request)

        then: "the request is served by the DeproxyEndpoint and handler"
        response.code == "606"
        response.message == "Spoiler"
        response.body == "Snape Kills Dumbledore!"
    }

    def testRouteServerSideRequest() {

        given: "define the expected request string, and the string to return from the fake server"
        String requestString = ("METHOD /path/to/resource HTTP/1.1\r\n" +
                "Name: Value\r\n" +
                "Content-Length: 0\r\n" +
                "Host: localhost:${port}\r\n" +
                "\r\n")
        String responseString = ("HTTP/1.1 606 Spoiler\r\n" +
                "Server: StaticTcpServer\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n")

        and: "set up the fake server"
        def (Socket client, Socket server) = LocalSocketPair.createLocalSocketPair()
        client.soTimeout = 2000 // in milliseconds
        server.soTimeout = 2000
        String serverSideRequest;
        def t = Thread.startDaemon("response") {
            serverSideRequest = StaticTcpServer.handleOneRequest(server, responseString, requestString.length())
        }

        and: "set up the connector for Route to use"
        BareClientConnector connector = new BareClientConnector(client)

        and: "set up the router and request"
        def router = Handlers.Route("localhost", port, false, connector)
        Request request = new Request(
                "METHOD",
                "/path/to/resource",
                ["Name": "Value", "Content-Length": "0"])



        when: "sending the request via the router"
        Response response = router(request)

        and: "wait for the thread to assign the variable"
        t.join()

        then: "the request that the fake server received is what we expected"
        serverSideRequest == requestString

        and: "the request is served by the StaticTcpServer"
        response.code == "606"
        response.message == "Spoiler"
        response.headers.size() == 2
        response.headers.contains("Server")
        response.headers["Server"] == "StaticTcpServer"
        response.headers.contains("Content-Length")
        response.headers["Content-Length"] == "0"
    }


    @Ignore
    def testRouteHttps() {
        // DeproxyEndpoint doesn't yet support HTTPS
    }

    def testRouteConnector() {

        given: "set up the connector, route handler, and the request"

        ClientConnector connector = { Request request, boolean https, host, port, RequestParams params ->

            // this custom connector sends the request and adds a single
            // custom header to the response

            Response response = (new BareClientConnector()).sendRequest(request, https, host, port, params)

            response.headers.add("CustomConnector", "true")

            return response

        } as ClientConnector

        def router = Handlers.Route("localhost", port, false, connector)
        Request request = new Request("METHOD", "/path/to/resource", ["Name": "Value"], "this is the body")



        when: "sending the request via the router"
        Response response = router(request)

        then: "the request is served by the DeproxyEndpoint and handler"
        response.code == "606"
        response.message == "Spoiler"
        response.body == "Snape Kills Dumbledore!"

        and: "the response is modified by the connector"
        response.headers.contains("CustomConnector")
        response.headers["CustomConnector"] == "true"

    }

    def cleanup() {

        if (deproxy) {
            deproxy.shutdown()
        }
    }
}

