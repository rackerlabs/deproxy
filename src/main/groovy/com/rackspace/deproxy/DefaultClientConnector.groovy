package com.rackspace.deproxy


class DefaultClientConnector implements ClientConnector {

    public DefaultClientConnector(ClientConnector nextConnector=null) {

        if (nextConnector == null) {
            nextConnector = new BareClientConnector()
        }

        this.nextConnector = nextConnector
    }

    ClientConnector nextConnector

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
                        length = request.body.getBytes("US-ASCII").length
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

            if (!request.headers.contains("Host")) {
                def port2 = port
                if ((port == 80 && !https) ||
                        (port == 443 && https)){
                    port2 = null
                }
                request.headers.add("Host", HostHeader.CreateHostHeaderValueNoCheck(host, port2))
            }
            if (!request.headers.contains("Accept")){
                request.headers.add("Accept", "*/*")
            }
            if (!request.headers.contains("Accept-Encoding")){
                request.headers.add("Accept-Encoding", "identity, gzip, compress, deflate, *;q=0")
            }
            if (!request.headers.contains("User-Agent")){
                request.headers.add("User-Agent", Deproxy.VERSION_STRING)
            }
        }

        return nextConnector.sendRequest(request, https, host, port, params)
    }
}