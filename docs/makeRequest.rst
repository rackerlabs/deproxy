
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
        String method="GET",
        String path="",
        headers=null,
        requestBody="",
        defaultHandler=null,
        Map handlers=null,
        boolean addDefaultHeaders=true,
        boolean chunked=false,
        ClientConnector clientConnector=null) { ... }

There are multiple ways to specify some information.
For example, if no value is given for the ``path`` parameter, then it will be taken from the path component of ``url``.

Named Parameters
================

makeRequest_ has a special override to handle named parameters.
The following are equivalent:
::
    deproxy.makeRequest("http://example.com/resource?name=value", "GET")

    deproxy.makeRequest(url: "http://example.com/resource?name=value", method: "GET")

    deproxy.makeRequest(method: "GET", url: "http://example.com/resource?name=value")