package org.rackspace.deproxy

import spock.lang.Specification
import spock.lang.Unroll

class HostHeaderTest extends Specification {

    def testBasicUsage() {

        when:
        def hh = new HostHeader("localhost", 8080)

        then:
        hh.host == "localhost"
        hh.port == 8080
        hh.name == "Host"
        hh.value == "localhost:8080"
    }

    @Unroll("when we call CreateHostHeaderValue with #host, #port, and https=#https, we should get #expectedValue")
    def testHostHeader() {

        expect:
        HostHeader.CreateHostHeaderValue(host, port, https) == expectedValue

        where:
        host          | port  | https | expectedValue
        "localhost"   | 80    | true  | "localhost:80"
        "localhost"   | 80    | false | "localhost:80"
        "localhost"   | 80    | null  | "localhost:80"
        "localhost"   | 443   | true  | "localhost:443"
        "localhost"   | 443   | false | "localhost:443"
        "localhost"   | 443   | null  | "localhost:443"
        "localhost"   | 12345 | true  | "localhost:12345"
        "localhost"   | 12345 | false | "localhost:12345"
        "localhost"   | 12345 | null  | "localhost:12345"
        "localhost"   | null  | true  | "localhost:443"
        "localhost"   | null  | false | "localhost:80"
        "localhost"   | null  | null  | "localhost"
        "example.com" | 80    | true  | "example.com:80"
        "example.com" | 80    | false | "example.com:80"
        "example.com" | 80    | null  | "example.com:80"
        "example.com" | 443   | true  | "example.com:443"
        "example.com" | 443   | false | "example.com:443"
        "example.com" | 443   | null  | "example.com:443"
        "example.com" | 12345 | true  | "example.com:12345"
        "example.com" | 12345 | false | "example.com:12345"
        "example.com" | 12345 | null  | "example.com:12345"
        "example.com" | null  | true  | "example.com:443"
        "example.com" | null  | false | "example.com:80"
        "example.com" | null  | null  | "example.com"
        "12.34.56.78" | 80    | true  | "12.34.56.78:80"
        "12.34.56.78" | 80    | false | "12.34.56.78:80"
        "12.34.56.78" | 80    | null  | "12.34.56.78:80"
        "12.34.56.78" | 443   | true  | "12.34.56.78:443"
        "12.34.56.78" | 443   | false | "12.34.56.78:443"
        "12.34.56.78" | 443   | null  | "12.34.56.78:443"
        "12.34.56.78" | 12345 | true  | "12.34.56.78:12345"
        "12.34.56.78" | 12345 | false | "12.34.56.78:12345"
        "12.34.56.78" | 12345 | null  | "12.34.56.78:12345"
        "12.34.56.78" | null  | true  | "12.34.56.78:443"
        "12.34.56.78" | null  | false | "12.34.56.78:80"
        "12.34.56.78" | null  | null  | "12.34.56.78"
    }

    @Unroll("when we call CreateHostHeaderValue with #host and #port, we should get #expectedValue (e.g., https is not specified)")
    def testHostHeaderDefaultHttpsArgument() {

        expect:
        HostHeader.CreateHostHeaderValue(host, port) == expectedValue

        where:
        host          | port  | expectedValue
        "localhost"   | 80    | "localhost:80"
        "localhost"   | 443   | "localhost:443"
        "localhost"   | 12345 | "localhost:12345"
        "localhost"   | null  | "localhost"
        "example.com" | 80    | "example.com:80"
        "example.com" | 443   | "example.com:443"
        "example.com" | 12345 | "example.com:12345"
        "example.com" | null  | "example.com"
        "12.34.56.78" | 80    | "12.34.56.78:80"
        "12.34.56.78" | 443   | "12.34.56.78:443"
        "12.34.56.78" | 12345 | "12.34.56.78:12345"
        "12.34.56.78" | null  | "12.34.56.78"
    }

