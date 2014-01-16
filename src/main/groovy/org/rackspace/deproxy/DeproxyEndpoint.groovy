package org.rackspace.deproxy

import groovy.util.logging.Log4j
import org.apache.log4j.Logger
import org.linkedin.util.clock.SystemClock

import java.text.SimpleDateFormat

import static org.linkedin.groovy.util.concurrent.GroovyConcurrentUtils.waitForCondition

/**
 * A class that acts as a mock HTTP server.
 */

@Log4j
class DeproxyEndpoint {

    String name;
    int port;
    String hostname;
    def defaultHandler;

    protected Deproxy deproxy;
    protected SocketServerConnector serverConnector;

    public DeproxyEndpoint(Deproxy deproxy, Integer port=null,
                           String name="Endpoint-${System.currentTimeMillis()}",
                           String hostname="localhost", def defaultHandler=null) {
        //        """
        //Initialize a DeproxyEndpoint
        //
        //Params:
        //deproxy - The parent Deproxy object that contains this endpoint
        //port - The port on which this endpoint will listen
        //name - A descriptive name for this endpoint
        //hostname - The ``hostname`` portion of the address tuple passed to
        //``socket.bind``. If not specified, it defaults to 'localhost'
        //default_handler - An optional handler function to use for requests that
        //this endpoint services, if not specified elsewhere
        //"""
        //
        if (hostname == null) {
            hostname = "localhost"
        }

        this.deproxy = deproxy
        this.name = name
        this.port = port
        this.hostname = hostname
        this.defaultHandler = defaultHandler
        serverConnector = new SocketServerConnector(this, port, name)
    }

    void shutdown() {
        log.debug("Shutting down ${this.name}")
        serverConnector.shutdown()
        log.debug("Finished shutting down ${this.name}")
    }

    ResponseWithContext handleRequest(Request request, String connectionName) {

        log.debug "Begin handleRequest"

        try {

            MessageChain messageChain = null

            def requestId = request.headers.getFirstValue(Deproxy.REQUEST_ID_HEADER_NAME)
            if (requestId) {

                log.debug "the request has a request id: ${Deproxy.REQUEST_ID_HEADER_NAME}=${requestId}"

                messageChain = this.deproxy.getMessageChain(requestId)

            } else {

                log.debug "the request does not have a request id"
            }

            // Handler resolution:
            // 1. Check the handlers mapping specified to ``makeRequest``
            //   a. By reference
            //   b. By name
            // 2. Check the defaultHandler specified to ``makeRequest``
            // 3. Check the default for this endpoint
            // 4. Check the default for the parent Deproxy
            // 5. Fallback to simpleHandler

            def handler
            if (messageChain &&
                    messageChain.handlers &&
                    messageChain.handlers.containsKey(this)) {

                handler = messageChain.handlers[this]

            } else if (messageChain &&
                    messageChain.handlers &&
                    messageChain.handlers.containsKey(this.name)) {

                handler = messageChain.handlers[this.name]

            } else if (messageChain && messageChain.defaultHandler) {

                handler = messageChain.defaultHandler

            } else if (this.defaultHandler) {

                handler = this.defaultHandler

            } else if (this.deproxy.defaultHandler) {

                handler = this.deproxy.defaultHandler

            } else {

                handler = Handlers.&simpleHandler

            }

            log.debug "calling handler"
            Response response
            HandlerContext context = new HandlerContext()

            if (handler instanceof Closure) {
                if (handler.getMaximumNumberOfParameters() == 1) {

                    response = handler(request)

                } else if (handler.getMaximumNumberOfParameters() == 2) {

                    response = handler(request, context)

                } else {

                    throw new UnsupportedOperationException("Invalid number of parameters in handler")
                }

            } else {

                response = handler(request)
            }

            log.debug "returned from handler"


            if (context.sendDefaultResponseHeaders) {

                if (!response.headers.contains("Server")) {
                    response.headers.add("Server", Deproxy.VERSION_STRING)
                }
                if (!response.headers.contains("Date")) {
                    response.headers.add("Date", datetimeString())
                }

                if (response.body) {

                    if (context.usedChunkedTransferEncoding) {

                        if (!response.headers.contains("Transfer-Encoding")) {
                            response.headers.add("Transfer-Encoding", "chunked")
                        }

                    } else if (!response.headers.contains("Transfer-Encoding") ||
                            response.headers["Transfer-Encoding"] == "identity") {

                        def length
                        String contentType
                        if (response.body instanceof String) {
                            length = response.body.length()
                            contentType = "text/plain"
                        } else if (response.body instanceof byte[]) {
                            length = response.body.length
                            contentType = "application/octet-stream"
                        } else {
                            throw new UnsupportedOperationException("Unknown data type in requestBody")
                        }

                        if (length > 0) {
                            if (!response.headers.contains("Content-Length")) {
                                response.headers.add("Content-Length", length)
                            }
                            if (!response.headers.contains("Content-Type")) {
                                response.headers.add("Content-Type", contentType)
                            }
                        }
                    }
                }

                if (!response.headers.contains("Content-Length") &&
                        !response.headers.contains("Transfer-Encoding")) {

                    response.headers.add("Content-Length", 0)
                }
            }

            if (requestId && !response.headers.contains(Deproxy.REQUEST_ID_HEADER_NAME)) {
                response.headers.add(Deproxy.REQUEST_ID_HEADER_NAME, requestId)
            }

            def handling = new Handling(this, request, response, connectionName)
            if (messageChain) {
                messageChain.addHandling(handling)
            } else {
                this.deproxy.addOrphanedHandling(handling)
            }

            return new ResponseWithContext(response: response, context:context)

        } finally {

        }
    }

    String datetimeString() {
        // Return the current date and time formatted for a message header.

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

}
