package org.rackspace.deproxy.examples

import org.rackspace.deproxy.Deproxy
import org.rackspace.deproxy.Handlers
import org.rackspace.deproxy.MessageChain
import org.rackspace.deproxy.Response
import spock.lang.Specification

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
class HandlerExample extends Specification {

    def handlers() {

        def deproxy = new Deproxy()
        def e = deproxy.addEndpoint(9999)
        def mc = deproxy.makeRequest('http://localhost:9999/')
        println mc.receivedResponse.headers
        // [
        //  Server: deproxy 0.16-SNAPSHOT,
        //  Date: Wed, 04 Sep 2013 16:20:56 GMT,
        //  Content-Length: 0,
        //  Deproxy-Request-ID: 60e2a2bd-a179-4b50-a8c4-8d5b73d0218a
        // ]


        mc = deproxy.makeRequest(url: 'http://localhost:9999/',
                defaultHandler: Handlers.&echoHandler)
        println mc.receivedResponse.headers
        // [
        //  Deproxy-Request-ID: 6021d10a-f252-4816-9eb6-104b0aaf91f1,
        //  Host: localhost,
        //  Accept: */*,
        //  Accept-Encoding: identity,
        //  User-Agent: deproxy 0.16-SNAPSHOT,
        //  Server: deproxy 0.16-SNAPSHOT,
        //  Date: Wed, 04 Sep 2013 16:20:56 GMT,
        //  Content-Length: 0
        // ]

        expect:
        1 == 1
    }

    def specifyingHandlers() {

        def echoServer = new Deproxy(Handlers.&echoHandler)
        println echoServer.defaultHandler
        // org.codehaus.groovy.runtime.MethodClosure@1278dc4c

        def deproxy = new Deproxy()
        println deproxy.defaultHandler
        // null
        def echoEndpoint = deproxy.addEndpoint(9998, 'echo-endpoint', 'localhost',
                Handlers.&echoHandler)
        println echoEndpoint.defaultHandler
        // org.codehaus.groovy.runtime.MethodClosure@6ef2ea42

        def mc = deproxy.makeRequest(url: 'http://localhost:9998/',
                defaultHandler: Handlers.&simpleHandler)

        expect:
        1 == 1
    }

    def specifyingHandlers2() {

        def customHandler1 = { request -> new Response(200) }
        def customHandler2 = { request -> new Response(302) }
        def customHandler3 = { request -> new Response(404) }


        def deproxy = new Deproxy()
        def endpoint1 = deproxy.addEndpoint(9997, 'endpoint-1')
        def endpoint2 = deproxy.addEndpoint(9996, 'endpoint-2')
        def endpoint3 = deproxy.addEndpoint(9995, 'endpoint-3')
        def mc = deproxy.makeRequest(url: 'http://localhost:9997/',
                handlers: [
                        endpoint1: customHandler1,
                        endpoint2: customHandler2,
                        'endpoint-3': customHandler3
                ])


        expect:
        1 == 1
    }

