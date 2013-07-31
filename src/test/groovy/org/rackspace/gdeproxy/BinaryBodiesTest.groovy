package org.rackspace.gdeproxy

import org.junit.After
import org.junit.Before
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
    void testRequestBinaryBody() {
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
    void testResponseBinaryBody() {
        byte[] body = (-128 .. 127) as byte[]

        def handler = { request ->
            return new Response(200, "OK", ['Content-type': 'application/octet-stream'], body);
        }

        def mc = this.deproxy.makeRequest(url: this.url, defaultHandler: handler);

        assertEquals(1, mc.handlings.size());
        assertArrayEquals(body, mc.handlings[0].response.body);
        assertArrayEquals(body, mc.receivedResponse.body);
    }

    @After
    void tearDown() {
        if (this.deproxy) {
            this.deproxy.shutdown();
        }
    }
}
