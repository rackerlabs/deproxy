package org.rackspace.deproxy

import groovy.util.logging.Log4j

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */

@Log4j
class HeaderWriter {
    static void writeHeaders(OutputStream outStream, HeaderCollection headers) {

        PrintWriter writer = new PrintWriter(outStream)

        log.debug("Sending headers")
        for (Header header : headers.getItems()) {
            writer.write("${header.name}: ${header.value}")
            writer.write("\r\n")
            log.debug "  \"${header.name}: ${header.value}\""
        }

        writer.write("\r\n")
        writer.flush()
    }
}
