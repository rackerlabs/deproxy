package org.rackspace.deproxy

import groovy.util.logging.Log4j
import org.junit.Test

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

@Log4j
class BareClientConnectorTest {

    String requestString = ("GET / HTTP/1.1\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n")

    String responseString = ("HTTP/1.1 200 OK\r\n" +
            "Server: some-closure\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n")

    @Test
    void testConstructorWithSocketParameter() {

        def (Socket client, Socket server) = LocalSocketPair.createLocalSocketPair()

        client.soTimeout = 30000 // in milliseconds
        server.soTimeout = 30000

        String serverSideRequest;

        def t = Thread.startDaemon("response") {

            // TODO: replace this with StaticTcpServer
            byte[] bytes = new byte[requestString.length()]
            int n = 0;
            while (n < bytes.length) {
                def count = server.inputStream.read(bytes, n, bytes.length - n)
                n += count
            }
            ByteBuffer bb = ByteBuffer.wrap(bytes)
            CharBuffer cb = Charset.forName("US-ASCII").decode(bb)
            serverSideRequest = cb.toString()


            bytes = new byte[responseString.length()]
            Charset.forName("US-ASCII").encode(responseString).get(bytes)
            server.outputStream.write(bytes)
            server.outputStream.flush()
        }

        BareClientConnector clientConnector = new BareClientConnector(client)
        Request request = new Request("GET", "/", ['Content-Length': '0'])
        RequestParams params = new RequestParams()
        params.sendDefaultRequestHeaders = false

        Response response = clientConnector.sendRequest(request, false, "localhost", server.port, params)



        assertEquals(requestString, serverSideRequest)
        assertEquals("200", response.code)
        assertEquals("OK", response.message)
        assertEquals(2, response.headers.size())
        assertTrue(response.headers.contains("Server"))
        assertEquals("some-closure", response.headers["Server"])
        assertTrue(response.headers.contains("Content-Length"))
        assertEquals("0", response.headers["Content-Length"])
        assertEquals("", response.body)

        client.close()
        server.close()
    }
}
