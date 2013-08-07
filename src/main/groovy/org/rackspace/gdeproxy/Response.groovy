package org.rackspace.gdeproxy

class Response {

    /*
     * A simple HTTP Response, with status code, status message, headers, and
     * body.
     *
     */

    String code
    String message
    HeaderCollection headers
    def body

    public Response(code, message=null, headers=null, body=null) {

        """Parameters:

        code - A numerical status code. This doesn't have to be a valid HTTP
            status code; for example, values >= 600 are acceptable also, as
            are non-numbers.

        message - An optional message to go along with the status code. If
            null, a suitable default will be provided based on the given
            status code. If ``code`` is not a valid HTTP status code, then
            the default is the empty string.

        headers - An optional collection of name/value pairs, either a mapping
            object like ``['name': 'value']``, or a HeaderCollection. Defaults
            to an empty map.

        body - An optional request body. Defaults to the empty string. Both
            strings and byte arrays are acceptable. All other types are
            toString'd.
        """

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
        this.message = message.toString()
        this.headers = new HeaderCollection(headers)
        this.body = body
    }

    @Override
    String toString() {
        sprintf('Response(code=%s, message=%s, headers=%s, body=%s)', code, message, headers, body)
    }
}



