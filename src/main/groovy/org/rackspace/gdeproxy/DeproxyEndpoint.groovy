package org.rackspace.gdeproxy

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
    protected Thread serverThread;
    protected ServerSocket serverSocket;
    protected SystemClock clock = new SystemClock();

    public DeproxyEndpoint(Deproxy deproxy, int port, String name,
                           String hostname="localhost", Object defaultHandler=null) {
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
        this.serverThread = new Thread("Thread-${name}")

        this.serverSocket = new ServerSocket(port)

        this.serverThread = new DeproxyEndpointListenerThread(this, this.serverSocket, "Thread-${name}");
        this.serverThread.start();

        waitForCondition(this.clock, '5s', '1s', {
            isListening()
        })
    }

    public class DeproxyEndpointListenerThread extends Thread {

        DeproxyEndpoint parent;
        ServerSocket socket;
        Logger log = Logger.getLogger(DeproxyEndpointListenerThread.class.getName());

        public DeproxyEndpointListenerThread(DeproxyEndpoint parent, ServerSocket socket, String name) {

            this.setName(name)
            this.parent = parent;
            this.socket = socket;
        }

        @Override
        public void run() {

            while (!this.socket.isClosed()) {
                try {
                    this.socket.setSoTimeout(1000);

                    Socket socket;
                    try {
                        socket = this.socket.accept();
                    } catch (SocketException e) {
                        if (this.socket.isClosed()) {
                            break;
                        } else {
                            throw e;
                        }
                    }

                    log.debug("Accepted a new connection");
                    //socket.setSoTimeout(1000);
                    log.debug("Creating the handler thread");

                    String connectionName = UUID.randomUUID().toString()

                    DeproxyEndpointHandlerThread handlerThread = new DeproxyEndpointHandlerThread(this.parent, socket, connectionName, this.getName() + "-connection-" + connectionName.toString());

                    log.debug("Starting the handler thread");
                    handlerThread.start();
                    log.debug("Handler thread started");

                } catch (SocketTimeoutException ste) {
                    // do nothing
                } catch (IOException ex) {
                    log.error(null, ex);
                }
            }
        }
    }

    public class DeproxyEndpointHandlerThread extends Thread {

        Logger log = Logger.getLogger(DeproxyEndpointHandlerThread.class.getName());

        DeproxyEndpoint parent;
        Socket socket;
        String connectionName

        public DeproxyEndpointHandlerThread(DeproxyEndpoint parent,
                                            Socket socket,
                                            String connectionName,
                                            String threadName) {

            this.setName(threadName);
            this.parent = parent;
            this.socket = socket;
            this.connectionName = connectionName
        }

        @Override
        public void run() {

            log.debug("Processing new connection");

            this.parent.processNewConnection(this.socket, connectionName);

            log.debug("Connection processed");
        }
    }

    Socket createRawConnection() {

        return new Socket("localhost", this.port)
    }

    def processNewConnection(Socket socket, String connectionName) {

        log.debug "processing new connection..."
        def reader;
        def writer;
        OutputStream outStream;
        InputStream inStream;

        try {

            log.debug "getting reader"
            log.debug "getting writer"
            inStream = socket.getInputStream()
            outStream = socket.getOutputStream();

            try {

                log.debug "starting loop"
                def close = false
                while (!close) {

                    log.debug "about to handle one request"

                    close = handleOneRequest(inStream, outStream, connectionName)
                    log.debug "handled one request"
                }

                log.debug "ending loop"

            } catch (RuntimeException e) {

                log.error("there was an error", e)
                sendResponse(outStream,
                        new Response(500, "Internal Server Error", null,
                                "The server encountered an unexpected condition which prevented it from fulfilling the request."))
            }

        } finally {

            //socket.shutdownInput()
            //socket.shutdownOutput()
            socket.close()
        }

        log.debug "done processing"

    }

    def shutdown() {
        log.debug "shutting down"

        log.debug("Shutting down ${this.name}")
        if (serverThread) {
            serverThread.interrupt()
        }
        if (serverSocket)
            serverSocket.close()
        log.debug("Finished shutting down ${this.name}")
    }


    boolean isListening() {
        return serverSocket != null && !serverSocket.isClosed()
    }

    def handleOneRequest(InputStream inStream, OutputStream outStream, String connectionName) {

        log.debug "Begin handleOneRequest"
        def closeConnection = false

        try {
            log.debug "calling parseRequest"
            def ret = parseRequest(inStream, outStream)
            log.debug "returned from parseRequest"

            if (!ret) {
                return true
            }

            def (Request request, boolean persistConnection) = ret

            if (persistConnection) {

                closeConnection = false

                if (request.headers.contains('Connection')) {
                    request.headers.findAll('Connection').each {
                        if (it == "close") {
                            closeConnection = true
                        }
                    }
                }
            } else {

                closeConnection = true
            }

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

            sendResponse(outStream, response, context)

            if (persistConnection && !closeConnection) {
                if (request.headers.contains('Connection')) {
                    request.headers.findAll('Connection').each {
                        if (it == "close") {
                            closeConnection = true
                        }
                    }
                }
            }

        } finally {

        }

        return closeConnection
    }

    def parseRequest(InputStream inStream, OutputStream outStream) {

        log.debug "reading request line"
        def requestLine = LineReader.readLine(inStream)

        if (!requestLine) {
            log.debug "request line is null: ${requestLine}"

            return []
        }

        log.debug "request line is not null: ${requestLine}"

        def words = requestLine.split("\\s+")
        log.debug "${words}"

        def version
        def method
        def path
        if (words.size() == 3) {

            method = words[0]
            path = words[1]
            version = words[2]

            log.debug "${method}, ${path}, ${version}"

            if (!version.startsWith("HTTP/")) {
                sendResponse(outStream, new Response(400, null, null, "Bad request version \"${version}\""))
                return []
            }

        } else {

            sendResponse(outStream, new Response(400))
            return []
        }

        log.debug "checking http protocol version: ${version}"
        if (version != "HTTP/1.1" &&
                version != "HTTP/1.0" &&
                version != "HTTP/0.9") {

            sendResponse(outStream, new Response(505, null, null, "Invalid HTTP Version \"${version}\"}"))
            return []
        }

        HeaderCollection headers = HeaderReader.readHeaders(inStream)

        def persistentConnection = false
        if (version == "HTTP/1.1") {
            persistentConnection = true
            for (value in headers.findAll("Connection")) {
                if (value == "close") {
                    persistentConnection = false
                }
            }
        }

        log.debug "reading the body"
        def body = BodyReader.readBody(inStream, headers)
        String length = (body instanceof byte[] ? body.length : body.toString().length()).toString();
        log.debug("Done reading body, length ${length}");

        log.debug "returning"
        return [
                new Request(method, path, headers, body),
                persistentConnection
        ]
    }

    def sendResponse(OutputStream outStream, Response response, HandlerContext context=null) {

        def writer = new PrintWriter(outStream, true);

        if (response.message == null) {
            response.message = ""
        }

        writer.write("HTTP/1.1 ${response.code} ${response.message}")
        writer.write("\r\n")

        writer.flush()

        HeaderWriter.writeHeaders(outStream, response.headers)

        BodyWriter.writeBody(response.body, outStream,
                context?.usedChunkedTransferEncoding ?: false)

        log.debug("finished sending response")
    }

    def datetimeString() {
        // Return the current date and time formatted for a message header.

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

}
