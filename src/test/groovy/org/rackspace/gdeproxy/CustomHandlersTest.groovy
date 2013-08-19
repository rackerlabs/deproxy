/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.gdeproxy

import org.junit.*;
import static org.junit.Assert.*;

class CustomHandlersTest {

    int port;
    String url;
    Deproxy deproxy;
    DeproxyEndpoint endpoint;

    @Before
    public void setUp() {
        PortFinder pf = new PortFinder();
        this.port = pf.getNextOpenPort();
        this.url = "http://localhost:${this.port}/";
        this.deproxy = new Deproxy();
        this.endpoint = this.deproxy.addEndpoint(this.port);
    }

    @Test
    public void testCustomHandlerInlineClosure() {

        def mc = this.deproxy.makeRequest(url: this.url,
                defaultHandler: { request ->
                    new Response(
                            606,
                            "Spoiler",
                            ["Header-Name": "Header-Value"],
                            "Snape kills Dumbledore");
                });

        assertEquals(1, mc.handlings.size());
        assertEquals("606", mc.handlings[0].response.code);
        assertEquals("606", mc.receivedResponse.code);
    }

    def customHandlerMethod(request) {
        new Response(
                606,
                "Spoiler",
                ["Header-Name": "Header-Value"],
                "Snape kills Dumbledore");
    }

    @Test
    public void testCustomHandlerMethod() {
        def mc = this.deproxy.makeRequest(url: this.url,
                defaultHandler: this.&customHandlerMethod);

        assertEquals(1, mc.handlings.size());
        assertEquals("606", mc.handlings[0].response.code);
        assertEquals("606", mc.receivedResponse.code);
    }

    public static Response customHandlerStaticMethod(Request request) {
        return new Response(
                606,
                "Spoiler",
                ["Header-Name": "Header-Value"],
                "Snape kills Dumbledore");
    }

    @Test
    public void testCustomHandlerStaticMethod() {
        def mc = this.deproxy.makeRequest(url: this.url,
                defaultHandler: CustomHandlersTest.&customHandlerStaticMethod);

        assertEquals(1, mc.handlings.size());
        assertEquals("606", mc.handlings[0].response.code);
        assertEquals("606", mc.receivedResponse.code);
    }


    @Test
    public void testCustomHandlerInlineClosureWithContext() {

        def mc = this.deproxy.makeRequest(url: this.url,
                defaultHandler: { request, context ->
                    new Response(
                            606,
                            "Spoiler",
                            ["Header-Name": "Header-Value", "Context-Object": context],
                            "Snape kills Dumbledore");
                });

        assertEquals("606", mc.receivedResponse.code);
        assertTrue(mc.receivedResponse.headers.contains("Header-Name"))
        assertEquals("Header-Value", mc.receivedResponse.headers["Header-Name"])
        assertTrue(mc.receivedResponse.headers.contains("Context-Object"))
        assertEquals(1, mc.handlings.size());
        assertEquals("606", mc.handlings[0].response.code);
        assertTrue(mc.handlings[0].response.headers.contains("Header-Name"))
        assertEquals("Header-Value", mc.handlings[0].response.headers["Header-Name"])
        assertTrue(mc.handlings[0].response.headers.contains("Context-Object"))
    }

    def customHandlerMethodWithContext(Request request,
                                       HandlerContext context) {
        new Response(
                606,
                "Spoiler",
                ["Header-Name": "Header-Value", "Context-Object": context],
                "Snape kills Dumbledore");
    }

    @Test
    public void testCustomHandlerMethodWithContext() {
        def mc = this.deproxy.makeRequest(url: this.url,
                defaultHandler: this.&customHandlerMethodWithContext);

        assertEquals("606", mc.receivedResponse.code);
        assertTrue(mc.receivedResponse.headers.contains("Header-Name"))
        assertEquals("Header-Value", mc.receivedResponse.headers["Header-Name"])
        assertTrue(mc.receivedResponse.headers.contains("Context-Object"))
        assertEquals(1, mc.handlings.size());
        assertEquals("606", mc.handlings[0].response.code);
        assertTrue(mc.handlings[0].response.headers.contains("Header-Name"))
        assertEquals("Header-Value", mc.handlings[0].response.headers["Header-Name"])
        assertTrue(mc.handlings[0].response.headers.contains("Context-Object"))
    }

    public static Response customHandlerStaticMethodWithContext(
            Request request,
            HandlerContext context) {
        return new Response(
                606,
                "Spoiler",
                ["Header-Name": "Header-Value", "Context-Object": context],
                "Snape kills Dumbledore");
    }

    @Test
    public void testCustomHandlerStaticMethodWithContext() {
        def mc = this.deproxy.makeRequest(url: this.url,
                defaultHandler: CustomHandlersTest.&customHandlerStaticMethodWithContext);

        assertEquals("606", mc.receivedResponse.code);
        assertTrue(mc.receivedResponse.headers.contains("Header-Name"))
        assertEquals("Header-Value", mc.receivedResponse.headers["Header-Name"])
        assertTrue(mc.receivedResponse.headers.contains("Context-Object"))
        assertEquals(1, mc.handlings.size());
        assertEquals("606", mc.handlings[0].response.code);
        assertTrue(mc.handlings[0].response.headers.contains("Header-Name"))
        assertEquals("Header-Value", mc.handlings[0].response.headers["Header-Name"])
        assertTrue(mc.handlings[0].response.headers.contains("Context-Object"))
    }

    @After
    public void tearDown() {
        if (this.deproxy) {
            this.deproxy.shutdown();
        }
    }
}

//class TestCustomHandlers(unittest.TestCase):
//    def setUp(self):
//        self.deproxy_port = get_next_deproxy_port()
//        self.deproxy = deproxy.Deproxy()
//        self.end_point = self.deproxy.add_endpoint(self.deproxy_port)
//
//    def tearDown(self):
//        self.deproxy.shutdown_all_endpoints()
//
//    def test_custom_handler_function(self):
//        def custom_handler(request):
//            return deproxy.Response(code=606, message="Spoiler",
//                                    headers={"Header-Name": "Header-Value"},
//                                    body='Snape Kills Dumbledore')
//        mc = self.deproxy.make_request('http://localhost:%i/' %
//                                       self.deproxy_port,
//                                       default_handler=custom_handler)
//        self.assertEquals(int(mc.received_response.code), 606)
//
//    def handler_method(self, request):
//        return deproxy.Response(code=606, message="Spoiler",
//                                headers={"Header-Name": "Header-Value"},
//                                body='Snape Kills Dumbledore')
//
//    def test_custom_handler_method(self):
//        mc = self.deproxy.make_request('http://localhost:%i/' %
//                                       self.deproxy_port,
//                                       default_handler=self.handler_method)
//        self.assertEquals(int(mc.received_response.code), 606)
//
//