==============
 How It Works
==============

Request/Response
----------------

GDeproxy is intended to test complex HTTP proxies, rather than just clients or
servers. For this reason, it deals with more than just HTTP requests and
responses. An entire complex system of HTTP components can be involved, and
GDeproxy keeps track of them all.

We'll start by describing the simplest possible arrangement: a client makes a
request to a server.::

  ________                            ________
 |        |  --->  1. Request  --->  |        |
 | Client |                          | Server |
 |________|  <---  2. Response <---  |________|

In this instance, all we have to keep track of is the request sent and the
response received. Simple enough, right? So simple, in fact, that we wouldn't
even really need GDeproxy to test it. To test a server, all we would need to
do is use an HTTP client to send a request to the server, then compare what
the server got back with what we expected.

Handlings
---------

Next, let's consider a situation with more moving parts.::

  ________                            ________                           ________
 |        |  --->  1. Request  --->  |        |  ---> 2. Request  --->  |        |
 | Client |                          | Proxy  |                         | Server |
 |________|  <---  4. Response <---  |________|  <--- 3. Response <---  |________|

Now things are getting interesting.

1. The client sends a request to the proxy
2. The proxy potentially modifies the request and sends it along to the server
3. The server returns a response to the proxy
4. The proxy potentially modifies the responses and sends it bck to the client

If our goal is to test the behavior of the proxy and the modifications it
makes to requests, responses, or both, then we have to keep track of more
information. Not only that, we need to distinguish between two
Request/Response exchanges. We can create a DeproxyEndpoint to represent the
server, and make requests to the proxy using the Deproxy.makeRequest method.
When the endpoint receives a request from the proxy, it will return a
response. We say that it "handles" the request. Both the request the endpoint
receives and the response it sends back are collected into something called a
Handling. A handling represents the request/response pair at the server side
of the equation. So, the call to makeRequest should return:

- the sent request
- the received request and sent response, as a Handling
- the received response

But to have a more complete model, we should consider additional cases.


Here's another situation in which Handlings prove useful::

  ________                            ________
 |        |  --->  1. Request  --->  |        |
 | Client |                          | Proxy  |
 |________|  <---  2. Response <---  |________|


Suppose a proxy is limiting requests to the server to X per minute. Or serving
responses out of a cache. Or something like that. In these cases, there are
circumstances in which we expect the proxy to *not* forward the request to the
server, but instead to serve a response itself, whether an error or a cached
response.

As it turns out, we can test whether or not the proxy is forwarding requests,
in addition to checking that the response is correct. If the mock-server
endpoint never receives a request, then it never generates a response and no
Handling object is generated.


Message Chains
--------------

But what if we want to track more than a single Handling?
Consider another situation.
Suppose the proxy that we're testing is used to authenticate client requests *before* forwarding them on to the server.
And suppose further that this authentication has to go through an auxiliary, shared service that manages authentication for a number of different servers and services.
When a client sends a request, the proxy takes the credentials, and asks the authentication service whether the credentials are correct or not.
If correct, the proxy will forward the request to the server; if not, the proxy will return an error code to the client.
::

  ________                            ________                           ________
 |        |  --->  1. Request  --->  |        |  ---> 4. Request  --->  |        |
 | Client |                          | Proxy  |                         | Server |
 |________|  <---  6. Response <---  |________|  <--- 5. Response <---  |________|

                                       |    ^
                            2. Request |    |  3. Response
                                       v    |
                                      ________
                                     |  Aux.  |
                                     |Service |
                                     |________|


Now we have to keep track of more than one request coming from the proxy, and more than one handling.
Moreover, the proxy might have to make multiple requests to the auxiliary service. Or there could be multiple auxiliary servers that the proxy must coordinate with, each doing something different.
In order to test the proxy's behavior in all of these situations, we need to keep track of a lot more stuff.
Ultimately, what we need is a comprehensive record of everything that happens as a result of the original request the client made.
We call that a MessageChain.
Everything from the client to the proxy to the auxiliary services to the end server and back again is stored in a single, easy-to-assert object.

We can simulate the auxiliary service using a second DeproxyEndpoint in addition to the first.
That endpoint can be made to return canned responses to the proxy's authentication requests.
All handlings from both endpoints will be stored in a single MessageChain object, which makeRequest returns back to its caller.

Orphaned Handlings
------------------

In order to
GDeproxy keeps track of separate MessageChains as a result of separate calls to makeRequest.
This is even the case when making simultaneous calls on different threads.

::


  ________
 |        |  --->  1. Request  -------------.
 | Client |                                 |
 |________|  <---  7. Response <-------.    |
                                       |    |
                                       |    |
                                       |    |
                                       |    v
  ________                            ________                           ________
 |        |  --->  2. Request  --->  |        |  ---> 3. Request  --->  |        |
 | Client |                          | Proxy  |  ---> 4. Request  --->  | Server |
 |________|  <---  8. Response <---  |________|  <--- 5. Response <---  |________|
                                                 <--- 6. Response <---


In such a situation, there needs to be a way to distinguish which requests are associated with which MessageChains when they reach the server.
Depending on the timing, the second request made might reach the server first.
In order to keep track, makeRequest adds a special header (Deproxy-Request-ID) with a unique identifier to each outgoing request, and associates it with the MessageChain for that request.
Typically, a proxy won't remove such a header from the request unless configured to do so, so this a reasonable safe way to keep track.




Connections
-----------

This effectively comprises a chain of messages, aka a MessageChain.



Request and Response - the two basic building block of an HTTP transaction.

Request - Method, path, headers, body
Response - Code, message, headers, body

Handling - a pair of Request and Response,
a reference to the endpoint that handled the request, and the identifier of the connection used by the endpoint.

Message chain
the initial request made from the client side
the final response received at the client side
the defaultHandler to use
the mapping of per-endpoint handlers to use
a collection of all handlings associated with the initial request
a collection of all orphaned handlings served by all endpoints while the request was active

