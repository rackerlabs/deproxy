package org.rackspace.deproxy

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */

class Handlers {

    // Handler function.
    // Returns a 200 OK Response, with no additional headers or response body.
    static Response simpleHandler(Request request) {
        return new Response(200, 'OK')
    }

    // Handler function.
    // Returns a 200 OK Response, with the same headers and body as the request.
    static Response echoHandler(Request request) {
        return new Response(200, 'OK', request.headers, request.body)
    }

    // Handler creator
    // Returns a closure (handler) that waits for the given amount of time
    // before forawrding the request to another handler.
    // Note: timeout is in milliseconds
    static def Delay(int timeout, nextHandler=Handlers.&simpleHandler) {
        return { Request request ->
            Thread.sleep(timeout);
            return nextHandler(request);
        }
    }

    static def Route(String host, int port, boolean https=false, ClientConnector connector=null) {
        if (connector == null) {
            connector = new BareClientConnector()
        }

        return { Request request ->

            Request request2 = new Request(
                    request.method,
                    request.path,
                    new HeaderCollection(request.headers),
                    request.body
            )

            if (request2.headers.contains("Host")) {
                request2.headers.deleteAll("Host")
            }

            request2.headers.add("Host", "${host}:${port}")

            RequestParams params = new RequestParams()
            Response response = connector.sendRequest(request2, https, host, port, params)

            return response
        }
    }
}

