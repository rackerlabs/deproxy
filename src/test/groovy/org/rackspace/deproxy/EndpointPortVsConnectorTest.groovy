package org.rackspace.deproxy

import spock.lang.Specification

class EndpointPortVsConnectorTest extends Specification {

    Deproxy deproxy

    def setup() {
        deproxy = new Deproxy()
    }

    def "when instantiating with port but no connectorFactory, create a new SocketServerConnector"() {

        given:
        int port = PortFinder.Singleton.getNextOpenPort()

        when:
        def endpoint = new Endpoint(deproxy, port)

        then:
        endpoint.serverConnector instanceof SocketServerConnector
        SocketServerConnector ssc = (SocketServerConnector)(endpoint.serverConnector)
        ssc.port == port
        ssc.endpoint == endpoint
    }

    def "when instantiating with no port but connectorFactory, use the factory"() {

        given:
        def connector = [ shutdown: { } ] as ServerConnector
        def factory = { endpoint -> connector }

        when:
        def endpoint = new Endpoint(deproxy, connectorFactory: factory)

        then:
        endpoint.serverConnector == connector
    }

    def "when instantiating with port and connectorFactory, ignore the port and use the factory"() {

        given:
        def connector = [
                confirm: 1,
                shutdown: { }

        ] as ServerConnector
        def factory = { endpoint -> connector }
        int port = PortFinder.Singleton.getNextOpenPort()

        when:
        def endpoint = new Endpoint(deproxy, port: port, connectorFactory: factory)

        then:
        endpoint.serverConnector == connector

    }

    def "when instantiating with neither port nor factory, grab an open port create a new SocketServerConnector"() {

        when:
        def endpoint = new Endpoint(deproxy)

        then:
        endpoint.serverConnector instanceof SocketServerConnector
        SocketServerConnector ssc = (SocketServerConnector)(endpoint.serverConnector)
        // this next assertion is possibly flaky if PortFinder.Singleton.getNextOpenPort()
        // gets called somewhere else between "new Endpoint(deproxy)" and here
        ssc.port == PortFinder.Singleton.currentPort
        ssc.endpoint == endpoint
    }

    def "when calling addEndpoint with port but no connectorFactory, create a new SocketServerConnector"() {

        given:
        int port = PortFinder.Singleton.getNextOpenPort()

        when:
        def endpoint = deproxy.addEndpoint(port)

        then:
        endpoint.serverConnector instanceof SocketServerConnector
        SocketServerConnector ssc = (SocketServerConnector)(endpoint.serverConnector)
        ssc.port == port
        ssc.endpoint == endpoint
    }

    def "when calling addEndpoint with no port but connectorFactory, use the factory"() {

        given:
        def connector = [ shutdown: { } ] as ServerConnector
        def factory = { endpoint -> connector }

        when:
        def endpoint = deproxy.addEndpoint(connectorFactory: factory)

        then:
        endpoint.serverConnector == connector
    }

    def "when calling addEndpoint with port and connectorFactory, ignore the port and use the factory"() {

        given:
        def connector = [
                confirm: 1,
                shutdown: { }

        ] as ServerConnector
        def factory = { endpoint -> connector }
        int port = PortFinder.Singleton.getNextOpenPort()

        when:
        def endpoint = deproxy.addEndpoint(port: port, connectorFactory: factory)

        then:
        endpoint.serverConnector == connector

    }

    def "when calling addEndpoint with neither port nor factory, grab an open port create a new SocketServerConnector"() {

        when:
        def endpoint = deproxy.addEndpoint()

        then:
        endpoint.serverConnector instanceof SocketServerConnector
        SocketServerConnector ssc = (SocketServerConnector)(endpoint.serverConnector)
        // this next assertion is possibly flaky if PortFinder.Singleton.getNextOpenPort()
        // gets called somewhere else between "new Endpoint(deproxy)" and here
        ssc.port == PortFinder.Singleton.currentPort
        ssc.endpoint == endpoint
    }

    def cleanup() {

        if (deproxy) {
            deproxy.shutdown()
        }
    }
}
