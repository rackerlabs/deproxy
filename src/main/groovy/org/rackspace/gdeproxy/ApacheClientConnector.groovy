package org.rackspace.gdeproxy

import groovy.util.logging.Log4j
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.protocol.RequestContent
import org.apache.http.util.EntityUtils

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 * Send requests using the Apache HttpComponents library
 *
 */

@Log4j
class ApacheClientConnector implements ClientConnector {
    @Override
    Response sendRequest(Request request, boolean https, host, port) {

        HttpClient client = new DefaultHttpClient();
        client.removeRequestInterceptorByClass(RequestContent.class)
        def scheme = (https ? 'https' : 'http');
        def request2 = new DeproxyHttpRequest(request, scheme as String, host as String, port);
        HttpResponse response2 = client.execute(request2);

        def body;
        if (response2.entity.contentType != null &&
                response2.entity.contentType.value.toLowerCase().startsWith("text/")) {

            body = EntityUtils.toString(response2.getEntity());
        } else {
            body = EntityUtils.toByteArray(response2.getEntity());
        }

        Response response = new Response(response2.statusLine.statusCode.toString(),
                response2.statusLine.reasonPhrase,
                response2.getAllHeaders().collect { new Header(it.getName(), it.getValue()) },
                body);
        return response;
    }

}
