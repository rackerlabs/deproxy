=========================
 Using Deproxy in Tests
=========================

To use deproxy in your unit tests:

  1. In the test class's setup method, create a Deproxy object and endpoint(s), and configure your proxy to forward requests to the endpoint's port.
  2. In the actual test method, use the makeRequest method to send a request to the proxy, and get a message chain back.
  3. Still in the test method, make assertions against the returned message chain.
  4. In the cleanup method, shutdown the Deproxy object by calling shutdown().

Here's a code example of a unit test that tests the fictional theProxy library::

    import org.theProxy.*
    import org.rackspace.deproxy.*
    import org.junit.*
    import static org.junit.Assert.*

    class TestTheProxy {

        Deproxy deproxy
        DeproxyEndpoint endpoint
        TheProxy theProxy

        @Before
        void setup() {

            deproxy = new Deproxy()
            endpoint = deproxy.addEndpoint(9999)

            // Set up the proxy to listen on port 8080, forwarding requests to
            // localhost:9999
            theProxy = new TheProxy()
            theProxy.port = 8080
            theProxy.targetHostname = "localhost"
            theProxy.targetPort = 9999

            // Set up the proxy to add an X-Request header to requests
            theProxy.requestOperations.add(
                addHeaderOperation(name:  "X-Request",
                                   value: "This is a request"))

            // Set up the proxy to add an X-Response header to responses
            theProxy.responseOperations.add(
                addHeaderOperation(name:  "X-Response",
                                   value: "This is a response"))
        }

        @Test
        void testTheProxy() {

            def mc = deproxy.makeRequest(method: "GET",
                                         url: "http://localhost:8080/")

            // the endpoint returns a 200 by default
            assertEquals("200", mc.receivedResponse.code)

            // the request reached the endpoint once
            assertEquals(1, mc.handlings.size())

            // the X-Request header was not sent, but was added by the proxy and
            // received by the endpoint
            assertFalse(mc.sentRequest.headers.contains("X-Request"))
            assertTrue(mc.handlings[0].request.headers.contains("X-Request"))

            // the X-Response header was not sent by the endpoint, but was added
            // by the proxy and received by the client
            assertFalse(mc.handlings[0].response.headers.contains("X-Response"))
            assertTrue(mc.receivedResponse.headers.contains("X-Response"))
        }

        @After
        void cleanup() {

            if (theProxy) {
                theProxy.shutdown()
            }
            if (deproxy) {
                deproxy.shutdown()
            }
        }
    }
