
package org.rackspace.gdeproxy

import groovy.util.logging.Log4j;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author izrik
 */
@Log4j
public class ChunkedBodiesTest {
	
    Deproxy deproxy;
    int port;
    String url;
    DeproxyEndpoint endpoint;

    @Before
    void setUp() {
        this.deproxy = new Deproxy();
        PortFinder pf = new PortFinder();
        this.port = pf.getNextOpenPort();
        this.url = "http://localhost:${this.port}/";
        this.endpoint = this.deproxy.addEndpoint(this.port);
    }

    Socket client
    Socket server

    @Test
    void testChunkedRequestBodyInBareClientConnector() {

        String body = """ This is another body

This is the next paragraph.
"""

        String length = Integer.toHexString(body.length());

        String requestString = ("GET / HTTP/1.1\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "\r\n" +
                "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n") // end of chunked body, no trailer

        String responseString = ("HTTP/1.1 200 OK\r\n" +
                "Server: StaticTcpServer\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n")

        (client, server) = LocalSocketPair.createLocalSocketPair()
        client.soTimeout = 5000
        server.soTimeout = 5000

        String serverSideRequest

        def t = Thread.startDaemon("static-tcp-server") {
            serverSideRequest = StaticTcpServer.handleOneRequest(server, responseString,
                    requestString.length())
        }

        Request request = new Request("GET", "/",
                ["Transfer-Encoding": "chunked"], body)
        RequestParams params = new RequestParams()
        params.usedChunkedTransferEncoding = true

        BareClientConnector clientConnector = new BareClientConnector(client)

        Response response = clientConnector.sendRequest(request, false,
                "localhost", server.localPort, params)



        assertEquals(requestString, serverSideRequest)
        assertEquals("200", response.code)
        assertEquals("OK", response.message)
        assertEquals(2, response.headers.size())
        assertTrue(response.headers.contains("Server"))
        assertEquals("StaticTcpServer", response.headers["Server"])
        assertTrue(response.headers.contains("Content-Length"))
        assertEquals("0", response.headers["Content-Length"])
        assertEquals("", response.body)
    }

    @Test
    void testChunkedRequestBodyInDefaultClientConnector() {

        String body = """ This is another body

This is the next paragraph.
"""

        String length = Integer.toHexString(body.length());

        String requestString = ("GET / HTTP/1.1\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "Host: localhost\r\n" +
                "Accept: */*\r\n" +
                "Accept-Encoding: identity\r\n" +
                "User-Agent: ${Deproxy.VERSION_STRING}\r\n" +
                "\r\n" +
                "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n") // end of chunked body, no trailer

        String responseString = ("HTTP/1.1 200 OK\r\n" +
                "Server: StaticTcpServer\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n")

        (client, server) = LocalSocketPair.createLocalSocketPair()
        client.soTimeout = 5000
        server.soTimeout = 5000

        String serverSideRequest

        def t = Thread.startDaemon("static-tcp-server") {
            serverSideRequest = StaticTcpServer.handleOneRequest(server, responseString,
                    requestString.length())
        }

        Request request = new Request("GET", "/", [:], body)
        RequestParams params = new RequestParams()
        params.usedChunkedTransferEncoding = true

        DefaultClientConnector clientConnector = new DefaultClientConnector(client)

        Response response = clientConnector.sendRequest(request, false,
                "localhost", server.localPort, params)



        assertEquals(requestString, serverSideRequest)
        assertEquals("200", response.code)
        assertEquals("OK", response.message)
        assertEquals(2, response.headers.size())
        assertTrue(response.headers.contains("Server"))
        assertEquals("StaticTcpServer", response.headers["Server"])
        assertTrue(response.headers.contains("Content-Length"))
        assertEquals("0", response.headers["Content-Length"])
        assertEquals("", response.body)
    }

    @Test
    void testChunkedRequestBodyInDeproxyEndpoint() {

        // setup - create canned request; setup deproxy and endpoint

        Deproxy deproxy = new Deproxy();
        PortFinder pf = new PortFinder();
        int port = pf.getNextOpenPort();
        String url = "http://localhost:${port}/";
        DeproxyEndpoint endpoint = deproxy.addEndpoint(port);

        String body = """ This is another body\n\r\nThis is the next paragraph.\n"""

        String length = Integer.toHexString(body.length());
        String requestString = ("GET / HTTP/1.1\r\n" +
                "Host: localhost:${port}\r\n" +
                "Content-Type: text/plain\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "Accept: */*\r\n" +
                "Accept-Encoding: identity\r\n" +
                "User-Agent: Canned-String\r\n" +
                "\r\n" +
                "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n") // end of chunked body, no trailer

        String responseString = ("HTTP/1.1 200 OK\r\n" +
                "Server: StaticTcpServer\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n")



        client = endpoint.createRawConnection()
        client.soTimeout = 3000

        MessageChain mc
        def t = Thread.startDaemon("request-on-the-side-to-get-orphan") {
            mc = deproxy.makeRequest(url: url, defaultHandler: Handlers.Delay(2000),
                                     addDefaultHeaders: true)
        }

        client.outputStream.write(requestString.getBytes("US-ASCII"))


        t.join()


        assertEquals(1, mc.orphanedHandlings.size())
        assertEquals(body, mc.orphanedHandlings[0].request.body)

    }

    @Ignore
    @Test
    void testChunkedResponseBody() {
        def body = """ This is another body

            This is the next paragraph.
            """
        def length = body.length();
        def chunkedBody = "${length}\r\n${body}\r\n0\r\n\r\n";

        def handler = { request, context ->
            context.sendChunkedResponse = true
            return new Response(200, "OK", [:], body);
        }

        def mc = this.deproxy.makeRequest(url: this.url, defaultHandler: handler);

        assertEquals(1, mc.handlings.size());
        assertTrue(mc.handlings[0].response.headers.contains("Transfer-Encoding"))
        assertEquals("chunked", mc.handlings[0].response.headers.getFirstValue("Transfer-Encoding"))
        assertEquals(chunkedBody, mc.handlings[0].response.body);
        assertTrue(mc.receivedResponse.headers.contains("Transfer-Encoding"))
        assertEquals("chunked", mc.receivedResponse.headers.getFirstValue("Transfer-Encoding"))
        assertEquals(body, mc.receivedResponse.body);
    }
    
    @After
    void tearDown() {
        if (this.deproxy) {
            this.deproxy.shutdown();
        }

        if (client) {
            client.close()
            client = null
        }

        if (server) {
            server.close()
            server = null
        }
    }

}

