package com.rackspace.deproxy.examples

import org.junit.Test
import com.rackspace.deproxy.Deproxy

class DisconnectConnectorTest {

    @Test
    void testConnector() {

        def deproxy = new Deproxy()
        def endpoint = deproxy.addEndpoint(9999)

//        def theProxy = new TheProxy(port: 8080,
//                targetHostname: "localhost",
//                targetPort: 9999)
        def theProxy = deproxy.addEndpoint(8080, null, null, Handlers.Route("localhost", 9999, false, new DefaultClientConnector()))

        def connector = new DisconnectConnector()

        def mc = deproxy.makeRequest(url: "http://localhost:8080/",
                headers: ['Host': 'localhost:8080'],
                clientConnector: connector,
                defaultHandler: connector.&handler)

        assert mc.handlings.size() == 1
        assert mc.handlings[0].response.code == "200"
        assert mc.receivedResponse == null
    }
}
