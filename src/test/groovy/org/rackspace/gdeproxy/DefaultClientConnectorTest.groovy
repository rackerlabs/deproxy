package org.rackspace.gdeproxy

import groovy.util.logging.Log4j
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
@Log4j
class DefaultClientConnectorTest {

    String requestString = ("GET / HTTP/1.1\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n")

    String responseString = ("HTTP/1.1 200 OK\r\n" +
            "Server: some-closure\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n")

}
