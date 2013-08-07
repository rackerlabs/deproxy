package org.rackspace.gdeproxy

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
class PathTest extends Specification {

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

    @Unroll("Path and query parameter combos: \"#pathPart#queryPart\" -> \"#expectedResult\"")
    def "Path and query parameter combos"() {

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

    // TODO: Test for non-ascii characters, illegal characters, escaping, etc.

    def cleanup() {
        if (this.deproxy) {
            this.deproxy.shutdown();
        }
    }
}
