package org.rackspace.gdeproxy

/**
 * A simple HTTP Response, with status code, status message, headers, and
 * body.
 *
 */
class Response {

    String code
    String message
    HeaderCollection headers
    def body

    /**
     *
     * Creates a Response object
     *
     * @param code A numerical status code. This doesn't have to be a valid
     * HTTP status code; for example, values >= 600 are acceptable also, as
     * are non-numbers.
     * @param message An optional message to go along with the status code. If
     * null, a suitable default will be provided based on the given status
     * code. If code is not a valid HTTP status code, then the default is
     * the empty string.
     * @param headers An optional collection of name/value pairs, either a
     * map, like "['name': 'value']", or a HeaderCollection. Defaults to an
     * empty map.
     * @param body An optional request body. Defaults to the empty string.
     * Both strings and byte arrays are acceptable. All other types are
     * toString'd.
     */
    public Response(code, String message=null, headers=null, body=null) {

        code = code.toString()

        if (message == null) {
            if (HttpResponseMessage.messagesByResponseCode.containsKey(code)) {
                message = HttpResponseMessage.messagesByResponseCode[code]
            } else {
                message = ""
            }
        }

        if (headers == null) {
            headers = [:]
        }

        if (body == null) {
            body = ""
        } else if (!(body instanceof byte[]) &&
                   !(body instanceof String)) {
            body = body.toString()
        }

        this.code = code
        this.message = message
        this.headers = new HeaderCollection(headers)
        this.body = body
    }

    @Override
    String toString() {
        sprintf('Response(code=%s, message=%s, headers=%s, body=%s)', code, message, headers, body)
    }
}



