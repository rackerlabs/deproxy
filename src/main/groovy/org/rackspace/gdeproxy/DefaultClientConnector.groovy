package org.rackspace.gdeproxy

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
class DefaultClientConnector extends BareClientConnector {

    public DefaultClientConnector() {
        this(null)
    }
    public DefaultClientConnector(Socket socket) {
        super(socket)
    }

    @Override
    Response sendRequest(Request request, boolean https, host, port, RequestParams params) {

        if (params.sendDefaultRequestHeaders) {

            if (request.body) {

                if (params.usedChunkedTransferEncoding) {

                    if (!request.headers.contains("Transfer-Encoding")) {
                        request.headers.add("Transfer-Encoding", "chunked")
                    }

                } else if (!request.headers.contains("Transfer-Encoding") ||
                            request.headers["Transfer-Encoding"] == "identity") {

                    def length
                    String contentType
                    if (request.body instanceof String) {
                        length = request.body.length()
                        contentType = "text/plain"
                    } else if (request.body instanceof byte[]) {
                        length = request.body.length
                        contentType = "application/octet-stream"
                    } else {
                        throw new UnsupportedOperationException("Unknown data type in requestBody")
                    }

                    if (length > 0) {
                        if (!request.headers.contains("Content-Length")) {
                            request.headers.add("Content-Length", length)
                        }
                        if (!request.headers.contains("Content-Type")) {
                            request.headers.add("Content-Type", contentType)
                        }
                    }
                }
            }

            if (!request.headers.contains("Host")){
                request.headers.add("Host", host)
            }
            if (!request.headers.contains("Accept")){
                request.headers.add("Accept", "*/*")
            }
            if (!request.headers.contains("Accept-Encoding")){
                request.headers.add("Accept-Encoding", "identity")
            }
            if (!request.headers.contains("User-Agent")){
                request.headers.add("User-Agent", Deproxy.VERSION_STRING)
            }
        }

        return super.sendRequest(request, https, host, port, params)
    }
}