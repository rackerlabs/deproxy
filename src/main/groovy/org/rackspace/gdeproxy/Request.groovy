package org.rackspace.gdeproxy

/**
 * A simple HTTP Request, with method, path, headers, and body.
 */
class Request {

    String method
    String path
    HeaderCollection headers
    def body

    /**
     *
     * Creates a Request object
     *
     * @param method The HTTP method to use, such as 'GET', 'POST', or 'PUT'.
     * @param path The path of the resource requested, without host info,
     * e.g. "/path/to/resource"
     * @param headers An optional collection of name/value pairs, either a
     * map, like "['name': 'value']", or a HeaderCollection. Defaults to an
     * empty map.
     * @param body An optional request body. Defaults to the empty string.
     * Both strings and byte arrays are acceptable. All other types are
     * toString'd.
     */
    public Request(method, path, headers=[:], body=null) {

        if (body == null) {
            body = ""
        } else if (!(body instanceof byte[]) &&
                   !(body instanceof String)) {
            body = body.toString()
        }

        this.method = method.toString()
        this.path = path.toString()
        this.headers = new HeaderCollection(headers)
        this.body = body
    }

    String toString() {
        sprintf('Request(method=%s, path=%s, headers=%s, body=%s)', method, path, headers, body);
    }

}


