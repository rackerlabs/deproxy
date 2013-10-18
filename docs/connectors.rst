
.. include:: global.rst.inc

.. _ClientConnectors:

===================
 Client Connectors
===================

GDeproxy uses *client connectors* to provide fine-grained control over how a client will send a Request_ object to a destination and receive a Response_ object.
By default, makeRequest_ will use a DefaultClientConnector_\, which will simply open a socket, write the request to the socket, and read a response from the socket.
Also, it can optionally add some default headers to the request before sending.

Built-in Connectors
=====================

GDeproxy provides the following built-in client connectors:

- BareClientConnector_ - This connector opens a socket, sends the request, and the reads the response.
  It doesn't modify the request or response in any way. It won't even add a Host header.
  It also doesn't employ any clever tricks, like following 300-level redirection responses.
  If there is any failure to connect or error while transmitting, BareClientConnector_ will throw an exception.
  However, simple error codes like 501 and 404 will *not* trigger an exception.

- DefaultClientConnector_ - This connector inherits from BareClientConnector_\.
  If the ``sendDefaultRequestHeaders`` field in RequestParams_ is set to ``true``, then it will add some default headers to the request before calling BareClientConnector_\.\ sendRequest_\.

Both connector classes have an optional ``socket`` parameter that allows to use a previously-established connection when creating the connector.
If you specify it, the connectors will use this socket; otherwise, they will create a new socket for every request.
Specifying a specific socket to use can be useful for re-using connections.
The built-in connectors don't provide any connection pooling or re-use by themselves.

Specifying Connectors
=====================

A connector can be specified for use for a particular request by passing it as the ``clientConnector`` parameter to makeRequest_, like so:

::

    def deproxy = new Deproxy()

    def mc = deproxy.makeRequest(url: "http://example.org",
                clientConnector: new BareClientConnector())

    assert mc.handlings.size() == 0
    assert mc.receivedResponse.code == "400"    // Bad Request, due to missing Host header

    def mc = deproxy.makeRequest(url: "http://example.org",
                headers: ['Host': 'example.org'],
                clientConnector: new BareClientConnector())

    assert mc.handlings.size() == 0
    assert mc.receivedResponse.code == "200"

Of course, you don't have to use a new connector each time.
You can store a connector to a variable and use it for multiple requests.

Custom Connectors
=================

You can create a custom client connector by implementing the ClientConnector_ interface.

Suppose you want to test a proxy for more than just its handling of certain request information.
For example, how does it handle connection interruptions?
::

  ________                            ________                           ________
 |        |  --->  1. Request  --->  |        |  ---> 2. Request  --->  |        |
 | Client |                          | Proxy  |                         | Server |
 |________|                          |________|                 X <---  |________|


1. The client sends a request to the proxy
2. The proxy potentially modifies the request and sends it along to the server
3. Before the server can return a response, the client closes the connection to the proxy

What will the proxy do in this case? Throw an exception and log an error? Hang and catch fire?
In order to test how the proxy will behave in this situation, we can create a custom client connector that closes the socket before receiving a response.
We can couple that with a handler on the endpoint side that delays for a few seconds.

Here's some example code for the connector:
::

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

And here's the test that uses it:
::


    def deproxy = new Deproxy()
    def endpoint = deproxy.addEndpoint(9999)

    def theProxy = new TheProxy(port: 8080,
            targetHostname: "localhost",
            targetPort: 9999)

    def connector = new DisconnectConnector()

    def mc = deproxy.makeRequest(url: "http://localhost:8080/",
            headers: ['Host': 'localhost:8080'],
            clientConnector: connector,
            defaultHandler: connector.&handler)

    assert mc.handlings.size() == 1
    assert mc.handlings[0].response.code == "200"
    assert mc.receivedResponse == null

So what should happen is that the server returns a response to the proxy, but that response never makes it back to the client.
Therefore, there's a handling in the MessageChain_, but ``receivedResponse`` is null.

Default Request Headers
=======================

sendDefaultRequestHeaders_

usedChunkedTransferEncoding_req_

::

        if (params.sendDefaultRequestHeaders) {

            if (request.body) {

                if (params.usedChunkedTransferEncoding) {

                        request.headers.add("Transfer-Encoding", "chunked")
                            request.headers["Transfer-Encoding"] == "identity") {

                    def length
                    String contentType
                    if (request.body instanceof String) {
                        length = request.body.length()
                        contentType = "text/plain"
                    } else if (request.body instanceof byte[]) {
                        length = request.body.length
                        contentType = "application/octet-stream"
                    } else {
                        throw new UnsupportedOperationException("Unknown data type in requestBody")
                    }

                    if (length > 0) {
                        if (!request.headers.contains("Content-Length")) {
                            request.headers.add("Content-Length", length)
                        }
                        if (!request.headers.contains("Content-Type")) {
                            request.headers.add("Content-Type", contentType)
                        }
                    }
                }
            }

                request.headers.add("Host", Header.CreateHostHeaderValue(host, port, https))
            }
                request.headers.add("Accept", "*/*")
            }
                request.headers.add("Accept-Encoding", "identity")
            }
            if (!request.headers.contains("User-Agent")){
            }
        }




By default, an endpoint will add a number of headers on all out-bound
responses. This behavior can be turned off in custom handlers by setting the
HandlerContext's sendDefaultResponseHeaders field to false (it is true by
default). This can be useful for testing how a proxy responds to a
misbehaving origin server. Each of the following headers is added if it has
not already been explicitly added by the handler, and subject to certain
conditions (e.g., presence of a response body):

- Host

- User-Agent
    The identifying information of the server software, "gdeproxy" followed
    by the version number.

- Accept
    */*
    The date and time at which the response was returned by the handler, in
    RFC 1123 format.

- Accept-Encoding
    identity
    The date and time at which the response was returned by the handler, in
    RFC 1123 format.

- Content-Type
    If the request contains a body, then the connector will try to guess. If
    the body is of type ``String``, then it will add a Content-Type header with a
    value of "text/plain". If the body is of type ``byte[]``, it will use a value
    of "application/octet-stream". If the request does not contain a body,
    then this header will not be added.

- Transfer-Encoding
    If the request has a body, and `usedChunkedTransferEncoding <usedChunkedTransferEncoding_req_>` is
    true, this header will have a value of "chunked". If it has a body but
    `usedChunkedTransferEncoding <usedChunkedTransferEncoding_r_>` is false, the header will have a value of
    "identity". If there is no body, then this header will not be added.

- Content-Length
    If the request has a body, and the `usedChunkedTransferEncoding <usedChunkedTransferEncoding_req_>` is
    false, then this header will have a value equal to the decimal count of
    octets in the body. If the body is a ``String``, then the length is the number
    of bytes after encoding as ASCII. If the body is of type ``byte[]``, then the
    length is just the number of bytes in the array. If the request has a
    body, but `usedChunkedTransferEncoding <usedChunkedTransferEncoding_req_>` is true, then this field is not
    added. If the request does not have a body, then this header will be
    added with a value of "0".


Note: If the request has a body, and sendDefaultRequestHeaders_ is set to
false, and the handler doesn't explicitly set the Transfer-Encoding header or
the Content-Length header, then the client/proxy may not be able to correctly
read the request body.
