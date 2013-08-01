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

        writer.flush();

        HeaderWriter.writeHeaders(outStream, request.headers)

        writer.flush();
        outStream.flush();

        BodyWriter.writeBody(request.body, outStream)

        InputStream inStream = s.getInputStream();

        log.debug "reading response line"
        String responseLine = LineReader.readLine(inStream)
        log.debug "response read: ${responseLine}"

        def words = responseLine.split("\\s+", 3)
        if (words.size() != 3)
        {
            throw new RuntimeException()
        }

        def proto = words[0]
        def code = words[1]
        def message = words[2]

        HeaderCollection headers = HeaderReader.readHeaders(inStream)

        log.debug "reading body"
        def body = BodyReader.readBody(inStream, headers)

        log.debug "creating response object"
        def response = new Response(code, message, headers, body)

        log.debug "returning response object"
        return response
    }


}
