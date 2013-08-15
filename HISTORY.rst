.. :changelog:

History
-------

0.15 (in progress)
++++++++++++++++++


0.14 (2013-08-14)
++++++++++++++++++

- Worked around the bug in groovy *again*, this time in
  DeproxyEndpointHandlerThread


0.13 (2013-08-14)
++++++++++++++++++

- Worked around a bug in groovy releated to Thread class
  (see http://jira.codehaus.org/browse/GROOVY-5475) in
  DeproxyEndpointListenerThread.


0.12 (2013-08-13)
++++++++++++++++++

- Associated a unique identifier with each TCP connection. Said identifier
  will be attached to each Handling that an endpoint handles.
- Fixed version numbers so that User-Agent and Server headers use the correct
  version of the software.


0.11 (2013-08-08)
++++++++++++++++++

- Fixed query parameters in Request path.
- Delay handler creator.
- Better inferring of binary/string data based on content-type.
- Content-Type is now a default added header on requests and responses with
  bodies.
- Made PortFinder faster by decreasing sleep time.


0.10 (2013-08-07)
+++++++++++++++++

- Made the PortFinder class public. Useful for dynamically getting port
  numbers in tests.
- Created the ClientConnector concept, which allows more fine-grained
  control over how connections are made and requests are sent.
- Added support for binary message bodies.

0.9 (2013-06-19)
++++++++++++++++

- Port to Groovy.

0.8 (2013-06-05)
++++++++++++++++

- Rough preliminary support for using ssl for requests.
- Add "Content-Length: 0" header if no body is present on the request.

0.7 (2013-05-31)
++++++++++++++++

- Changed the signature of ``add_endpoint`` so that the user can just pass a
  port, instead of an ugly ``('localhost', port)`` tuple, when creating an
  endpoint.
- Added the ability for the user to specify a default handler for a Deproxy via
  the ``default_handler`` parameter to ``__init__``.
- Added the ability for the user to specify a default handler for a specific
  endpoint via the ``default_handler`` parameter to ``add_endpoint``.
- Added the ability for the user to specify separate handlers for each endpoint
  via the ``handlers`` parameter to ``make_request``.
- Renamed the ``make_request`` method's ``handler_function`` parameter to
  ``default_handler``.
- Renamed the ``MessageChain`` class's ``handler`` to ``default_handler``, to
  avoid confusion with the new ``handlers`` attribute.
- Renamed the built-in ``default_handler`` function to ``simple_handler`` to
  avoid confusion.
- Renamed the ``delay`` function's ``handler_function`` parameter to
  ``next_handler`` and made it optional, defaulting to ``simple_handler``.
- Stringified parameters to ``Request`` and ``Response``. They are always
  converted to strings now. Also made some of them optional.
- Fixes to ``route``. It's still not 100%, though.

There are several breaking changes to method signatures. Upgrading existing
tests to use v0.7 will be tedious, but not difficult.

0.6 (2013-05-30)
++++++++++++++++

- Added a class, ``HeaderCollection``, to hold request and response headers
  instead of using a ``dict``. The new class is a hybrid of a dictionary and a
  list of tuples. It supports case-insensitive lookup and storing multiple
  values for headers with the same name.
- Updated reading of simple message bodies. It doesn't yet support chunked
  transfers or encodings, but it's far more useful.
- Re-organized the codebase from a multi-file package into a single module.

0.5 (2013-05-16)
++++++++++++++++

- Fixed the logic when adding default headers so that they are recorded in the
  handlings as well.

0.4 (2013-05-16)
++++++++++++++++

- Added option to ``make_request`` to not add default request headers
- Added option to handler functions to not add default response headers

