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
    Response sendRequest(Request request, boolean https, host, port, RequestParams params) {
        """Send the given request to the host and return the Response."""
        log.debug "sending request: https=${https}, host=${host}, port=${port}"

        if (port == null || port == "") {
            port = ( https ? 443 : 80 );
        }

        def hostIP = InetAddress.getByName(host)

        def requestLine = String.format("%s %s HTTP/1.1", request.method, request.path ?: "/")

        log.debug "creating socket: host=${host}, port=${port}"
        Socket s;
        if (https) {
            s = SSLSocketFactory.getDefault().createSocket(host, port)
        } else {
            s = new Socket(host, port)
        }

        def outStream = s.getOutputStream();
        def writer = new PrintWriter(outStream, true);

        writer.write(requestLine);
        writer.write("\r\n");
        log.debug "Sending \"${requestLine}\""

        for (Header header : request.headers.getItems()) {
            writer.write("${header.name}: ${header.value}");
            writer.write("\r\n");
            log.debug "Sending \"${header.name}: ${header.value}\""
        }

        writer.write("");
        writer.write("\r\n");

        writer.flush();
        outStream.flush();

        if (request.body == null ||
                request.body == "" ||
                request.body == [] as byte[]) {

            log.debug("No body to send");

        } else if (request.body instanceof String) {
            log.debug "Sending string body, length = ${request.body.length()}"
            def count = writer.write(request.body)
            writer.flush();
        } else if (request.body instanceof byte[]) {
            log.debug "Sending binary body, length = ${request.body.length}"
            def count = outStream.write(request.body)
            outStream.flush()
        } else {
            throw new UnsupportedOperationException("Unknown data type in request body")
        }

        writer.flush();
        outStream.flush();

        log.debug "creating socket reader"
        InputStream inStream = s.getInputStream();
        def reader = new InputStreamReader(inStream);

        log.debug "reading response line"
        String responseLine = Deproxy.readLine(reader)
        log.debug "response line is ok: ${responseLine}"

        def words = responseLine.split("\\s+", 3)
        if (words.size() != 3)
        {
            throw new RuntimeException()
        }

        def proto = words[0]
        def code = words[1]
        def message = words[2]

        log.debug "reading headers"
        def headers = HeaderCollection.fromReader(reader)
        if (headers.size() > 0) {
            headers.each {
                log.debug "  ${it.name}: ${it.value}"
            }
        } else {
            log.debug "no headers received"
        }

        log.debug "reading body"
        def body = Deproxy.readBody(inStream, headers)

        log.debug "creating response object"
        def response = new Response(code, message, headers, body)

        log.debug "returning response object"
        return response
    }
}
