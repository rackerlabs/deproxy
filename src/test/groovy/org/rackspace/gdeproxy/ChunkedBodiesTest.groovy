
package org.rackspace.gdeproxy

import groovy.util.logging.Log4j;
import org.junit.*

import static org.junit.Assert.*;

/**
 *
 * @author izrik
 */
@Log4j
public class ChunkedBodiesTest {
	
    Deproxy deproxy;
    Socket client
    Socket server

    @Test
    void testChunkedRequestBodyInBareClientConnector() {

        String body = """ This is another body

This is the next paragraph.
"""

        String length = Integer.toHexString(body.length());

        String requestString = ("GET / HTTP/1.1\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "\r\n" +
                "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n") // end of chunked body, no trailer

        String responseString = ("HTTP/1.1 200 OK\r\n" +
                "Server: StaticTcpServer\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n")

        (client, server) = LocalSocketPair.createLocalSocketPair()
        client.soTimeout = 5000
        server.soTimeout = 5000

        String serverSideRequest

        def t = Thread.startDaemon("static-tcp-server") {
            serverSideRequest = StaticTcpServer.handleOneRequest(server, responseString,
                    requestString.length())
        }

        Request request = new Request("GET", "/",
                ["Transfer-Encoding": "chunked"], body)
        RequestParams params = new RequestParams()
        params.usedChunkedTransferEncoding = true

        BareClientConnector clientConnector = new BareClientConnector(client)

        Response response = clientConnector.sendRequest(request, false,
                "localhost", server.localPort, params)



        assertEquals(requestString, serverSideRequest)
        assertEquals("200", response.code)
        assertEquals("OK", response.message)
        assertEquals(2, response.headers.size())
        assertTrue(response.headers.contains("Server"))
        assertEquals("StaticTcpServer", response.headers["Server"])
        assertTrue(response.headers.contains("Content-Length"))
        assertEquals("0", response.headers["Content-Length"])
        assertEquals("", response.body)
    }

    @Test
    void testChunkedRequestBodyInDefaultClientConnector() {

        String body = """ This is another body

This is the next paragraph.
"""

        String length = Integer.toHexString(body.length());

        String requestString = ("GET / HTTP/1.1\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "Host: localhost\r\n" +
                "Accept: */*\r\n" +
                "Accept-Encoding: identity\r\n" +
                "User-Agent: ${Deproxy.VERSION_STRING}\r\n" +
                "\r\n" +
                "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n") // end of chunked body, no trailer

        String responseString = ("HTTP/1.1 200 OK\r\n" +
                "Server: StaticTcpServer\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n")

        (client, server) = LocalSocketPair.createLocalSocketPair()
        client.soTimeout = 5000
        server.soTimeout = 5000

        String serverSideRequest

        def t = Thread.startDaemon("static-tcp-server") {
            serverSideRequest = StaticTcpServer.handleOneRequest(server, responseString,
                    requestString.length())
        }

        Request request = new Request("GET", "/", [:], body)
        RequestParams params = new RequestParams()
        params.usedChunkedTransferEncoding = true

        DefaultClientConnector clientConnector = new DefaultClientConnector(client)

        Response response = clientConnector.sendRequest(request, false,
                "localhost", server.localPort, params)



        assertEquals(requestString, serverSideRequest)
        assertEquals("200", response.code)
        assertEquals("OK", response.message)
        assertEquals(2, response.headers.size())
        assertTrue(response.headers.contains("Server"))
        assertEquals("StaticTcpServer", response.headers["Server"])
        assertTrue(response.headers.contains("Content-Length"))
        assertEquals("0", response.headers["Content-Length"])
        assertEquals("", response.body)
    }