    def builtinHandlers() {

        Deproxy deproxy = new Deproxy()
        deproxy.addEndpoint(9994)
        MessageChain mc

        mc = deproxy.makeRequest(url: 'http://localhost:9994/',
                defaultHandler: Handlers.&simpleHandler)
        println mc.receivedResponse.headers
        // [
        //  Server: deproxy 0.16-SNAPSHOT,
        //  Date: Wed, 04 Sep 2013 16:45:44 GMT,
        //  Content-Length: 0,
        //  Deproxy-Request-ID: 398bbcf7-d342-4457-8e8e-0b7e8f8ca826
        // ]



        mc = deproxy.makeRequest(url: 'http://localhost:9994/',
                defaultHandler: Handlers.&echoHandler)
        println mc.receivedResponse.headers
        // [
        //  Deproxy-Request-ID: 5f488584-fbe2-4322-bab2-8e9c157e84be,
        //  Host: localhost,
        //  Accept: */*,
        //  Accept-Encoding: identity,
        //  User-Agent: deproxy 0.16-SNAPSHOT,
        //  Server: deproxy 0.16-SNAPSHOT,
        //  Date: Wed, 04 Sep 2013 16:45:44 GMT,
        //  Content-Length: 0
        // ]




        mc = deproxy.makeRequest(url: 'http://localhost:9994/',
                defaultHandler: Handlers.Delay(3000))
        println mc.receivedResponse.headers
        // [
        //  Server: deproxy 0.16-SNAPSHOT,
        //  Date: Wed, 04 Sep 2013 16:45:47 GMT,
        //  Content-Length: 0,
        //  Deproxy-Request-ID: cb92db72-fb53-46c6-b143-d884af5f536d
        // ]



        mc = deproxy.makeRequest(url: 'http://localhost:9994/',
                defaultHandler: Handlers.Delay(3000, Handlers.&echoHandler))
        println mc.receivedResponse.headers
        // [
        //  Deproxy-Request-ID: 31eb3d8a-9eba-4fdc-80a5-03101b10aec5,
        //  Host: localhost,
        //  Accept: */*,
        //  Accept-Encoding: identity,
        //  User-Agent: deproxy 0.16-SNAPSHOT,
        //  Server: deproxy 0.16-SNAPSHOT,
        //  Date: Wed, 04 Sep 2013 16:45:50 GMT,
        //  Content-Length: 0
        // ]



        mc = deproxy.makeRequest(url: 'http://localhost:9994/ip',
                defaultHandler: Handlers.Route("httpbin.org", 80))
        println mc.receivedResponse.headers
        // [
        //  Date: Thu, 12 Sep 2013 18:19:25 GMT,
        //  Server: gunicorn/0.17.4,
        //  X-Cache: MISS from [ ... ],
        //  Connection: Keep-Alive,
        //  Content-Type: application/json,
        //  Content-Length: 45,
        //  Access-Control-Allow-Origin: *,
        //  Deproxy-Request-ID: 6c5b0741-87dc-456b-ae2f-87201efcf6e3
        // ]




        expect:
        1 == 1
    }

    def customHandler(request) {
        return new Response(606, 'Spoiler', null, 'Snape Kills Dumbledore')
    }

    def customHandlers() {

        def deproxy = new Deproxy()
        deproxy.addEndpoint(9993)

        def mc = deproxy.makeRequest(url: "http://localhost:9993",
                defaultHandler: this.&customHandler)
        println mc.receivedResponse
        // Response(
        //  code=606,
        //  message=Spoiler,
        //  headers=[
        //      Server: deproxy 0.16-SNAPSHOT,
        //      Date: Wed, 04 Sep 2013 17:00:19 GMT,
        //      Content-Length: 22,
        //      Content-Type: text/plain,
        //      Deproxy-Request-ID: fe2f9d2d-ec03-4b7e-b0b2-19f35c5b6df8],
        //  body=Snape Kills Dumbledore
        // )


        mc = deproxy.makeRequest(url: "http://localhost:9993",
                defaultHandler: { request ->
                    return new Response(
                            607,
                            "Something Else",
                            ['Custom-Header': 'Value'],
                            "Some other body")
                })
        println mc.receivedResponse
        // Response(
        //  code=607,
        //  message=Something Else,
        //  headers=[
        //      Custom-Header: Value,
        //      Server: deproxy 0.16-SNAPSHOT,
        //      Date: Wed, 04 Sep 2013 17:00:19 GMT,
        //      Content-Length: 15,
        //      Content-Type: text/plain,
        //      Deproxy-Request-ID: 8d46b115-d7ec-4505-b5ba-dc61c60a0518],
        //  body=Some other body
        // )


        expect:
        1 == 1
    }

    def defaultResponseHeaders() {

        def deproxy = new Deproxy()
        deproxy.addEndpoint(9992)

        def customHandler = { request, context ->

            context.sendDefaultResponseHeaders = false

            return new Response(503, "Something went wrong", null,
                    "Something went wrong in the server\n" +
                            "and it didn't return correct headers!'")
        }

        def mc = deproxy.makeRequest(url: 'http://localhost:9992/',
                defaultHandler: customHandler)
        println mc.receivedResponse
        // Response(
        //  code=503,
        //  message=Something went wrong,
        //  headers=[
        //      Deproxy-Request-ID: f3ee8e35-66c1-4b7f-a0be-1b64e94615e6],
        //  body=
        // )



        expect:
        1 == 1
    }
}