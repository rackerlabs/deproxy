package org.rackspace.gdeproxy

/**
 * A simple HTTP Request, with method, path, headers, and body.
 */
class Request {

    String method
    String path
    HeaderCollection headers
    def body

    public Request(method, path, headers=[:], body=null) {
        """Parameters:

        method - The HTTP method to use, such as 'GET', 'POST', or 'PUT'.

        path - The relative path of the resource requested.

        headers - An optional collection of name/value pairs, either a mapping
            object like ``['name': 'value']``, or a HeaderCollection.
            Defaults to an empty map.

        body - An optional request body. Defaults to the empty string. Both
            strings and byte arrays are acceptable. All other types are
            toString'd.
        """

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


