package org.rackspace.deproxy

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
class makeRequestParamsTest extends Specification {

    static PortFinder pf = new PortFinder()

    Deproxy deproxy;
    int port;
    String urlbase;

    def setup() {

        this.deproxy = new Deproxy();
        this.port = pf.getNextOpenPort();
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

    // TODO: Test for non-ascii characters, illegal characters, escaping, etc.

    def cleanup() {
        if (this.deproxy) {
            this.deproxy.shutdown();
        }
    }
}