    @Unroll("when we call CreateHostHeaderValue with #host, we should get #expectedValue (e.g., neither port nor https is specified)")
    def testHostHeaderDefaultPortAndHttpsArguments() {

        expect:
        HostHeader.CreateHostHeaderValue(host) == expectedValue

        where:
        host          | expectedValue
        "localhost"   | "localhost"
        "example.com" | "example.com"
        "12.34.56.78" | "12.34.56.78"
    }

    @Unroll("when we create a HostHeader with #host and #port, we should get #expectedValue")
    def testHostHeaderConstructorDefaultHttpsArgument() {

        when:
        HostHeader hh = new HostHeader(host, port)

        then:
        hh.name == "Host"
        hh.value == expectedValue

        where:
        host          | port  | expectedValue
        "localhost"   | 80    | "localhost:80"
        "localhost"   | 443   | "localhost:443"
        "localhost"   | 12345 | "localhost:12345"
        "localhost"   | null  | "localhost"
        "example.com" | 80    | "example.com:80"
        "example.com" | 443   | "example.com:443"
        "example.com" | 12345 | "example.com:12345"
        "example.com" | null  | "example.com"
        "12.34.56.78" | 80    | "12.34.56.78:80"
        "12.34.56.78" | 443   | "12.34.56.78:443"
        "12.34.56.78" | 12345 | "12.34.56.78:12345"
        "12.34.56.78" | null  | "12.34.56.78"
    }

    @Unroll("Calling fromString with #value should give #host and #port")
    def testHostHeaderFromString() {

        when:
        HostHeader hh = HostHeader.fromString(value)

        then:
        hh.name == "Host"
        hh.host == host
        hh.port == port

        where:
        value                       | host                  | port
        "localhost"                 | "localhost"           | null
        "localhost:"                | "localhost"           | null
        "localhost:443"             | "localhost"           | 443
        "localhost:12345"           | "localhost"           | 12345
        "example.com"               | "example.com"         | null
        "example.com:"              | "example.com"         | null
        "example.com:443"           | "example.com"         | 443
        "example.com:12345"         | "example.com"         | 12345
        "12.34.56.78"               | "12.34.56.78"         | null
        "12.34.56.78:"              | "12.34.56.78"         | null
        "12.34.56.78:443"           | "12.34.56.78"         | 443
        "12.34.56.78:12345"         | "12.34.56.78"         | 12345
        "12345.com"                 | "12345.com"           | null
        "12345.com:"                | "12345.com"           | null
        "12345.com:443"             | "12345.com"           | 443
        "12345.com:12345"           | "12345.com"           | 12345
        "example.com."              | "example.com."        | null
        "example.com.:"             | "example.com."        | null
        "example.com.:443"          | "example.com."        | 443
        "example.com.:12345"        | "example.com."        | 12345
        " example.com:12345"        | "example.com"         | 12345
        "example.com :12345"        | "example.com"         | 12345
        " example.com :12345"       | "example.com"         | 12345
        "example.com: 12345"        | "example.com"         | 12345
        "example.com:12345 "        | "example.com"         | 12345
        "example.com: 12345 "       | "example.com"         | 12345
        "hyp-hen.example.com"       | "hyp-hen.example.com" | null
        "hyp-hen.example.com:"      | "hyp-hen.example.com" | null
        "hyp-hen.example.com:443"   | "hyp-hen.example.com" | 443
        "hyp-hen.example.com:12345" | "hyp-hen.example.com" | 12345
    }

    @Unroll("Calling fromString with #value should throw an exception")
    def testHostHeaderFromStringErrors() {

        when:
        HostHeader hh = HostHeader.fromString(value)

        then:
        thrown(IllegalArgumentException)

        where:
        value                | _
        "12.34.56.78."       | _
        "12.34.56.78.:"      | _
        "12.34.56.78.:443"   | _
        "12.34.56.78.:12345" | _
        "example.com:asdf"   | _
        "example.com:123 45" | _
        "example.com:.123"   | _
        "exam ple.com:123"   | _
        "exam\$ple.com:123"  | _
        "12.34.56.78.90"     | _
        "12.34.56"           | _

    }
}
