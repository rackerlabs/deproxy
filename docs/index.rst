.. gdeproxy documentation master file, created by
   sphinx-quickstart on Fri Aug 30 16:27:24 2013.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

========
GDeproxy
========

GDeproxy (or "gDeproxy" or "gdeproxy") is a tool for performing functional/regression testing of proxies, and other HTTP intermediaries. It is written in Groovy (ported from python). It is meant to be incorporated into unit tests for functional testing.

Testing normal client/server interaction is relatively straight-forward: Use a specialized test client to send requests to the server, and compare the response that the server returns to what it ought to return.::

  ________                     ________
 |  Test  |  --->  req  --->  |        |
 | Client |                   | Server |
 |________|  <---  resp <---  |________|


Proxies sit in-between an HTTP client (e.g. novaclient) and an HTTP server (e.g. the Nova API nodes). This makes testing a little more difficult.::

  ________                     ________                    ________
 |        |  --->  req  --->  |        |  ---> req2 --->  |        |
 | Client |                   | Proxy  |                  | Server |
 |________|  <---  resp2 <--  |________|  <--- resp <---  |________|

A proxy can modify either the incoming request to the server, or the outgoing response to the client, or both. In addition, it may handle the request itself (e.g. in the case of caching or authenication), and prevent it from reaching the server in the first place.
The functionality and positioning of the proxy provides more of a challenge to functionality testing.
The traditional model is not enough.
Because a test client only sees one side of the transaction, it can't make definitive determinations about the server's side of it. ::

  ________                     ________                    ________
 |  Test  |  --->  req  --->  |        |  ---> ???? --->  |        |
 | Client |                   | Proxy  |                  | Server |
 |________|  <---  resp2 <--  |________|  <--- ???? <---  |________|

If we don't have a copy of the request that the server received, then we can't compare it to the request sent, which means we don't know for sure that the proxy is modifying it correctly.
Likewise, if we don't have a copy of the response that the server originally sent, we can't conclusively prove that the proxy is modifying responses correctly.
[Some specific cases don't have this problem, such as whether the proxy overwrites the "Server" header on a response; that can be confirmed because a response will only ever have one "Server" header, and that can easily be checked by a test client.]
But in the general case, we can't say for sure about other functional requirements.
Additionally, if the proxy is required to prevent a request from even reaching the server (as in the case of invalid authentication credentials in the request) a test client cannot determine whether or not any such request was in fact forwarded, because all it sees is the error response from the proxy.
For that, we'd need to be able see both sides of the exchange, and record all requests that made it to the server.
And that is what a deproxy does. It de-proxifies the proxy::

  ________                     ________                    ________
 |        |  --->  req  --->  |        |  ---> req2 --->  |        |
 |   (C)  |                   | Proxy  |                  |   (S)  |
 |        |  <---  resp2 <--  |________|  <--- resp <---  |        |
 |        |                                               |        |
 |        |_______________________________________________|        |
 |                                                                 |
 |                             Deproxy                             |
 |_________________________________________________________________|

A deproxy acts as both the client and the server, and the proxy it is testing will forward requests from one side to the other.
Any requests received by the server side are matched up with the requests that started them.
A call to a deproxy's makeRequest method will return the request that the client side sent, the request that the server side received, the response that the server side sent, and the response that the client side received. In this way, we can conclusively prove whether or not the proxy modified requests and responses correctly. We can even conclusively show when no request makes it to the server in the first place, because the receivedRequest and sentResponse fields will be null.

But this is just scratching the surface. The `org.rackspace.gdeproxy` module contains additional tools and utilities for custom server responses, mocking, testing multiple endpoints, and more.


Contents:

.. toctree::
   :maxdepth: 2

   use
   handlers
   howItWorks


Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`

