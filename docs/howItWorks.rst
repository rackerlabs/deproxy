
.. include:: global.rst.inc

==============
 How It Works
==============

Deproxy is intended to test complex HTTP proxies, rather than just clients or
servers. For this reason, it deals with more than just HTTP requests and
responses. An entire complex system of HTTP components can be involved, and
Deproxy keeps track of them all.
What follows is a description of various scenarios and how to test them using Deproxy.

Deproxy
-------

At the heart of the system is the Deproxy_ class. It acts as both the client and server on opposite sides of a proxy to be tested: ::

  ________                     ________                    ________
 |        |  --->  req  --->  |        |  ---> req2 --->  |        |
 |   (C)  |                   | Proxy  |                  |   (S)  |
 |        |  <---  resp2 <--  |________|  <--- resp <---  |        |
 |        |                                               |        |
 |        |_______________________________________________|        |
 |                                                                 |
 |                             Deproxy                             |
 |_________________________________________________________________|

You can use this in your test code by creating an instance of the Deproxy_ class, potentially adding one or more endpoints (about which, more :ref:`here <Endpoints>`), and then calling the makeRequest_ method to initiate an HTTP request from the client side to the proxy.
makeRequest_ allows you to craft a custom request, including the HTTP method, headers, and request body to send. You can also indicate how the server-side should respond and what additional complex behavior the client-side should exhibit (e.g. re-using connections).


Request/Response
----------------

We'll start by describing the simplest possible arrangement: a client makes a
request to a server.::

  ________                            ________
 |        |  --->  1. Request  --->  |        |
 | Client |                          | Server |
 |________|  <---  2. Response <---  |________|


In this instance, all we have to keep track of is the request sent and the
response received. To test a server, all we would need to
do is use an HTTP client to send a request to the server, then compare what
the server got back with what we expected.
Simple enough, right?
So simple, in fact, that we wouldn't even really need Deproxy to test it.

Nevertheless, Deproxy contains the facilities to do so.
Simply, create a Deproxy_ and then call it's makeRequest_ method, specifying the URL of the server.
It will create a Request_ object, and send it over the wire.
Then, it will receive a response from the server, and convert it into a Response_ object.
Each of these classes stores basic information about HTTP messages;

 - Request_ stores a ``method`` and a ``path``, both as strings.
 - Response_ stores a ``code`` and a ``message``, both as strings.
 - Both classes store a collection of headers. This collection stores headers as name/value pairs, in the order that they travel across the wire. You can also do by-name lookup, which is case-insensitive.
 - Both classes store an optional message body. The message body will be either a string or an array of ``byte``\s, depending on whether Deproxy could figure out what kind of data it is.

The Request_ sent and the Response_ recieved will be returned back to you from makeRequest_, along with a bunch of other stuff.
Then you can make assertions on the request and response as in any unit test.
That's client/server testing in a nutshell.

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
Request/Response exchanges. We can create a DeproxyEndpoint_ to represent the
server, and make requests to the proxy using the makeRequest_ method.
When the endpoint receives a request from the proxy, it will return a
response. We say that it "handles" the request. Both the request the endpoint
receives and the response it sends back are collected into something called a
Handling_. A handling represents the request/response pair at the server side
of the equation. So, the call to makeRequest should return:

- the sent request
- the received request and sent response, as a Handling
- the received response

But to have a more complete model, we should consider additional cases.


Here's another situation in which the Handling concept proves helpful::

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
We call that a MessageChain_.
Everything from the client to the proxy to the auxiliary services to the end server and back again is stored in a single, easy-to-assert object.

We can simulate the auxiliary service using a second DeproxyEndpoint_ in addition to the first.
That endpoint can be made to return canned responses to the proxy's authentication requests.
All handlings from both endpoints will be stored in a single MessageChain_ object, which makeRequest_ returns back to its caller.

Orphaned Handlings
------------------

In order to
Deproxy keeps track of separate MessageChain_\s as a result of separate calls to makeRequest_.
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


