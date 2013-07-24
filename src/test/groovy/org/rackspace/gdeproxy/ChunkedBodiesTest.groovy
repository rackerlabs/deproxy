
package org.rackspace.gdeproxy

import groovy.util.logging.Log4j;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author izrik
 */
@Log4j
public class ChunkedBodyTest {
	
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

    @Test
    void testChunkedRequestBody() {

        def body = """ This is another body

            This is the next paragraph.
            """

        def length = body.length();
        def chunkedBody = "${length}\r\n${body}\r\n0\r\n\r\n";

        def mc = this.deproxy.makeRequest(url: this.url, method: "POST", 
                                          requestBody: body, chunked: true);

        assertTrue(mc.sentRequest.headers.contains("Transfer-Encoding"));
        assertEquals("chunked", mc.sentRequest.headers.getFirstValue("Transfer-Encoding"));
        assertEquals(chunkedBody, mc.sentRequest.body);
        assertEquals(1, mc.handlings.size());
        assertTrue(mc.handlings[0].request.headers.contains("Transfer-Encoding"));
        assertEquals("chunked", mc.handlings[0].request.headers.getFirstValue("Transfer-Encoding"));
        assertEquals(body, mc.handlings[0].request.body);
    }
    
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
    }

}

