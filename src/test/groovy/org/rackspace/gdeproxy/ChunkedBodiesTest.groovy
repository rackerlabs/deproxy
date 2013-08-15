
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
    void testChunkedRequestBody() {

        String body = """ This is another body

This is the next paragraph.
"""

        String length = Integer.toHexString(body.length());

        String requestString = ("GET / HTTP/1.1\r\n" +
                "Transfer-Endoding: chunked\r\n" +
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
            serverSideRequest = StaticTcpServer.run(server, responseString,
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

