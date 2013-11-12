
==================
 Standard Headers
==================

Deproxy provides a number of classes that correspond to the standard headers defined in RFC 2616.
These classes provide a useful object model to construct headers that conform to the rfc, preventing accidental mistakes like leaving an extra space or period in a Host header.

- HostHeader - Defines a `Host header <http://tools.ietf.org/html/rfc2616#section-14.23>`_. Takes a string for the hostname and an optional integer port.
