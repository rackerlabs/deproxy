package com.rackspace.deproxy

import spock.lang.Specification
import spock.lang.Unroll


class makeRequestParamsTest extends Specification {

    Deproxy deproxy;
    int port;
    String urlbase;

    def setup() {

        this.deproxy = new Deproxy();
        this.port = PortFinder.Singleton.getNextOpenPort();
        this.urlbase = "http://localhost:${this.port}";
        this.deproxy.addEndpoint(this.port);
    }

    @Unroll("url param: path and query parameter combos: \"#pathPart#queryPart\" -> \"#expectedResult\"")
    def "url param: path and query parameter combos"() {

        when: "making the request"
        def mc = this.deproxy.makeRequest(url: "${urlbase}${pathPart}${queryPart}")

        then:
        mc.sentRequest.path == expectedResult
        mc.handlings.size() == 1
        mc.handlings[0].request.path == expectedResult

        where:
        pathPart | queryPart     | expectedResult
        ""       | ""            | "/"
        "/"      | ""            | "/"
        "/path"  | ""            | "/path"
        "/path/" | ""            | "/path/"
        ""       | "?"           | "/?"
        "/"      | "?"           | "/?"
        "/path"  | "?"           | "/path?"
        "/path/" | "?"           | "/path/?"
        ""       | "?name=value" | "/?name=value"
        "/"      | "?name=value" | "/?name=value"
        "/path"  | "?name=value" | "/path?name=value"
        "/path/" | "?name=value" | "/path/?name=value"
    }

    def "host param: should override the host in url param"() {

        given:
        String hostValueAtConnector = null
        def captureHostParam = {

            request, https, host, port, params ->

            // by overriding the client connector, we can record what host value it received and skip  an actual TCP connection

            hostValueAtConnector = host
            return new Response(200)

        } as ClientConnector

        def hostname = "example.com"

        when: "send a request with an explicit path param"
        def mc = this.deproxy.makeRequest(url: "${urlbase}/urlpath",
                host: hostname,
                clientConnector: captureHostParam)

        then: "the host in the sent request (and in the Host header) should be that of the host param"
        hostValueAtConnector == hostname
    }

    def "host param: should allow invalid characters"() {

        given:
        String hostValueAtConnector = null
        def captureHostParam = {

            request, https, host, port, params ->

            // by overriding the client connector, we can record what host value it received and skip  an actual TCP connection

            hostValueAtConnector = host
            return new Response(200)

        } as ClientConnector

        def hostname = "!@#\$"

        when: "send a request with an explicit path param"
        def mc = this.deproxy.makeRequest(url: "${urlbase}/urlpath",
                host: hostname,
                clientConnector: captureHostParam)

        then: "the host in the sent request (and in the Host header) should be that of the host param"
        hostValueAtConnector == hostname
    }

    def "port param: should override the port in url param"() {

        given:
        int portValueAtConnector = null
        def capturePortParam = {

            request, https, host, port, params ->

                // by overriding the client connector, we can record what port value it received and skip an actual TCP connection

                portValueAtConnector = port
                return new Response(200)

        } as ClientConnector

        int expectedPort = 12345

        when: "send a request with an explicit path param"
        def mc = this.deproxy.makeRequest(url: "http://localhost:8080/urlpath",
                port: expectedPort,
                clientConnector:  capturePortParam)

        then: "the host in the sent request (and in the Host header) should be that of the host param"
        portValueAtConnector == expectedPort
    }

    def "path param: should override the path in 'url'"() {

        when: "send a request with an explicit path param"
        def mc = this.deproxy.makeRequest(url: "${urlbase}/urlpath", path: "/parampath")

        then: "the path in the sent request should be that of the path param"
        mc.sentRequest.path == "/parampath"
    }

    def "path param: should allow otherwise invalid characters"() {

        when: "send a request with an explicit path param with invalid characters"
        def mc = this.deproxy.makeRequest(url: "${urlbase}/urlpath", path: "/parampath?query=value@%")

        then: "the path in the sent request should be that of the path param"
        mc.sentRequest.path == "/parampath?query=value@%"
    }

    def cleanup() {
        if (this.deproxy) {
            this.deproxy.shutdown();
        }
    }
}
