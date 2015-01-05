package com.rackspace.deproxy.examples

import com.rackspace.deproxy.BodyWriter
import com.rackspace.deproxy.ClientConnector
import com.rackspace.deproxy.HeaderWriter
import com.rackspace.deproxy.Request
import com.rackspace.deproxy.RequestParams
import com.rackspace.deproxy.Response

import java.util.concurrent.CountDownLatch

class DisconnectConnector implements ClientConnector {

    CountDownLatch latch = new CountDownLatch(1)

    @Override
    Response sendRequest(Request request, boolean https, host, port,
                         RequestParams params) {

        """Send the given request to the host,
            then wait for a few seconds and cut the connection."""

        def hostIP = InetAddress.getByName(host)


        // open the connection
        // (ignore https for now)
        Socket s = new Socket(host, port)

        def outStream = s.getOutputStream();
        def writer = new PrintWriter(outStream, true);

        // send the request
        def requestLine = String.format("%s %s HTTP/1.1",
                                        request.method, request.path ?: "/")
        writer.write(requestLine);
        writer.write("\r\n");

        writer.flush();

        HeaderWriter.writeHeaders(outStream, request.headers)

        writer.flush();
        outStream.flush();

        BodyWriter.writeBody(request.body, outStream,
                params.usedChunkedTransferEncoding)


        // wait for the handler to signal
        latch.await()

        // prematurely close the connection
        s.close()

        // wait long enough for the endpoint to
        // attach the server-side response
        sleep 3000

        return null
    }

    def handler(request) {

        sleep 2000

        // tell the connector to proceed
        latch.countDown()

        sleep 2000

        return new Response(200)
    }
}
