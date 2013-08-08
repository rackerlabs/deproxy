package org.rackspace.gdeproxy

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */

class Handlers {

    // Handler function.
    // Returns a 200 OK Response, with no additional headers or response body.
    static def simpleHandler(request) {
        return new Response(200, 'OK')
    }

    // Handler function.
    // Returns a 200 OK Response, with the same headers and body as the request.
    static def echoHandler(request) {
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
}


//def route(scheme, host, deproxy):
//    """
//Factory function.
//Returns a handler that forwards the request to a specified URL, using
//either HTTP or HTTPS (regardless of what protocol was used in the initial
//request), and returning the response from the host so routed to.
//"""
//    logger.debug('')
//
//    def route_to_host(request):
//        logger.debug('scheme, host = %s, %s' % (scheme, host))
//        logger.debug('request = %s %s' % (request.method, request.path))
//
//        request2 = Request(request.method, request.path, request.headers,
//                           request.body)
//
//        if 'Host' in request2.headers:
//            request2.headers.delete_all('Host')
//        request2.headers.add('Host', host)
//
//        logger.debug('sending request')
//        response = deproxy.send_request(scheme, host, request2)
//        logger.debug('received response')
//
//        return response, False
//
//    route_to_host.__doc__ = "Route responses to %s using %s" % (host, scheme)
//
//    return route_to_host

