package org.rackspace.gdeproxy

import groovy.util.logging.Log4j

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
@Log4j
class BodyWriter {
    def static void writeBody(body, OutputStream outStream) {

        outStream.flush()

        if (body != null) {
            if (body instanceof String) {
                log.debug("sending string body, length ${body.length()}")
                log.debug(body)
                if (body.length() > 0) {
                    PrintWriter writer = new PrintWriter(outStream, true);
                    writer.write(body)
                    writer.flush()
                }
            } else if (body instanceof byte[]) {
                log.debug("sending binary body, length ${body.length}")
                log.debug(body.toString())
                if (body.length > 0) {
                    outStream.write(body)
                    outStream.flush()
                }
            } else {
                throw new UnsupportedOperationException("Unknown data type in request body")
            }
        } else {
            log.debug("No body to send");
        }

        outStream.flush();
    }
}
