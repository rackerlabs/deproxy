package org.rackspace.gdeproxy

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.*

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
class BinaryBodiesTest {

    Deproxy deproxy;
    int port;
    String url;

    @Before
    void setUp() {

        this.deproxy = new Deproxy();
        PortFinder pf = new PortFinder();
        this.port = pf.getNextOpenPort();
        this.url = "http://localhost:${this.port}/";
        this.deproxy.addEndpoint(this.port);
    }

    @Test
    void testBinaryRequestBody() {
        byte[] body = (-128 .. 127) as byte[]

        def mc = this.deproxy.makeRequest(url: this.url,
                    method: "POST",
                    headers: [ 'Content-type': 'application/octet-stream'],
                    requestBody: body);

        assertEquals(1, mc.handlings.size());
        assertArrayEquals(body, mc.sentRequest.body);
        assertArrayEquals(body, mc.handlings[0].request.body);
    }

    @Test
    void testBinaryResponseBody() {
        byte[] body = (-128 .. 127) as byte[]

        def handler = { request ->
            return new Response(200, "OK", ['Content-type': 'application/octet-stream'], body);
        }

        def mc = this.deproxy.makeRequest(url: this.url, defaultHandler: handler);

        assertEquals(1, mc.handlings.size());
        assertArrayEquals(body, mc.handlings[0].response.body);
        assertArrayEquals(body, mc.receivedResponse.body);
    }

    @Test
    void testDefaultRequestHeadersForBinaryBody() {
        byte[] body = (-128 .. 127) as byte[]

        def mc = this.deproxy.makeRequest(url: this.url, method: "POST",
                requestBody: body);

        assertArrayEquals(body, mc.sentRequest.body);
        assertEquals(1, mc.sentRequest.headers.getCountByName("Content-Type"))
        assertEquals("application/octet-stream", mc.sentRequest.headers["Content-Type"])
        assertEquals(1, mc.handlings.size());
        assertArrayEquals(body, mc.handlings[0].request.body);
        assertEquals(1, mc.handlings[0].request.headers.getCountByName("Content-Type"))
        assertEquals("application/octet-stream", mc.handlings[0].request.headers["Content-Type"])
    }

    @Test
    void testNoDefaultRequestHeadersForBinaryBody() {
        byte[] body = (-128 .. 127) as byte[]

        def mc = this.deproxy.makeRequest(url: this.url, method: "POST",
                requestBody: body,
                addDefaultHeaders: false);

        assertArrayEquals(body, mc.sentRequest.body);
        assertEquals(0, mc.sentRequest.headers.getCountByName("Content-Type"))
        assertEquals(1, mc.handlings.size());
        assertEquals("", mc.handlings[0].request.body); // because there's no Content-Length header, it doesn't read the body
        assertEquals(0, mc.handlings[0].request.headers.getCountByName("Content-Type"))
    }

    @Test
    void testDefaultResponseHeadersForBinaryBody() {
        byte[] body = (-128 .. 127) as byte[]

        def handler = { request ->
            return new Response(200, "OK", null, body);
        }

        def mc = this.deproxy.makeRequest(url: this.url,
                                          defaultHandler: handler);

        assertArrayEquals(body, mc.receivedResponse.body);
        assertEquals(1, mc.receivedResponse.headers.getCountByName("Content-Type"))
        assertEquals("application/octet-stream", mc.receivedResponse.headers["Content-Type"])
        assertEquals(1, mc.handlings.size());
        assertArrayEquals(body, mc.handlings[0].response.body);
        assertEquals(1, mc.handlings[0].response.headers.getCountByName("Content-Type"))
        assertEquals("application/octet-stream", mc.handlings[0].response.headers["Content-Type"])
    }

    @Test
    void testNoDefaultResponseHeadersForBinaryBody() {
        byte[] body = (-128 .. 127) as byte[]

        def handler = { request, context ->
            context.sendDefaultResponseHeaders = false
            return new Response(200, "OK", null, body);
        }


        def mc = this.deproxy.makeRequest(url: this.url,
                                          defaultHandler: handler);


        assertEquals("200", mc.receivedResponse.code)
        assertEquals(1, mc.receivedResponse.headers.size())
        assertEquals(1, mc.receivedResponse.headers.getCountByName(Deproxy.REQUEST_ID_HEADER_NAME))
        assertEquals(0, mc.receivedResponse.headers.getCountByName("Content-Length"))
        assertEquals(0, mc.receivedResponse.headers.getCountByName("Content-Type"))
        assertEquals("", mc.receivedResponse.body);
        assertEquals(1, mc.handlings.size());
        assertEquals(1, mc.handlings[0].response.headers.size())
        assertEquals(1, mc.receivedResponse.headers.getCountByName(Deproxy.REQUEST_ID_HEADER_NAME))
        assertEquals(0, mc.handlings[0].response.headers.getCountByName("Content-Length"))
        assertEquals(0, mc.handlings[0].response.headers.getCountByName("Content-Type"))
        assertArrayEquals(body, mc.handlings[0].response.body);
    }

    @After
    void tearDown() {
        if (this.deproxy) {
            this.deproxy.shutdown();
        }
    }
}