    @Test
    void testChunkedRequestBodyInDeproxyEndpoint() {

        // setup - create canned request; setup deproxy and endpoint

        deproxy = new Deproxy();
        PortFinder pf = new PortFinder();
        int port = pf.getNextOpenPort();
        String url = "http://localhost:${port}/";
        DeproxyEndpoint endpoint = deproxy.addEndpoint(port);

        String body = """ This is another body\n\r\nThis is the next paragraph.\n"""

        String length = Integer.toHexString(body.length());
        String requestString = ("GET / HTTP/1.1\r\n" +
                "Host: localhost:${port}\r\n" +
                "Content-Type: text/plain\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "Accept: */*\r\n" +
                "Accept-Encoding: identity\r\n" +
                "User-Agent: Canned-String\r\n" +
                "\r\n" +
                "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n") // end of chunked body, no trailer

        String responseString = ("HTTP/1.1 200 OK\r\n" +
                "Server: StaticTcpServer\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n")



        client = endpoint.createRawConnection()
        client.soTimeout = 3000

        MessageChain mc
        def t = Thread.startDaemon("request-on-the-side-to-get-orphan") {
            mc = deproxy.makeRequest(url: url, defaultHandler: Handlers.Delay(2000),
                                     addDefaultHeaders: true)
        }

        client.outputStream.write(requestString.getBytes("US-ASCII"))


        t.join()


        assertEquals(1, mc.orphanedHandlings.size())
        assertEquals(body, mc.orphanedHandlings[0].request.body)

    }

    @Test
    void testChunkedResponseBodyInBareClientConnector() {

        String body = """ This is another body\n\r\nThis is the next paragraph.\n"""

        String length = Integer.toHexString(body.length());

        String responseString = ("HTTP/1.1 200 OK\r\n" +
                "Server: StaticTcpServer\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "\r\n" +
                "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n"  // end of chunked body, no trailer
            )

        (client, server) = LocalSocketPair.createLocalSocketPair()
        client.soTimeout = 5000
        server.soTimeout = 5000

        String serverSideRequest

        def t = Thread.startDaemon("static-tcp-server") {
            serverSideRequest = StaticTcpServer.handleOneRequest(server,
                    responseString, 1)
        }

        Request request = new Request("GET", "/",
                ["Transfer-Encoding": "chunked"], body)

        BareClientConnector clientConnector = new BareClientConnector(client)



        Response response = clientConnector.sendRequest(request, false,
                "localhost", server.localPort, new RequestParams())



        assertEquals("200", response.code)
        assertEquals("OK", response.message)
        assertEquals(2, response.headers.size())
        assertTrue(response.headers.contains("Server"))
        assertEquals("StaticTcpServer", response.headers["Server"])
        assertTrue(response.headers.contains("Transfer-Encoding"))
        assertEquals("chunked", response.headers["Transfer-Encoding"])
        assertEquals(body, response.body)
    }

    @Test
    void testChunkedResponseBodyInDefaultClientConnector() {

        String body = """ This is another body\n\r\nThis is the next paragraph.\n"""

        String length = Integer.toHexString(body.length());

        String responseString = ("HTTP/1.1 200 OK\r\n" +
                "Server: StaticTcpServer\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "\r\n" +
                "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n"  // end of chunked body, no trailer
        )

        (client, server) = LocalSocketPair.createLocalSocketPair()
        client.soTimeout = 5000
        server.soTimeout = 5000

        String serverSideRequest

        def t = Thread.startDaemon("static-tcp-server") {
            serverSideRequest = StaticTcpServer.handleOneRequest(server,
                    responseString, 1)
        }

        Request request = new Request("GET", "/",
                ["Transfer-Encoding": "chunked"], body)

        DefaultClientConnector clientConnector = new DefaultClientConnector(client)



        Response response = clientConnector.sendRequest(request, false,
                "localhost", server.localPort, new RequestParams())



        assertEquals("200", response.code)
        assertEquals("OK", response.message)
        assertEquals(2, response.headers.size())
        assertTrue(response.headers.contains("Server"))
        assertEquals("StaticTcpServer", response.headers["Server"])
        assertTrue(response.headers.contains("Transfer-Encoding"))
        assertEquals("chunked", response.headers["Transfer-Encoding"])
        assertEquals(body, response.body)
    }

    @Test
    void testChunkedResponseBodyInDeproxyEndpoint() {

        // create canned request & response

        String body = """ This is another body\n\r\nThis is the next paragraph.\n"""

        String length = Integer.toHexString(body.length());
        String requestString = ("GET / HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: 0\r\n" +
                "Accept: */*\r\n" +
                "User-Agent: Canned-String\r\n" +
                "\r\n")

        String responseString = ("HTTP/1.1 200 OK\r\n" +
                "Server: ${Deproxy.VERSION_STRING}\r\n" +
                "Date: EEE, dd MMM yyyy HH:mm:ss zzz\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "\r\n" +
                "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n" // end of chunked body, no trailer
            )
        byte[] responseBytes = responseString.getBytes("US-ASCII")

        // setup deproxy and endpoint

        deproxy = new Deproxy();
        PortFinder pf = new PortFinder();
        int port = pf.getNextOpenPort();
        String url = "http://localhost:${port}/";
        def handler = { request, HandlerContext context ->
            context.usedChunkedTransferEncoding = true
            return new Response(200, "OK", null, body)
        }
        DeproxyEndpoint endpoint = deproxy.addEndpoint(port, null, null, handler);

        // create raw connection

        client = endpoint.createRawConnection()
        client.soTimeout = 3000

        // send the data to the endpoint

        client.outputStream.write(requestString.getBytes("US-ASCII"))

        // read the response

        byte[] bytesRecieved = new byte[responseBytes.length]
        try {

            int n = 0;
            while (n < bytesRecieved.length) {
                def count = client.inputStream.read(bytesRecieved, n, bytesRecieved.length - n)
                n += count
            }

        } catch (SocketTimeoutException ignored) {

        }
        String stringRecieved = new String(bytesRecieved, "US-ASCII")




        // compare the expected and actual responses. They are only partially
        // identical, though

        StringReader expected = new StringReader(responseString)
        StringReader actual = new StringReader(stringRecieved)

        // response lines should be the same
        String expectedLine = LineReader.readLine(expected)
        String actualLine = LineReader.readLine(actual)
        assertEquals(expectedLine, actualLine)

        // server headers have potentially variable length
        expectedLine = LineReader.readLine(expected)
        actualLine = LineReader.readLine(actual)
        assertTrue(expectedLine.startsWith("Server: "))
        assertTrue(actualLine.startsWith("Server: "))

        // date headers will be different in content, but equal in length
        expectedLine = LineReader.readLine(expected)
        actualLine = LineReader.readLine(actual)
        assertTrue(expectedLine.startsWith("Date: "))
        assertTrue(actualLine.startsWith("Date: "))
        assertEquals(expectedLine.length(), actualLine.length())

        // the rest should be identical
        expectedLine = LineReader.readLine(expected)
        actualLine = LineReader.readLine(actual)
        while (expectedLine != null && actualLine != null) {
            assertEquals(expectedLine, actualLine)
            expectedLine = LineReader.readLine(expected)
            actualLine = LineReader.readLine(actual)
        }
    }

