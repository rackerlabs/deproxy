/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.deproxy

import org.junit.*;
import static org.junit.Assert.*;


class BodiesTest {

    Deproxy deproxy;
    int port;
    String url;

    @Before
    void setUp() {
        this.deproxy = new Deproxy();
        this.port = PortFinder.Singleton.getNextOpenPort();
        this.url = "http://localhost:${this.port}/";
        this.deproxy.addEndpoint(this.port);
    }

    @Test
    void testRequestBody() {
        def body = """ This is another body

        This is the next paragraph.
        """
        def mc = this.deproxy.makeRequest(url: this.url, method: "POST", requestBody: body);

        assertEquals(1, mc.handlings.size());
        assertEquals(body, mc.sentRequest.body);
        assertEquals(body, mc.handlings[0].request.body);
    }

    @Test
    void testResponseBody() {
        def body = """ This is another body

        This is the next paragraph.
        """

        def handler = { request ->
            return new Response(200, "OK", ['Content-type': 'text/plain'], body);
        }

        def mc = this.deproxy.makeRequest(url: this.url, defaultHandler: handler);

        assertEquals(1, mc.handlings.size());
        assertEquals(body, mc.handlings[0].response.body);
        assertEquals(body, mc.receivedResponse.body);
    }

    @Test
    void testDefaultRequestHeadersForTextBody() {
        def body = """ This is another body

        This is the next paragraph.
        """

        def mc = this.deproxy.makeRequest(url: this.url, method: "POST",
                requestBody: body);

        assertEquals(body, mc.sentRequest.body);
        assertEquals(1, mc.sentRequest.headers.getCountByName("Content-Type"))
        assertEquals("text/plain", mc.sentRequest.headers["Content-Type"])
        assertEquals(1, mc.handlings.size());
        assertEquals(body, mc.handlings[0].request.body);
        assertEquals(1, mc.handlings[0].request.headers.getCountByName("Content-Type"))
        assertEquals("text/plain", mc.handlings[0].request.headers["Content-Type"])
    }

    @Test
    void testNoDefaultRequestHeadersForTextBody() {
        def body = """ This is another body

        This is the next paragraph.
        """

        def mc = this.deproxy.makeRequest(url: this.url, method: "POST",
                requestBody: body,
                addDefaultHeaders: false);

        assertEquals(body, mc.sentRequest.body);
        assertEquals(0, mc.sentRequest.headers.getCountByName("Content-Type"))
        assertEquals(1, mc.handlings.size());
        assertEquals("", mc.handlings[0].request.body); // because there's no Content-Length header, it doesn't read the body
        assertEquals(0, mc.handlings[0].request.headers.getCountByName("Content-Type"))
    }

    @Test
    void testDefaultResponseHeadersForTextBody() {
        def body = """ This is another body

        This is the next paragraph.
        """

        def handler = { request ->
            return new Response(200, "OK", null, body);
        }

        def mc = this.deproxy.makeRequest(url: this.url,
                defaultHandler: handler);

        assertEquals(body, mc.receivedResponse.body);
        assertEquals(1, mc.receivedResponse.headers.getCountByName("Content-Type"))
        assertEquals("text/plain", mc.receivedResponse.headers["Content-Type"])
        assertEquals(1, mc.handlings.size());
        assertEquals(body, mc.handlings[0].response.body);
        assertEquals(1, mc.handlings[0].response.headers.getCountByName("Content-Type"))
        assertEquals("text/plain", mc.handlings[0].response.headers["Content-Type"])
    }

    @Test
    void testNoDefaultResponseHeadersForTextBody() {
        def body = """ This is another body

        This is the next paragraph.
        """

        def handler = { request, HandlerContext context ->
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
        assertEquals(body, mc.handlings[0].response.body);
    }

    @After
    void tearDown() {
        if (this.deproxy) {
            this.deproxy.shutdown();
        }
    }


}