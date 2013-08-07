package org.rackspace.gdeproxy

import groovy.util.logging.Log4j

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
@Log4j
class HeaderReader {
    public static HeaderCollection readHeaders(InputStream inStream) {

        log.debug "reading headers"
        def headers = HeaderCollection.fromStream(inStream)
        if (headers.size() > 0) {
            headers.each {
                log.debug "  ${it.name}: ${it.value}"
            }
        } else {
            log.debug "no headers received"
        }

        return headers
    }
}
