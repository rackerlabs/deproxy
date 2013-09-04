==========
 Handlers
==========

Handlers are the things that turn requests into responses. A given call to
makeRequest can take a ``handler`` argument that will be called for each
request that reaches an endpoint. Deproxy includes a number of built-in
handlers for some of the most common use cases. Also, you can define your own
handlers.
::

    def deproxy = new Deproxy()
    def e = deproxy.addEndpoint(9999)
    def mc = deproxy.makeRequest('http://localhost:9999/')
    println mc.receivedResponse.headers
    // [
    //  Server: gdeproxy 0.16-SNAPSHOT,
    //  Date: Wed, 04 Sep 2013 16:20:56 GMT,
    //  Content-Length: 0,
    //  Deproxy-Request-ID: 60e2a2bd-a179-4b50-a8c4-8d5b73d0218a
    // ]

    mc = deproxy.makeRequest(url: 'http://localhost:9999/',
            defaultHandler: Handlers.&echoHandler)
    println mc.receivedResponse.headers
    // [
    //  Deproxy-Request-ID: 6021d10a-f252-4816-9eb6-104b0aaf91f1,
    //  Host: localhost,
    //  Accept: */*,
    //  Accept-Encoding: identity,
    //  User-Agent: gdeproxy 0.16-SNAPSHOT,
    //  Server: gdeproxy 0.16-SNAPSHOT,
    //  Date: Wed, 04 Sep 2013 16:20:56 GMT,
    //  Content-Length: 0
    // ]


Specifying Handlers
===================

Handlers can be specified in multiple ways, depending on your needs.

