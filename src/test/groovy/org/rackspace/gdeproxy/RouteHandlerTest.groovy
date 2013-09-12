
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


//class TestRoute(unittest.TestCase):
//    def setUp(self):
//        self.deproxy_port = get_next_deproxy_port()
//        self.deproxy = deproxy.Deproxy()
//        self.end_point = self.deproxy.add_endpoint(self.deproxy_port)
//
//    def tearDown(self):
//        self.deproxy.shutdown_all_endpoints()
//
//    def test_route(self):
//        handler = deproxy.route('http', 'httpbin.org', self.deproxy)
//        mc = self.deproxy.make_request('http://localhost:%i/' %
//                                       self.deproxy_port,
//                                       default_handler=handler)
//        self.assertEquals(int(mc.received_response.code), 200)
//
//