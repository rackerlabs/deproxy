package org.rackspace.gdeproxy

import groovy.util.logging.Log4j

import javax.net.ssl.SSLSocketFactory

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 * Send requests by writing bytes directly to the socket
 *
 */
@Log4j
class BareClientConnector implements ClientConnector {

    @Override
    Response sendRequest(Request request, boolean https, host, port) {
        //        """Send the given request to the host and return the Response."""
        //        logger.debug('sending request (scheme="%s", host="%s")' %
        //                     (scheme, host))
        log.debug "sending request: https=${https}, host=${host}, port=${port}"
        if (port == null || port == "") {
            if (https) {
                port = 443
            }
            else {
                port = 80
            }
        }
        //        hostname = hostparts[0]
        //        hostip = socket.gethostbyname(hostname)
        def hostIP = InetAddress.getByName(host)
        //
        //        request_line = '%s %s HTTP/1.1\r\n' % (request.method, request.path)
        def requestLine = String.format("%s %s HTTP/1.1", request.method, request.path ?: "/")
        //        lines = [request_line]
        //
        //        for name, value in request.headers.iteritems():
        //            lines.append('%s: %s\r\n' % (name, value))
        log.debug "creating socket: host=${host}, port=${port}"

        Socket s;
        //        if scheme == 'https':
        if (https) {
            //            s = self.create_ssl_connection(address)
            s = SSLSocketFactory.getDefault().createSocket(host, port)
            //        else:
        } else {
            //            s = socket.create_connection(address)
            s = new Socket(host, port)
        }

        //    def writer = new SocketWriter(new CountingOutputStream(s.getOutputStream()))
        def writer = new PrintWriter(s.getOutputStream(), true);

        writer.write(requestLine);
        writer.write("\r\n");
        log.debug "Sending \"${requestLine}\""

        for (Header header : request.headers.getItems()) {
            writer.write("${header.name}: ${header.value}");
            writer.write("\r\n");
            log.debug "Sending \"${header.name}: ${header.value}\""
        }

        //        lines.append('\r\n')
        writer.write("");
        writer.write("\r\n");
        log.debug "Sending \"\""

        //        if request.body is not None and len(request.body) > 0:
        if (request.body != null & request.body != "") {
            //            lines.append(request.body)
            writer.write(request.body);
            log.debug "Sending body, length = ${request.body.length()}"

        }
        else {
            log.debug("No body to send");
        }

        //
        //        #for line in lines:
        //        # logger.debug(' ' + line)
        //
        //        logger.debug('Creating connection (hostname="%s", port="%s")' %
        //                     (hostname, str(port)))
        //
        //        address = (hostname, port)

        //
        //        s.send(''.join(lines))
        log.debug "flush()"
        writer.flush();

        //
        //        rfile = s.makefile('rb', -1)
        log.debug "creating socket reader"
        //    def reader = new SocketReader(new CountingInputStream(s.getInputStream()))
        def reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        //
        //        logger.debug('Reading response line')
        log.debug "reading response line"
        //        response_line = rfile.readline(65537)
        String responseLine = reader.readLine()
        //        if (len(response_line) > 65536):
        //            raise ValueError
        //        response_line = response_line.rstrip('\r\n')
        //        logger.debug('Response line is ok: %s' % response_line)
        log.debug "response line is ok: ${responseLine}"
        //
        //        words = response_line.split()
        def words = responseLine.split("\\s+", 3)
        if (words.size() != 3)
        {
            throw new RuntimeException()
        }
        //
        //        proto = words[0]
        //        code = words[1]
        //        message = ' '.join(words[2:])
        def proto = words[0]
        def code = words[1]
        def message = words[2]
        //
        //        logger.debug('Reading headers')
        log.debug "reading headers"
        //        response_headers = HeaderCollection.from_stream(rfile)
        def headers = HeaderCollection.fromReader(reader)
        //        logger.debug('Headers ok')
        //        for k,v in response_headers.iteritems():
        //            logger.debug(' %s: %s', k, v)
        headers.each {
            log.debug "  ${it.name}: ${it.value}"
        }

        //
        //        logger.debug('Reading body')
        //        body = read_body_from_stream(rfile, response_headers)
        log.debug "reading body"
        def body = Deproxy.readBody(reader, headers)
        //
        //        logger.debug('Creating Response object')
        //        response = Response(code, message, response_headers, body)
        log.debug "creating response object"
        def response = new Response(code, message, headers, body)
        //
        //        logger.debug('Returning Response object')
        //        return response
        //
        log.debug "returning response object"
        return response
    }
}
