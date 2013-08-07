package org.rackspace.gdeproxy

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.StringEntity

class DeproxyHttpRequest extends HttpEntityEnclosingRequestBase {

    Request request;
    def uri;

    DeproxyHttpRequest(Request request, String scheme, String host, def port = null) {
        this.request = request;

        String url;
        def builder = new URIBuilder();
        builder.setScheme(scheme);
        builder.setHost(host);
        builder.setPath(request.path);
        if (port) {
            builder.setPort(port);
        }

        this.setURI(builder.build())

        request.headers.each { Header header ->
            this.addHeader(header.name, header.value);
        }

        if (request.body instanceof String) {
            this.setEntity(new StringEntity(request.body));
        } else if (request.body instanceof byte[]) {
            this.setEntity(new ByteArrayEntity(request.body));
        } else {
            throw new UnsupportedOperationException("Bad type of request.body");
        }
    }

    @Override
    String getMethod() {
        return request.method
    }
}
