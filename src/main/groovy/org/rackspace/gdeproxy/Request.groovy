package org.rackspace.gdeproxy

/**
 * A simple HTTP Request, with method, path, headers, and body.
 */
class Request {

    String method
    String path
    HeaderCollection headers
    String body

    public Request(method, path, headers = HeaderCollection.newInstance(), body = null) {
        //    def __init__(self, method, path, headers=None, body=None):
        //        """
        //Parameters:
        //
        //method - The HTTP method to use, such as 'GET', 'POST', or 'PUT'.
        //path - The relative path of the resource requested.
        //headers - An optional collection of name/value pairs, either a mapping
        //object like ``dict``, or a HeaderCollection. Defaults to an empty
        //collection.
        //body - An optional request body. Defaults to the empty string.
        //"""
        this.method = method.toString()
        this.path = path.toString()
        this.headers = new HeaderCollection(headers)
        this.body = (body ? body.toString() : "")
    }

    String toString() {
    sprintf('Request(method=%s, path=%s, headers=%s, body=%s)', method, path, headers, body);
    }

}