In such a situation, there needs to be a way to distinguish which requests are associated with which MessageChain_\s when they reach the server.
Depending on the timing, the second request made might reach the server first.
In order to keep track, makeRequest_ adds a special tracking header (Deproxy-Request-ID) with a unique identifier to each outgoing request, and associates it with the MessageChain_ for that request.
Typically, a proxy won't remove such a header from the request unless configured to do so, so this a reasonably safe way to keep track.
When the request reaches the endpoint, the tracking header value is used to get the associated MessageChain_ for the originating call to makeRequest_, and a Handling_ object is added to the list.
::

  ________
 |        |  --->  1. Request  -------------.
 | Client |                                 |
 |________|  <---  11.Response <-------.    |
                                       |    |
                                       |    |
                                       |    |
                                       |    v
  ________                            ________                           ________
 |        |  --->  2. Request  --->  |        |  ---> 7. Request  --->  |        |
 | Client |                          | Proxy  |  ---> 8. Request  --->  | Server |
 |________|  <---  12.Response <---  |________|  <--- 9. Response <---  |________|
                                                 <--- 10.Response <---
                                       ||  ^^
                            3. Request ||  ||  5. Response
                            4. Request vv  ||  6. Response
                                      ________
                                     |  Aux.  |
                                     |Service |
                                     |________|


A problem arises, however, in cases where a request reaches an endpoint without the tracking header.
This could happen a number of ways:

 - The proxy might be configured to remove all but a certain predetermined white-list of headers
 - The proxy might be initiating a new request to an auxiliary service, which wouldn't retain the tracking header
 - A completely unrelated request might have reached the endpoint from another source

Whatever the cause, it represents a problem for us, because it's not possible to tie the Handling to a particular MessageChain_ without the tracking header.
We call this an *orphaned* handling, and store it in the MessageChain_'s orphanedHandlings_ field.
Instead, what the endpoint will do is add the Handling_ to *all* active MessageChain_\s as an orphaned handling.
::

  ________
 |        |  --->  1. Request  -------------.
 | Client |                                 |     Deproxy-Request-ID present
 |________|  <---  11.Response <-------.    |              |
                                       |    |              |      Will create one
                                       |    |              |      handling per MC
                                       |    |              |                |
                                       |    v              v                v
  ________                            ________                           ________
 |        |  --->  2. Request  --->  |        |  ---> 7. Request  --->  |        |
 | Client |                          | Proxy  |  ---> 8. Request  --->  | Server |
 |________|  <---  12.Response <---  |________|  <--- 9. Response <---  |________|
                                                 <--- 10.Response <---
                                       ||  ^^
  No Deproxy-Request-ID --> 3. Request ||  ||  5. Response
  No Deproxy-Request-ID --> 4. Request vv  ||  6. Response
                                      ________
                                     |  Aux.  |
                                     |Service |  <-- Will result in four orphaned
                                     |________|        Handlings total, one
                                                       per request per MC


 Each client Request will result in a MessageChain with:
   The initial client Request to the proxy
   Two orphaned Handlings, one for each originating client Request
   One normal Handling for the Request that makes it to the server
   The final Response that the client receives from the proxy



Connections
-----------

HTTP applications typically have support for persistent connections, which allow for multiple HTTP transactions using the same TCP connection.
In Deproxy, when an endpoint receives a new connection, the connection is given a unique id. All Handling_ objects created by that endpoint from that TCP connection are tagged with the connection's id value.
If we want to test whether or not the proxy is using connection pooling, for example, we could simply make two identical calls to makeRequest_. Assuming that the requests are forwarded by the proxy to the server and is re-using connections, the MessageChain_\s that we get back will each have a single Handling_ object and both Handling_ objects will have the same ``connection`` value. If the proxy is not re-using connections, then the two Handling_ objects will have different ``connection`` values.
::

  ________                           ________                           ________
 |        |  ---> 1. Request  --->  |        |  ---> 2. Request  --->  |        |
 | Client |  <--- 4. Response <---  | Proxy  |  <--- 3. Response <---  | Server |
 |________|  ---> 5. Request  --->  |________|  ---> 6. Request  --->  |________|
             <--- 8. Response <---              <--- 7. Response <---

 # 2, 3, 6, and 7 use the same TCP connection
 Each MessageChain should have one Handling, and both Handlings
   should have the same value for the connection field.