    @Test
    void testChunkedBodyInBodyWriter1() {

        String body = """ This is another body\n\r\nThis is the next paragraph.\n"""
        String length = Integer.toHexString(body.length());
        byte[] chunkedBody = (
                "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n" // end of chunked body, no trailer
            ).getBytes("US-ASCII")
        ByteArrayOutputStream outStream = new ByteArrayOutputStream()


        BodyWriter.writeBodyChunked(body, outStream)
        byte[] bytesWritten = outStream.toByteArray()


        assertArrayEquals(chunkedBody, bytesWritten)
    }

    @Test
    void testChunkedBodyInBodyWriter2() {

        String body = """ This is another body\n\r\nThis is the next paragraph.\n"""
        String length = Integer.toHexString(body.length());
        byte[] chunkedBody = (
        "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n" // end of chunked body, no trailer
        ).getBytes("US-ASCII")
        ByteArrayOutputStream outStream = new ByteArrayOutputStream()


        BodyWriter.writeBody(body, outStream, true)
        byte[] bytesWritten = outStream.toByteArray()


        assertArrayEquals(chunkedBody, bytesWritten)
    }

    @Test
    void testChunkedBodyInBodyReader1() {

        String body = """ This is another body\n\r\nThis is the next paragraph.\n"""
        String length = Integer.toHexString(body.length());
        byte[] chunkedBody = (
                "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n" // end of chunked body, no trailer
            ).getBytes("US-ASCII")
        ByteArrayInputStream inStream = new ByteArrayInputStream(chunkedBody)


        byte[] bytesRead = BodyReader.readChunkedBody(inStream)


        assertArrayEquals(body.getBytes("US-ASCII"), bytesRead)
    }

    @Test
    void testChunkedBodyInBodyReader2() {

        String body = """ This is another body\n\r\nThis is the next paragraph.\n"""
        String length = Integer.toHexString(body.length());
        byte[] chunkedBody = (
        "${length}\r\n" + // chunk-size, with no chunk-extension
                "${body}\r\n" + // chunk-data
                "0\r\n" + // last-chunk, with no chunk-extension
                "\r\n" // end of chunked body, no trailer
        ).getBytes("US-ASCII")
        ByteArrayInputStream inStream = new ByteArrayInputStream(chunkedBody)
        HeaderCollection headers = new HeaderCollection(['Transfer-Encoding': 'chunked'])


        byte[] bytesRead = BodyReader.readBody(inStream, headers)


        assertArrayEquals(body.getBytes("US-ASCII"), bytesRead)
    }

    @After
    void tearDown() {
        if (this.deproxy) {
            this.deproxy.shutdown();
        }

        if (client) {
            client.close()
            client = null
        }

        if (server) {
            server.close()
            server = null
        }
    }

}

