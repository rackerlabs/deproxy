package com.rackspace.deproxy

import groovy.util.logging.Log4j

@Log4j
class SocketServerConnector implements ServerConnector {

    ListenerThread serverThread
    ServerSocket serverSocket
    Endpoint endpoint
    int port

    int port() {
        port
    }

    public SocketServerConnector(Endpoint endpoint, Integer port) {

        if (!endpoint) { throw new IllegalArgumentException("endpoint") }

        this.endpoint = endpoint

        if(port == null) {
            serverSocket = new ServerSocket(0)
            this.port = serverSocket.getLocalPort()
        } else {
            serverSocket = new ServerSocket(port)
            this.port = port
        }

        serverThread = new ListenerThread(this, serverSocket, "Thread-${endpoint.name}");
        serverThread.start();
    }

    @Log4j
    public class ListenerThread extends Thread {

        SocketServerConnector parent;
        ServerSocket socket;

        public ListenerThread(SocketServerConnector parent, ServerSocket socket, String name) {

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

                    HandlerThread handlerThread = new HandlerThread(this.parent, socket, connectionName, this.getName() + "-connection-" + connectionName.toString());

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

    @Log4j
    public class HandlerThread extends Thread {

        SocketServerConnector parent;
        Socket socket;
        String connectionName

        public HandlerThread(SocketServerConnector parent,
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

    void processNewConnection(Socket socket, String connectionName) {

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
                boolean persistConnection = true
                while (persistConnection) {

                    log.debug "calling parseRequest"
                    def ret = parseRequest(inStream, outStream)
                    log.debug "returned from parseRequest"

                    if (!ret) {
                        break
                    }

                    Request request
                    (request, persistConnection) = ret

                    if (persistConnection &&
                            request.headers.contains('Connection')) {

                        request.headers.findAll('Connection').each {
                            if (it == "close") {
                                persistConnection = false
                            }
                        }
                    }

                    log.debug "about to handle one request"
                    ResponseWithContext rwc = endpoint.handleRequest(request, connectionName)
                    log.debug "handled one request"


                    log.debug "send the response"
                    sendResponse(outStream, rwc.response, rwc.context)

                    if (persistConnection &&
                            rwc.response.headers.contains('Connection')) {

                        rwc.response.headers.findAll('Connection').each {
                            if (it == "close") {
                                persistConnection = false
                            }
                        }
                    }
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

    void shutdown() {

        if (serverThread) {
            serverThread.interrupt()
        }
        if (serverSocket) {
            serverSocket.close()
        }
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

        String length;
        if (body == null) {
            length = 0
        } else if (body instanceof byte[]) {
            length = body.length
        } else {
            length = body.toString().length()
        }

        log.debug("Done reading body, length ${length}");

        log.debug "returning"
        return [
                new Request(method, path, headers, body),
                persistentConnection
        ]
    }

    void sendResponse(OutputStream outStream, Response response, HandlerContext context=null) {

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

}
