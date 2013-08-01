package org.rackspace.gdeproxy

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
class DefaultClientConnector extends BareClientConnector {

    @Override
    Response sendRequest(Request request, boolean https, host, port, RequestParams params) {

        if (params.sendDefaultRequestHeaders) {

            if (request.body &&
                    !params.usedChunkedTransferEncoding &&
                    !request.headers.contains("Content-Length")) {

                def length
                if (request.body instanceof String) {
                    length = request.body.length()
                } else if (request.body instanceof byte[]) {
                    length = request.body.length
                } else {
                    throw new UnsupportedOperationException("Unknown data type in requestBody")
                }

                if (length > 0) {
                    request.headers.add("Content-Length", length)
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