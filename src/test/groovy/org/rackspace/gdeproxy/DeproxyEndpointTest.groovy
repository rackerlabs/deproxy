package org.rackspace.gdeproxy

import org.linkedin.util.clock.SystemClock
import spock.lang.Specification

import static org.linkedin.groovy.util.concurrent.GroovyConcurrentUtils.waitForCondition

class DeproxyEndpointTest extends Specification {

    def DeproxyEndpoint deproxyEndpoint


    def setup() {
        deproxyEndpoint = new DeproxyEndpoint(Mock(Deproxy), 8888, "foo", "localhost")
    }

    def cleanup() {
        deproxyEndpoint.shutdown()
    }

    def "when initialized, should start listening on socket"() {
        expect:
        deproxyEndpoint.isListening() == true
    }


    def "when initialized with no handler, should respond with simple handler"() {
        given:
        def client = new Socket("localhost", 8888)

        when:
        def buffer
        client.withStreams { input, output ->
            output << "echo testing...\n"
            buffer = input.newReader().readLine()
            println "response = $buffer"
        }

        then:
        buffer == "echo testing..."
    }

    def "when shutting down, should stop the server thread"() {
        when:
        deproxyEndpoint.shutdown()
        def clock = new SystemClock()
        waitForCondition(clock, '5s', '1s', {
            !deproxyEndpoint.isListening()
        })
        then:
        deproxyEndpoint.isListening() == false
    }

}
