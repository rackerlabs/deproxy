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

    Deproxy _deproxy;
    String _name;
    int _port;
    String _hostname;
    Object _defaultHandler;
    SystemClock clock = new SystemClock();

    Thread serverThread;
    ServerSocket serverSocket;

    //    def __init__(self, deproxy, port, name, hostname=None,
    //                 default_handler=None):
    public DeproxyEndpoint(Deproxy deproxy, int port, String name) {
        this(deproxy, port, name, "localhost");
    }

    public DeproxyEndpoint(Deproxy deproxy, int port, String name, String hostname) {
        this(deproxy, port, name, hostname, null);
    }

    public DeproxyEndpoint(Deproxy deproxy, int port, String name, String hostname, Object defaultHandler) {
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
        //        logger.debug('port=%s, name=%s, hostname=%s', port, name, hostname)
        //
        //        if hostname is None:
        if (hostname == null) {
            //            hostname = 'localhost'
            hostname = "localhost"
        }
        //
        //        self.deproxy = deproxy
        _deproxy = deproxy
        //        self.name = name
        _name = name
        //        self.port = port
        _port = port
        //        self.hostname = hostname
        _hostname = hostname
        //        self.default_handler = default_handler
        _defaultHandler = defaultHandler
        //
        serverThread = new Thread("Thread-${name}")

        //    serverThread = Thread.startDaemon("Thread-${name}") {
        serverSocket = new ServerSocket(port)
        //      serverSocket.setReuseAddress(true)
        //      serverSocket.setSoTimeout(5000)
        //      serverSocket.bind(new InetSocketAddress(port))
        serverThread = new DeproxyEndpointListenerThread(this, serverSocket, "Thread-${name}");
        serverThread.start();

        waitForCondition(clock, '5s', '1s', {
            isListening()
        })
    }

    public class DeproxyEndpointListenerThread extends Thread {

        DeproxyEndpoint _parent;
        ServerSocket _socket;
        Logger log = Logger.getLogger(DeproxyEndpointListenerThread.class.getName());

        public DeproxyEndpointListenerThread(DeproxyEndpoint parent, ServerSocket socket, String name) {

            this.setName(name)
            _parent = parent;
            _socket = socket;
        }

        @Override
        public void run() {

            while (!_socket.isClosed()) {
                try {
                    _socket.setSoTimeout(1000);

                    Socket socket;
                    try {
                        socket = _socket.accept();
                    } catch (SocketException e) {
                        if (_socket.isClosed()) {
                            break;
                        } else {
                            throw e;
                        }
                    }

                    log.debug("Accepted a new connection");
                    //socket.setSoTimeout(1000);
                    log.debug("Creating the handler thread");
                    String connectionName = UUID.randomUUID().toString()
                    DeproxyEndpointHandlerThread handlerThread = new DeproxyEndpointHandlerThread(_parent, socket, connectionName, this.getName() + "-connection-" + connectionName.toString());
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

        DeproxyEndpoint _parent;
        Socket _socket;
        String connectionName

        public DeproxyEndpointHandlerThread(DeproxyEndpoint parent, Socket socket, String connectionName, String threadName) {

            this.setName(threadName);
            _parent = parent;
            _socket = socket;
            this.connectionName = connectionName
        }

        @Override
        public void run() {
            log.debug("Processing new connection");
            _parent.processNewConnection(_socket, connectionName);
            log.debug("Connection processed");
        }
    }

    Socket createRawConnection() {
        return new Socket("localhost", this._port)
    }

    //    def process_new_connection(self, request, client_address):
    //        logger.debug('received request from %s' % str(client_address))
    //        try:
    //            connection = request
    //            if self.disable_nagle_algorithm:
    //                connection.setsockopt(socket.IPPROTO_TCP,
    //                                      socket.TCP_NODELAY, True)
    //            rfile = connection.makefile('rb', -1)
    //            wfile = connection.makefile('wb', 0)
    //
    //            try:
    //                close = self.handle_one_request(rfile, wfile)
    //                while not close:
    //                    close = self.handle_one_request(rfile, wfile)
    //            finally:
    //                if not wfile.closed:
    //                    wfile.flush()
    //                wfile.close()
    //                rfile.close()
    //        except:
    //            self.handle_error(request, client_address)
    //        finally:
    //            self.shutdown_request(request)
    //
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
            //reader.close()
            socket.close()
        }

        log.debug "done processing"

    }

    //    def shutdown_request(self, request):
    //        """Called to shutdown and close an individual request."""
    //        logger.debug('')
    //        try:
    //            #explicitly shutdown. socket.close() merely releases
    //            #the socket and waits for GC to perform the actual close.
    //            request.shutdown(socket.SHUT_WR)
    //        except socket.error:
    //            pass # some platforms may raise ENOTCONN here
    //        request.close()
    //

    //    def serve_forever(self, poll_interval=0.5):
    //        """Handle one request at a time until shutdown.
    //
    //Polls for shutdown every poll_interval seconds. Ignores
    //self.timeout. If you need to do periodic tasks, do them in
    //another thread.
    //"""
    //        logger.debug('')
    //        self.__is_shut_down.clear()
    //        try:
    //            while not self.__shutdown_request:
    //                # XXX: Consider using another file descriptor or
    //                # connecting to the socket to wake this up instead of
    //                # polling. Polling reduces our responsiveness to a
    //                # shutdown request and wastes cpu at all other times.
    //                r, w, e = select.select([self.socket], [], [], poll_interval)
    //                if self.socket in r:
    //                    try:
    //                        request, client_address = self.socket.accept()
    //                    except socket.error:
    //                        return
    //
    //                    try:
    //                        with self._conn_number_lock:
    //                            t = threading.Thread(
    //                                target=self.process_new_connection,
    //                                name=("Thread - Connection %i on %s" %
    //                                      (self._conn_number, self.name)),
    //                                args=(request, client_address))
    //                            self._conn_number += 1
    //                        t.daemon = True
    //                        t.start()
    //
    //                    except:
    //                        self.handle_error(request, client_address)
    //                        self.shutdown_request(request)
    //
    //        finally:
    //            self.socket.close()
    //            self.__shutdown_request = False
    //            self.__is_shut_down.set()
    //

    //    def shutdown(self):
    //        """Stops the serve_forever loop.
    //
    //Blocks until the loop has finished. This must be called while
    //serve_forever() is running in another thread, or it will
    //deadlock.
    //"""
    //        logger.debug('Shutting down "%s"' % self.name)
    //        self.deproxy._remove_endpoint(self)
    //        self.__shutdown_request = True
    //        self.__is_shut_down.wait()
    //        self.server_thread.join(timeout=5)
    //        logger.debug('Finished shutting down "%s"' % self.name)
    //
    def shutdown() {
        log.debug "shutting down"

        log.debug("Shutting down ${_name}")
        if (serverThread) {
            serverThread.interrupt()
        }
        if (serverSocket)
            serverSocket.close()
        log.debug("Finished shutting down ${_name}")
    }


    boolean isListening() {
        return serverSocket != null && !serverSocket.isClosed()
    }

    //    def handle_error(self, request, client_address):
    //        """Handle an error gracefully. May be overridden.
    //
    //The default is to print a traceback and continue.
    //
    //"""
    //        logger.debug('')
    //        print '-' * 40
    //        print 'Exception happened during processing of request from',
    //        print client_address
    //        import traceback
    //        traceback.print_exc() # XXX But this goes to stderr!
    //        print '-' * 40
    //

    //    def handle_one_request(self, rfile, wfile):
    def handleOneRequest(InputStream inStream, OutputStream outStream, String connectionName) {

        log.debug "Begin handleOneRequest"
        def closeConnection = false

        try {
            //            logger.debug('calling parse_request')
            //            ret = self.parse_request(rfile, wfile)
            log.debug "calling parseRequest"
            def ret = parseRequest(inStream, outStream)
            //            logger.debug('returned from parse_request')
            log.debug "returned from parseRequest"
            //            if not ret:
            //                return 1
            if (!ret) {
                return true
            }
            //
            //            (incoming_request, persistent_connection) = ret
            def (request, persistConnection) = ret
            //
            //            if persistent_connection:
            if (persistConnection) {
                //                close_connection = False
                closeConnection = false
                //                conn_value = incoming_request.headers.get('connection')
                //                if conn_value:
                //                    if conn_value.lower() == 'close':
                //                        close_connection = True
                if (request.headers.contains('Connection')) {
                    request.headers.findAll('Connection').each {
                        if (it == "close") {
                            closeConnection = true
                        }
                    }
                }
                //            else:
            } else {
                //                close_connection = True
                closeConnection = true
            }

            MessageChain messageChain = null
            //            request_id = incoming_request.headers.get(request_id_header_name)
            def requestId = request.headers.getFirstValue(Deproxy.REQUEST_ID_HEADER_NAME)
            if (requestId) {
                //            if request_id:
                //                logger.debug('The request has a request id: %s=%s' %
                //                             (request_id_header_name, request_id))
                log.debug "the request has a request id: ${Deproxy.REQUEST_ID_HEADER_NAME}=${requestId}"
                //                message_chain = self.deproxy.get_message_chain(request_id)
                messageChain = _deproxy.getMessageChain(requestId)
                //            else:
            } else {
                //                logger.debug('The request does not have a request id')
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
                    messageChain.handlers.containsKey(this._name)) {

                handler = messageChain.handlers[this._name]

            } else if (messageChain && messageChain.defaultHandler) {

                handler = messageChain.defaultHandler

            } else if (_defaultHandler) {

                handler = _defaultHandler

            } else if (_deproxy._defaultHandler) {

                handler = _deproxy._defaultHandler

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
            //
            //            outgoing_response = resp
            //
            //            h = Handling(self, incoming_request, outgoing_response)
            //            if message_chain:
            //                message_chain.add_handling(h)
            //            else:
            //                self.deproxy.add_orphaned_handling(h)
            def handling = new Handling(this, request, response, connectionName)
            if (messageChain) {
                messageChain.addHandling(handling)
            } else {
                _deproxy.addOrphanedHandling(handling)
            }
            //
            //            self.send_response(wfile, resp)
            sendResponse(outStream, response, context)
            //
            //            wfile.flush()
            //
            //            if persistent_connection and not close_connection:
            //                conn_value = incoming_request.headers.get('connection')
            //                if conn_value:
            //                    if conn_value.lower() == 'close':
            //                        close_connection = True
            if (persistConnection && !closeConnection) {
                if (request.headers.contains('Connection')) {
                    request.headers.findAll('Connection').each {
                        if (it == "close") {
                            closeConnection = true
                        }
                    }
                }
            }
            //
            //        except socket.timeout, e:
            //    } catch (SocketTimeoutException e) {
            //      //            close_connection = True
            //      closeConnection = true
        } finally {

        }
        //
        //        return close_connection
        //
        return closeConnection
    }

    //    def parse_request(self, rfile, wfile):
    def parseRequest(InputStream inStream, OutputStream outStream) {

        log.debug "reading request line"
        def requestLine = LineReader.readLine(inStream)

        if (!requestLine) {
            log.debug "request line is null: ${requestLine}"

            return []
        }
        //
        //        request_line = request_line.rstrip('\r\n')
        //        logger.debug('request line is ok: "%s"' % request_line)
        log.debug "request line is not null: ${requestLine}"
        //
        //        if request_line[-2:] == '\r\n':
        //            request_line = request_line[:-2]
        //        elif request_line[-1:] == '\n':
        //            request_line = request_line[:-1]
        //        words = request_line.split()
        def words = requestLine.split("\\s+")
        log.debug "${words}"
        //        if len(words) == 3:
        def version
        def method
        def path
        if (words.size() == 3) {
            //            [method, path, version] = words
            method = words[0]
            path = words[1]
            version = words[2]

            log.debug "${method}, ${path}, ${version}"
            //            if version[:5] != 'HTTP/':
            //                self.send_error(wfile, 400, method,
            //                                self.default_request_version,
            //                                "Bad request version (%r)" % version)
            //                return ()
            if (!version.startsWith("HTTP/")) {
                sendResponse(outStream, new Response(400, null, null, "Bad request version \"${version}\""))
                return []
            }
            //            try:
            //                base_version_number = version.split('/', 1)[1]
            //                version_number = base_version_number.split(".")
            //                # RFC 2145 section 3.1 says there can be only one "." and
            //                # - major and minor numbers MUST be treated as
            //                # separate integers;
            //                # - HTTP/2.4 is a lower version than HTTP/2.13, which in
            //                # turn is lower than HTTP/12.3;
            //                # - Leading zeros MUST be ignored by recipients.
            //                if len(version_number) != 2:
            //                    raise ValueError
            //                version_number = int(version_number[0]), int(version_number[1])
            //            except (ValueError, IndexError):
            //                self.send_error(wfile, 400, method,
            //                                self.default_request_version,
            //                                "Bad request version (%r)" % version)
            //                return ()
        }
        //        elif len(words) == 2:
        //            [method, path] = words
        //            version = self.default_request_version
        //            if method != 'GET':
        //                self.send_error(wfile, 400, method,
        //                                self.default_request_version,
        //                                "Bad HTTP/0.9 request type (%r)" % method)
        //                return ()
        //        elif not words:
        //            return ()
        //        else:
        else {
            //            self.send_error(wfile, 400, None,
            //                            self.default_request_version,
            //                            "Bad request syntax (%r)" % request_line)
            //            return ()
            sendResponse(outStream, new Response(400))
            return []
        }
        //
        //        logger.debug('checking HTTP protocol version')
        //        if (version != 'HTTP/1.1' and
        //                version != 'HTTP/1.0' and
        //                version != 'HTTP/0.9'):
        //            self.send_error(wfile, 505, method, self.default_request_version,
        //                            "Invalid HTTP Version (%s)" % version)
        //            return ()
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
        //        logger.debug('reading body')
        //        body = read_body_from_stream(rfile, headers)
        log.debug "reading the body"
        def body = BodyReader.readBody(inStream, headers)
        String length = (body instanceof byte[] ? body.length : body.toString().length()).toString();
        log.debug("Done reading body, length ${length}");
        //
        //        logger.debug('returning')
        //        return (Request(method, path, headers, body), persistent_connection)
        //
        log.debug "returning"
        return [
                new Request(method, path, headers, body),
                persistentConnection
        ]
    }

    //    def send_error(self, wfile, code, method, request_version, message=None):
    //        """Send and log an error reply.
    //
    //Arguments are the error code, and a detailed message.
    //The detailed message defaults to the short entry matching the
    //response code.
    //
    //This sends an error response (so it must be called before any
    //output has been generated), logs the error, and finally sends
    //a piece of HTML explaining the error to the user.
    //
    //"""
    //
    //        try:
    //            short, long = messages_by_response_code[code]
    //        except KeyError:
    //            short, long = '???', '???'
    //        if message is None:
    //            message = short
    //        explain = long
    //        error_message_format = ("Error code %(code)d.\nMessage: %(message)s.\n"
    //                                "Error code explanation: %(code)s = "
    //                                "%(explain)s.")
    //        content = (error_message_format %
    //                   {'code': code, 'message': message,
    //                    'explain': explain})
    //
    //        headers = {
    //            'Content-Type': "text/html",
    //            'Connection': 'close',
    //        }
    //
    //        if method == 'HEAD' or code < 200 or code in (204, 304):
    //            content = ''
    //
    //        response = Response(request_version, code, message, headers, content)
    //
    //        self.send_response(response)
    //

    //    def send_response(self, wfile, response):
    def sendResponse(OutputStream outStream, Response response, HandlerContext context=null) {

        def writer = new PrintWriter(outStream, true);

        //        """
        //Send the given Response over the socket. Add Server and Date headers
        //if not already present.
        //"""
        //
        //        message = response.message
        //        if message is None:
        //            if response.code in messages_by_response_code:
        //                message = messages_by_response_code[response.code][0]
        //            else:
        //                message = ''
        if (response.message == null) {
            response.message = ""
        }
        //        wfile.write("HTTP/1.1 %s %s\r\n" %
        //                    (response.code, message))
        writer.write("HTTP/1.1 ${response.code} ${response.message}")
        writer.write("\r\n")

        writer.flush()

        HeaderWriter.writeHeaders(outStream, response.headers)

        BodyWriter.writeBody(response.body, outStream,
                context?.usedChunkedTransferEncoding ?: false)

        log.debug("finished sending response")
    }

    //    def date_time_string(self, timestamp=None):
    def datetimeString() {
        //        """Return the current date and time formatted for a message header."""
        //        if timestamp is None:
        //            timestamp = time.time()
        //        year, month, day, hh, mm, ss, wd, y, z = time.gmtime(timestamp)
        //
        //        weekdayname = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
        //        monthname = [None,
        //                     'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
        //                     'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
        //
        //        s = "%s, %02d %3s %4d %02d:%02d:%02d GMT" % (weekdayname[wd], day,
        //                                                     monthname[month], year,
        //                                                     hh, mm, ss)
        //        return s
        //
        //
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

}
