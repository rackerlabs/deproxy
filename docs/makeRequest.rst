
.. include:: global.rst.inc

.. _makeRequestPage:

=================
 Making Requests
=================

The makeRequest_ method is the primary means of sending requests to HTTP applications.
It prepares a Request_ object to be sent, constructs a MessageChain_ object to track the request and specify custom handlers, and passes the Request_ object to the ClientConnector_.

Parameters
==========

::

    public MessageChain makeRequest(
        String url,
        String host="",
        port=null,
        String method="GET",
        String path="",
        headers=null,
        requestBody="",
        defaultHandler=null,
        Map handlers=null,
        boolean addDefaultHeaders=true,
        boolean chunked=false,
        ClientConnector clientConnector=null) { ... }

- ``url`` - The URL of the request to be made. This will be broken up into scheme, host, port, and path (and query parameter) components. The host, and port will be passed to the client connector to be used to make the connection, and the path will form part of the Request_ object. Parts of this can be overriden by other parameters. This parameter gets passed to `java.net.URI <http://docs.oracle.com/javase/7/docs/api/java/net/URI.html>`_, so it must be a valid uri, with no bad characters. If you need to send invalid data in the request for testing purposes, use the ``host`` and ``path`` parameters.
- ``host`` - The host to which the request will be sent. If both ``host`` and ``url`` are given, ``host`` will override the host component of ``url``.
- ``port`` - The port to which the request will be sent. If both ``port`` and ``url`` are given, ``port`` will override any host component of ``url``.
- ``method`` - The HTTP method of the Request_ object. This is typically ``GET``, ``POST``, ``PUT``, or some other method defined in `RFC 2616 § 5.1.1 <http://tools.ietf.org/html/rfc2616#section-5.1.1>`_ and `RFC 2616 § 9 <http://tools.ietf.org/html/rfc2616#section-9>`_ . However, deproxy will allow any string, to test custom extension methods and invalid methods. The default is ``GET``.
- ``path`` - The path of the Request_ object. If both ``path`` and ``url`` are given, ``path`` will override the path component of ``url``.
- ``headers`` - The headers of the Request_ object. This parameter can be a map, with key-value pairs corresponding to "Key: Value" headers in arbitrary order, or a HeaderCollection_ with preserved order.
- ``requestBody`` - The body of the Request_ object.
- ``defaultHandler`` - A handler to service the request. Any endpoint that receives this request (or, more accurately, a request with the same Deproxy-Request-ID header) will use ``defaultHandler`` instead of it's own default. This is a good way to customize per-request handling of a few requests while still relying on the endpoints default handler to cover most other requests. See :ref:`handlerResolutionProcedure`, step 2.
- ``handlers`` - A map of endpoints (or endpoint names) to handlers. If an endpoint or its name is a key in the map, and that endpoint receives this request (or request with the same Deproxy-Request-ID header), then that endpoint will use the value associated with the endpoint to handle the request, instead of relying on the endpoint's own default handler. See :ref:`handlerResolutionProcedure`, step 1.
- ``addDefaultHeaders`` - A boolean value that instructs the client connector to add default request headers to the request before sending. Custom connectors are not required to honor this parameter. See :ref:`defaultRequestHeaders`. The default is ``true``.
- ``chunked`` - A boolean value that instructs the client connecto to send the request body using the ``chunked`` transfer encoding. If ``addDefaultHeaders`` is true, the DefaultClientConnector_ will also add the appropriate ``Transfer-Encoding`` header. The default is ``false``.
- ``clientConnector`` - A custom ClientConnector_. If not given, whatever was specified as the ``defaultClientConnector`` parameter to the Deproxy_ constructor will be used.

Note that there are multiple ways to specify some information.
For example, if no value is given for the ``path`` parameter, then it will be taken from the path component of ``url``.
But if both ``path`` and ``url`` are given, then ``path`` will override ``url``.
The same goes for ``host`` and ``port``.

Named Parameters
================

makeRequest_ has a special override to handle named parameters.
The following are equivalent:
::

    deproxy.makeRequest("http://example.com/resource?name=value", null, null, "GET")

    deproxy.makeRequest(url: "http://example.com/resource?name=value", method: "GET")

    deproxy.makeRequest(method: "GET", url: "http://example.com/resource?name=value")