package org.rackspace.deproxy

import spock.lang.Specification
import spock.lang.Unroll

class HeaderTest extends Specification {

    @Unroll("when we call CreateHostHeaderValue with #host, #port, and https=#https, we should get #expectedValue")
    def testHostHeader() {

        expect:
        Header.CreateHostHeaderValue(host, port, https) == expectedValue

        where:
        host          | port  | https | expectedValue
        "localhost"   | 80    | true  | "localhost:80"
        "localhost"   | 80    | false | "localhost"
        "localhost"   | 443   | true  | "localhost"
        "localhost"   | 443   | false | "localhost:443"
        "localhost"   | 12345 | true  | "localhost:12345"
        "localhost"   | 12345 | false | "localhost:12345"
        "example.com" | 80    | true  | "example.com:80"
        "example.com" | 80    | false | "example.com"
        "example.com" | 443   | true  | "example.com"
        "example.com" | 443   | false | "example.com:443"
        "example.com" | 12345 | true  | "example.com:12345"
        "example.com" | 12345 | false | "example.com:12345"
        "12.34.56.78" | 80    | true  | "12.34.56.78:80"
        "12.34.56.78" | 80    | false | "12.34.56.78"
        "12.34.56.78" | 443   | true  | "12.34.56.78"
        "12.34.56.78" | 443   | false | "12.34.56.78:443"
        "12.34.56.78" | 12345 | true  | "12.34.56.78:12345"
        "12.34.56.78" | 12345 | false | "12.34.56.78:12345"
    }

    @Unroll("when we call CreateHostHeaderValue with #host and #port, we should get #expectedValue")
    def testHostHeaderDefaultHttpsArgument() {

        expect:
        Header.CreateHostHeaderValue(host, port) == expectedValue

        where:
        host          | port  | expectedValue
        "localhost"   | 80    | "localhost"
        "localhost"   | 443   | "localhost:443"
        "localhost"   | 12345 | "localhost:12345"
        "example.com" | 80    | "example.com"
        "example.com" | 443   | "example.com:443"
        "example.com" | 12345 | "example.com:12345"
        "12.34.56.78" | 80    | "12.34.56.78"
        "12.34.56.78" | 443   | "12.34.56.78:443"
        "12.34.56.78" | 12345 | "12.34.56.78:12345"
    }
}
