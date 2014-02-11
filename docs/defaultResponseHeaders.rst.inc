
.. _defaultResponseHeaders :

Default Response Headers
========================

By default, an endpoint will add a number of headers on all out-bound
responses. This behavior can be turned off in custom handlers by setting the
HandlerContext's sendDefaultResponseHeaders field to false (it is true by
default). This can be useful for testing how a proxy responds to a
misbehaving origin server. Each of the following headers is added if it has
not already been explicitly added by the handler, and subject to certain
conditions (e.g., presence of a response body):

- Server
    The identifying information of the server software, "deproxy" followed
    by the version number.

- Date
    The date and time at which the response was returned by the handler, in
    RFC 1123 format.

- Content-Type
    If the response contains a body, then the endpoint will try to guess. If
    the body is of type ``String``, then it will add a Content-Type header with a
    value of ``text/plain``, If the body is of type ``byte[]``, it will use a value
    of ``application/octet-stream``. If the response does not contain a body,
    then this header will not be added.

- Transfer-Encoding
    If the response has a body, and the usedChunkedTransferEncoding_ field is
    true, this header will have a value of ``chunked``. If it has a body but
    usedChunkedTransferEncoding_ is false, the header will have a value of
    ``identity``. If there is no body, then this header will not be added.

.. _usedChunkedTransferEncoding: usedChunkedTransferEncoding_resp_

- Content-Length
    If the response has a body, and the usedChunkedTransferEncoding_ field is
    false, then this header will have a value equal to the decimal count of
    octets in the body. If the body is a ``String``, then the length is the number
    of bytes after encoding as ASCII. If the body is of type ``byte[]``, then the
    length is just the number of bytes in the array. If the response has a
    body, but usedChunkedTransferEncoding_ is true, then this field is not
    added. If the response does not have a body, then this header will be
    added with a value of ``0``.

- Deproxy-Request-ID
    If the response is associated with a message chain, then the ID of that
    message chain is assigned to this header and added to the response.

Note: If the response has a body, and sendDefaultResponseHeaders_ is set to
false, and the handler doesn't explicitly set the Transfer-Encoding header or
the Content-Length header, then the client/proxy may not be able to correctly
read the response body.
