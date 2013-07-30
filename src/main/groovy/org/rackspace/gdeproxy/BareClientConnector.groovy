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

        def writer = new PrintWriter(s.getOutputStream(), true);

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
        log.debug "Sending \"\""

        if (request.body != null & request.body != "") {
            writer.write(request.body);
            log.debug "Sending body, length = ${request.body.length()}"
        }
        else {
            log.debug("No body to send");
        }

        log.debug "flush()"
        writer.flush();

        log.debug "creating socket reader"
        def reader = new BufferedReader(new InputStreamReader(s.getInputStream()));

        log.debug "reading response line"
        String responseLine = reader.readLine()
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
        headers.each {
            log.debug "  ${it.name}: ${it.value}"
        }

        log.debug "reading body"
        def body = Deproxy.readBody(reader, headers)

        log.debug "creating response object"
        def response = new Response(code, message, headers, body)

        log.debug "returning response object"
        return response
    }
}
