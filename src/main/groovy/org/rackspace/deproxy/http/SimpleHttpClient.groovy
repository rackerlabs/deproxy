package org.rackspace.deproxy.http

import org.apache.http.client.HttpClient
import org.apache.http.client.methods.*
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.rackspace.deproxy.Response

import java.nio.charset.Charset

abstract class SimpleHttpClient {

    def String endpoint = ""

    SimpleHttpClient(String endpoint) {
        this.endpoint = endpoint
    }

    def makeRequest(HttpUriRequest httpMethod, Map requestHeaders) {

        def Response response = new Response()
        def HttpClient client

        try {
            client = new DefaultHttpClient()

            requestHeaders.each { key, value ->
                httpMethod.addHeader(key, value)
            }

            def httpResponse = client.execute(httpMethod)

            response.code = httpResponse.statusLine.statusCode
            if (httpResponse.entity) {
                response.body = httpResponse.entity.content.getText()
            }
            response.headers = httpResponse.getAllHeaders()

        } finally {
            if (client != null && client.getConnectionManager() != null) {
                client.getConnectionManager().shutdown();
            }
        }

        return response
    }

    def doGet(String path, Map headers = new HashMap()) {

        def String requestPath

        if (!path.startsWith("http")) {
            requestPath = endpoint + path
        } else {
            requestPath = path
        }

        HttpGet httpGet = new HttpGet(requestPath)

        makeRequest(httpGet, headers)
    }


    def doPut(String path, Map headers, String payload) {
        def requestPath = endpoint + path

        HttpPut httpPut = new HttpPut(requestPath)
        if (payload) {
            httpPut.setEntity(new StringEntity(payload, Charset.forName("UTF-8")))
        }

        makeRequest(httpPut, headers, requestPath)
    }

    def doDelete(String path, Map headers, String payload) {
        def requestPath = endpoint + path

         EntityEnclosingDelete httpDelete = new EntityEnclosingDelete()
         URI uri = URI.create(requestPath)
         httpDelete.setURI(uri)
        if (payload) {
            httpDelete.setEntity(new StringEntity(payload, Charset.forName("UTF-8")))
        }

        makeRequest(httpDelete, headers)
    }



    def doPost(String path, Map headers, String payload) {
        def requestPath = endpoint + path

        HttpPost httpPost = new HttpPost(requestPath)
        if (payload) {
            httpPost.setEntity(new StringEntity(payload, Charset.forName("UTF-8")))
        }

        makeRequest(httpPost, headers)
    }
}

class EntityEnclosingDelete extends HttpEntityEnclosingRequestBase {

    @Override
    public String getMethod() {
        return "DELETE";
    }

}