- Passing a handler as the ``defaultHandler`` parameter when creating a
  ``Deproxy`` object will set the handler to be used for every request serviced
  by any endpoint on that object. This covers every request coming in, whether
  it is originally initiated by some call to ``makeRequest`` (simply called a
  'handling') or by some other client (called an 'orphaned handling' because it
  isn't tied to any single message chain).::

    def echoServer = new Deproxy(Handlers.&echoHandler)
    println echoServer.defaultHandler
    // org.codehaus.groovy.runtime.MethodClosure@1278dc4c

- Passing a handler as the ``defaultHandler`` parameter to ``addEndpoint``
  will set the handler to be used for every request that the created endpoint
  receives, whether normal or orphaned.::

    def deproxy = new Deproxy()
    println deproxy.defaultHandler
    // null

    def echoEndpoint = deproxy.addEndpoint(9998, 'echo-endpoint', 'localhost',
            Handlers.&echoHandler)
    println echoEndpoint.defaultHandler
    // org.codehaus.groovy.runtime.MethodClosure@6ef2ea42

- Passing a handler as the ``defaultHandler`` parameter to ``makeRequest``
  will set the handler used for every request associated with the message
  chain, no matter which endpoint receives it. This does not affect orphaned
  requests from non-deproxy clients, or requests that lose their
  ``Deproxy-Request-ID`` header for some reason.::

    def mc = deproxy.makeRequest(url: 'http://localhost:9998/',
            defaultHandler: Handlers.&simpleHandler)

- Passing a ``dict`` or other mapping object as the ``handlers`` parameter to
  ``makeRequest`` will specify specific handlers to be used for specific
  endpoints for all requests received associated with the message chain. This
  does not affect orphaned requests. The mapping object must have endpoint
  objects (or their names) as keys, and the handlers as values.::

    def deproxy = new Deproxy()
    def endpoint1 = deproxy.addEndpoint(9997, 'endpoint-1')
    def endpoint2 = deproxy.addEndpoint(9996, 'endpoint-2')
    def endpoint3 = deproxy.addEndpoint(9995, 'endpoint-3')
    def mc = deproxy.makeRequest(url: 'http://localhost:9997/',
            handlers: [
                    endpoint1: customHandler1,
                    endpoint2: customHandler2,
                    'endpoint-3': customHandler3
            ])


Handler Resolution Procedure
----------------------------

Given the various ways to specify handlers, and the different needs for each,
there must be one way to unambiguously determine which handler to use for any
given request. When an endpoint receives and services a request, the process by
which a handler is chosen for it is defined so:

    1. If the incoming request is tied to a particular message chain by the
       presence of a ``Deproxy-Request-ID`` header, and the call to
       ``makeRequest`` includes a ``handlers`` parameters,

        a. if that ``handlers`` mapping object has the current servicing
           endpoint as a key, use the associated value as the handler.
        b. if the mapping object doesn't have the current servicing endpoint as
           a key, but does have the endpoint's *name* as a key, then use the
           associated value of the name as the handler.
        c. otherwise, continue below
    2. If the call to ``makeRequest`` didn't have a ``handlers`` argument or
       if the servicing endpoint was not found therein, but the call to
       ``makeRequest`` *did* include a ``defaultHandler`` argument, use that
       as the handler.
    3. If the incoming request cannot be tied to a particular message chain,
       but the servicing endpoint's ``defaultHandler`` attribute is not
       `None`, then use the value of that attribute as the handler.
    4. If the servicing endpoint's ``defaultHandler`` is None, but the parent
       ``Deproxy`` object's ``defaultHandler`` attribute is not `None`, then
       use that as the handler.
    5. Otherwise, use ``deproxy.simpleHandler`` as a last resort.


Built-in Handlers
=================

The following handlers are a part of the deproxy module. They can be used to
address a small number of potential use cases. They also demonstrate effective
ways to define additional handlers.

- simpleHandler
    The last-resort handler used if none is specified. It returns a response
    with a 200 status code, an empty response body, and only the basic Date,
    Server, and request id headers.::

        mc = deproxy.makeRequest(url: 'http://localhost:9994/',
                defaultHandler: Handlers.&simpleHandler)
        println mc.receivedResponse.headers
        // [
        //  Server: gdeproxy 0.16-SNAPSHOT,
        //  Date: Wed, 04 Sep 2013 16:45:44 GMT,
        //  Content-Length: 0,
        //  Deproxy-Request-ID: 398bbcf7-d342-4457-8e8e-0b7e8f8ca826
        // ]

- echoHandler
    Returns a response with a 200 status code, and copies the request body and
    request headers.::

        mc = deproxy.makeRequest(url: 'http://localhost:9994/',
                defaultHandler: Handlers.&echoHandler)
        println mc.receivedResponse.headers
        // [
        //  Deproxy-Request-ID: 5f488584-fbe2-4322-bab2-8e9c157e84be,
        //  Host: localhost,
        //  Accept: */*,
        //  Accept-Encoding: identity,
        //  User-Agent: gdeproxy 0.16-SNAPSHOT,
        //  Server: gdeproxy 0.16-SNAPSHOT,
        //  Date: Wed, 04 Sep 2013 16:45:44 GMT,
        //  Content-Length: 0
        // ]

- delay(timeout, nextHandler)
    This is actually a factory function that returns a handler. Give it a
    time-out in seconds and a second handler function, and it will return a
    handler that will wait the desired amount of time before calling the second
    handler.::

        mc = deproxy.makeRequest(url: 'http://localhost:9994/',
                defaultHandler: Handlers.Delay(3000))
        println mc.receivedResponse.headers
        // [
        //  Server: gdeproxy 0.16-SNAPSHOT,
        //  Date: Wed, 04 Sep 2013 16:45:47 GMT,
        //  Content-Length: 0,
        //  Deproxy-Request-ID: cb92db72-fb53-46c6-b143-d884af5f536d
        // ]

        mc = deproxy.makeRequest(url: 'http://localhost:9994/',
                defaultHandler: Handlers.Delay(3000, Handlers.&echoHandler))
        println mc.receivedResponse.headers
        // [
        //  Deproxy-Request-ID: 31eb3d8a-9eba-4fdc-80a5-03101b10aec5,
        //  Host: localhost,
        //  Accept: */*,
        //  Accept-Encoding: identity,
        //  User-Agent: gdeproxy 0.16-SNAPSHOT,
        //  Server: gdeproxy 0.16-SNAPSHOT,
        //  Date: Wed, 04 Sep 2013 16:45:50 GMT,
        //  Content-Length: 0
        // ]

- route(scheme, host, deproxy)
    *This handler is not currently implemented.*
    This is actually a factory function that returns a handler. The handler
    forwards all requests to the specified host via HTTP or HTTPS, as indicated
    by the scheme parameter. The deproxy parameter is a deproxy.Deproxy object,
    which is used only as an HTTP/S client. The response returned from the
    handler is the response returned from the specified host.

Custom Handlers
===============

You can define your own handlers and pass them as the ``handler`` parameter to
makeRequest. Any callable that accepts a single ``request`` parameter and
returns a ``Response`` object will do.
::


    def customHandler(request) {
        return new Response(606, 'Spoiler', null, 'Snape Kills Dumbledore')
    }

    // ...


    def mc = deproxy.makeRequest(url: "http://localhost:9999",
            defaultHandler: this.&customHandler)
    println mc.receivedResponse
    // Response(
    //  code=606,
    //  message=Spoiler,
    //  headers=[
    //      Server: gdeproxy 0.16-SNAPSHOT,
    //      Date: Wed, 04 Sep 2013 17:00:19 GMT,
    //      Content-Length: 22,
    //      Content-Type: text/plain,
    //      Deproxy-Request-ID: fe2f9d2d-ec03-4b7e-b0b2-19f35c5b6df8],
    //  body=Snape Kills Dumbledore
    // )


    mc = deproxy.makeRequest(url: "http://localhost:9999",
            defaultHandler: { request ->
                return new Response(
                        607,
                        "Something Else",
                        ['Custom-Header': 'Value'],
                        "Some other body")
            })
    println mc.receivedResponse
    // Response(
    //  code=607,
    //  message=Something Else,
    //  headers=[
    //      Custom-Header: Value,
    //      Server: gdeproxy 0.16-SNAPSHOT,
    //      Date: Wed, 04 Sep 2013 17:00:19 GMT,
    //      Content-Length: 15,
    //      Content-Type: text/plain,
    //      Deproxy-Request-ID: 8d46b115-d7ec-4505-b5ba-dc61c60a0518],
    //  body=Some other body
    // )

Default Response Headers
========================

By default, an endpoint will add the 'Server' and 'Date' headers on all
out-bound responses. This can be turned off in custom handlers by returning a
2-value tuple, with the first value being the `Response` object (as usual) and
the second value being `True` or `False` to indicate whether the default
response headers should or should not be added, respectively. This can be
useful for testing how a proxy responds to a misbehaving origin server.
::

    def customHandler = { request, context ->

        context.sendDefaultResponseHeaders = false

        return new Response(503, "Something went wrong", null,
                "Something went wrong in the server\n" +
                        "and it didn't return correct headers!'")
    }
    def mc = deproxy.makeRequest(url: 'http://localhost:9999/',
            defaultHandler: customHandler)
    println mc.receivedResponse
    // Response(
    //  code=503,
    //  message=Something went wrong,
    //  headers=[
    //      Deproxy-Request-ID: f3ee8e35-66c1-4b7f-a0be-1b64e94615e6],
    //  body=
    // )

Additionally, any response from a handler that has a response body will have
an additional ``Content-Length`` header added to it, giving the length of the
response body. If this is turned off, the client/proxy may not be able to
correctly read the response body.