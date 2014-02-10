
.. include:: global.rst.inc

.. _ServerConnectors:

===================
 Server Connectors
===================

Deproxy uses *server connectors* to provide fine-grained control over how an endpoint receives a Request_ object and returns a Response_ object.
Connectors can specify how sockets are created, and bytes are transferred to/from that socket.
By default, an endpoint will will use a SocketServerConnector_\, which will serve requests over a socket.

::

  ________   request   _________   handleRequest   __________         ________
 | Socket | --------> |         | --------------> |          | ----> |        |
 | Servlet|           |Connector|                 | Endpoint |       | Handler|
 | etc    | <-------  |         | <-------------- |          |       |        |
 |________| response  |_________|     return      |__________| <---- |________|


Built-in Connectors
=====================

Deproxy provides the following built-in server connectors:

- SocketServerConnector_ - This connector acts as a stand-alone HTTP server. It creates a socket on the given port, starts a thread that listens on that socket for incoming connections, and spawns a new thread to handle each new incoming TCP connection.
  Requests are read off the wire and converted from octet sequences into Request_ objects. Likewise, Response_ objects are converted to octet sequences and sent back to the source of the request.
  Each spawned thread can handle multiple HTTP requests in succession. However, the requests aren't pipelined. That is, each request is handled and a response to it sent before the next request is read.
  This connector does not yet support HTTPS.

- ServletServerConnector_ - This connector extends `javax.servlet.http.HttpServlet <http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServlet.html>`__
  and can therefore be embedded into a servlet container, such as `Tomcat <http://tomcat.apache.org/>`__.
  It relies on the container to handle the management of sockets, threading, and translation of request and response from/to octet sequences.

Specifying Connectors
=====================

The Endpoint_ constructor accepts a ``connectorFactory`` parameter. This can be any method or closure that accepts an Endpoint_ as a parameter and returns an object that implements the ServerConnector_ interface.
If no ``connectorFactory`` is specified, the Endpoint_ will default to SocketServerConnector_.
If a ``connectoryFactory`` is specified, then that factory will be used to get the connector during Endpoint_ construction.
Additionally, if an argument is passed to ``connectorFactory``, then the ``port`` parameter of both addEndpoint_ and the Endpoint_ constructor will be ignored; the ``port`` argument is only passed to SocketserverConnector_ constructor.

::

    def deproxy = new Deproxy()

    def servletEndpoint = deproxy.addEndpoint(
            connectorFactory: ServletServerConnector.&Factory);

    ...

    Tomcat.addServlet(rootCtx, "deproxy-servlet",
                      servletEndpoint.serverConnector as Servlet);


Custom Connectors
=================

You can create a custom server connector by implementing the ServerConnector_ interface.

The ServerConnector_ interface only has a single ``shutdown`` method.
The Endpoint_ is passive and relies on the connector to initiate the handling process.
It is the responsibility of a custom connector to:

1. retrieve a Request_ object from some source,
2. pass the request to the Endpoint_ via the handleRequest_ method,
3. receive the Response_ back from handleRequest_, and
4. send the response back to the origin of the request.


Suppose you want to test a proxy for more than just its handling of certain request information.
For example, how does it handle connection interruptions?
::

  ________                            ________                           ________
 |        |  --->  1. Request  --->  |        |  ---> 2. Request  --->  |        |
 | Client |                          | Proxy  |                         | Server |
 |________|                          |________|                 X <---  |________|


1. The client sends a request to the proxy
2. The proxy forwards the request to the server
3. While the server is returning the response, it hangs. Not all of the octets of the response are sent back to the proxy.

What will the proxy do in this case? Throw an exception and log an error? Hang and catch fire?
In order to test how the proxy will behave in this situation, we can create a custom server connector that sleeps for an arbitrarily long time while sending data.

Here's some example code for the connector:
::

    class SlowResponseServerConnector extends SocketServerConnector {

        public SlowResponseServerConnector(Endpoint endpoint, int port) {
            super(endpoint, port)
        }

        @Override
        void sendResponse(OutputStream outStream, Response response,
                          HandlerContext context=null) {

            def writer = new PrintWriter(outStream, true);

            if (response.message == null) {
                response.message = ""
            }

            writer.write("HTTP/1.1 ${response.code} ${response.message}")
            writer.write("\r\n")

            writer.flush()

            // sleep for a really long time. don't return headers
            Thread.sleep(Long.MAX_VALUE)
        }
    }

And here's the test that uses it:
::

    def deproxy = new Deproxy()
    def endpoint = deproxy.addEndpoint(
            connectorFactory: { e ->
                new SlowResponseServerConnector(e, 9999)
            })

    def theProxy = new TheProxy(port: 8080,
            targetHostname: "localhost",
            targetPort: 9999)

    def mc = deproxy.makeRequest(url: "http://localhost:8080/")

    assert mc != null
    assert mc.handlings.size() == 1
    assert mc.handlings[0].response.code == "200"
    assert mc.receivedResponse.code == "502"

The server stops sending data halfway through sending the response.
The handler in effect is simpleHandler_\, so the response generated should be a 200.
However, because the full response never makes it back to the proxy, the proxy should eventually timeout and return a 502 Bad Gateway response to the client.


ServerConnector Lifecycle
-------------------------

When a Deproxy_ is shutdown, all of it's Endpoint_\s are shutdown as well.

- SocketServerConnector_ - The default connector.
    1. When the connector is created, it opens a socket on the designated port and spawns a thread to listen for connections to that socket.
    2. Whenever a new connection is made, the listener thread will spawn a new handler thread.
    3. The handler thread will proceed to service HTTP request, like so:
        a. First, the incoming request is read from the socket, and parsed into a Request_ object.
        b. Next, the connector will pass the Request_ to the endpoint by calling the handleRequest_ method.
        c. The endpoint will:
            i. Examine the request headers for a ``Deproxy-Request-ID`` header, and then try to match it to an existing MessageChain_ (created before in a call to makeRequest_).
            ii. Determine which handler to use (see :ref:`Handler Resolution Procedure <handlerResolutionProcedure>`), and pass the Request object to the handler to get a Response_ object.
            iii. If there is a MessageChain_ associated with the request, a Handling_ will be created and attached to the message chain. Otherwise, it will be attached to the orphanedHandlings list of all active message chains.
            iv. Return the response back to the connector.
        d. The connector then sends the response back to the sender.
        e. Finally, if the Request_ or handler indicated that the connection should be closed (by setting the ``Connection`` header to ``close``), then the handler thread will exit the loop and close the connection. Otherwise, it will return to step ``a.`` above.
    4. When shutdown_ is called on a parent Endpoint_ object, the connector will be shutdown. Its listener thread will stop listening, and no longer receive any new connections. Any long-running handler threads will continue to run until finished or the JVM terminates, whichever comes first.

.. _shutdown: shutdownDeproxy_

- ServletServerConnector_ - This connector expects to be loaded into a servlet container.Therefore, it neither creates threads nor opens sockets, and its shutdown method does nothing.

