package org.rackspace.gdeproxy

import groovy.util.logging.Log4j
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
@Log4j
class DefaultClientConnectorTest {

    String requestString = ("GET / HTTP/1.1\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n")

    String responseString = ("HTTP/1.1 200 OK\r\n" +
            "Server: some-closure\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n")

    @Test
    void testConstructorWithSocketParameter() {

        def (Socket client, Socket server) = LocalSocketPair.createLocalSocketPair()

        client.soTimeout = 30000 // in milliseconds
        server.soTimeout = 30000

        String serverSideRequest;

        def t = Thread.startDaemon("response") {
            serverSideRequest = StaticTcpServer.handleOneRequest(server, responseString, requestString.length())
        }

        DefaultClientConnector clientConnector = new DefaultClientConnector(client)
        Request request = new Request("GET", "/", ['Content-Length': '0'])
        RequestParams params = new RequestParams()
        params.sendDefaultRequestHeaders = false

        Response response = clientConnector.sendRequest(request, false, "localhost", server.port, params)



        assertEquals(requestString, serverSideRequest)
        assertEquals("200", response.code)
        assertEquals("OK", response.message)
        assertEquals(2, response.headers.size())
        assertTrue(response.headers.contains("Server"))
        assertEquals("some-closure", response.headers["Server"])
        assertTrue(response.headers.contains("Content-Length"))
        assertEquals("0", response.headers["Content-Length"])
        assertEquals("", response.body)

        client.close()
        server.close()
    }
}
