package org.rackspace.gdeproxy

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.concurrent.locks.ReentrantLock

import groovy.util.logging.Log4j;
import org.apache.log4j.Logger;

/**
 * The main class.
 */
@Log4j
class Deproxy {

    public static final String REQUEST_ID_HEADER_NAME = "Deproxy-Request-ID";
    def _messageChainsLock = new ReentrantLock()
    def _messageChains = [:]
    def _endpointLock = new ReentrantLock()
    def _endpoints = []
    def _defaultHandler = null
    def _defaultClientConnector

    public static final String VERSION = getVersion()
    public static final String VERSION_STRING = String.format("gdeproxy %s", VERSION);

    private static String getVersion() {
        def res = Deproxy.class.getResourceAsStream("version.txt")
        def bytes = []
        while (true) {
            def b = res.read()
            if (b < 0) break

            bytes += (byte) b
        }

        return new String(bytes as byte[], "UTF-8")
    }

    Deproxy(defaultHandler=null, defaultClientConnector=null) {

        if (defaultClientConnector == null) {
            defaultClientConnector = new DefaultClientConnector()
        }

        _defaultHandler = defaultHandler;
        _defaultClientConnector = defaultClientConnector
    }

    public MessageChain makeRequest(Map params) {
        return makeRequest(
                params?.url,
                params?.method ?: "GET",
                params?.headers,
                params?.requestBody ?: "",
                params?.defaultHandler,
                params?.handlers,
                (params?.addDefaultHeaders == null ? true : params?.addDefaultHeaders),
                (params?.chunked ? true : false),
                params?.clientConnector ?: null
        );
    }

    public MessageChain makeRequest(
            String url,
            String method="GET",
            headers=null,
            requestBody="",
            defaultHandler=null,
            handlers=null,
            addDefaultHeaders=true,
            boolean chunked=false,
            ClientConnector clientConnector=null) {

        log.debug "begin makeRequest"

        def data
        if (headers == null) {
            headers = new HeaderCollection()
        } else if (headers instanceof Map) {
            data = new HeaderCollection()
            for (String key : headers.keySet()) {
                data.add(key, headers[key])
            }
            headers = data
        } else if (headers instanceof HeaderCollection) {
            data = new HeaderCollection()
            for (Header header : headers) {
                data.add(header.name, header.value)
            }
            headers = data
        }

        if (!clientConnector) {
            clientConnector = this._defaultClientConnector;
        }

        def requestId = UUID.randomUUID().toString()

        if (!headers.contains(REQUEST_ID_HEADER_NAME)) {
            headers.add(REQUEST_ID_HEADER_NAME, requestId)
        }

        def messageChain = new MessageChain(defaultHandler, handlers)
        addMessageChain(requestId, messageChain)


        def uri = new URI(url)
        def host = uri.host
        def port = uri.port
        boolean https = (uri.scheme == 'https');
        URI uri2 = new URI(uri.scheme, uri.authority, null, null, null)
        def path = uri2.relativize(uri).toString()
        if (!path.startsWith("/")) {
            path = "/" + path
        }


        log.debug "request body: ${requestBody}"

        Request request = new Request(method, path, headers, requestBody)

        RequestParams requestParams = new RequestParams()
        requestParams.usedChunkedTransferEncoding = chunked;
        requestParams.sendDefaultRequestHeaders = addDefaultHeaders;

        log.debug "calling sendRequest"
        Response response = clientConnector.sendRequest(request, https, host, port, requestParams)
        log.debug "back from sendRequest"

        removeMessageChain(requestId)

        messageChain.sentRequest = request
        messageChain.receivedResponse = response

        log.debug "end makeRequest"

        return messageChain
    }

    def addEndpoint(int port, name=null, hostname=null, defaultHandler=null) {

        def endpoint = null

        synchronized (_endpointLock) {

            if (name == null) {
                name = String.format("Endpoint-%d", _endpoints.size())
            }

            endpoint = new DeproxyEndpoint(this, port, name, hostname, defaultHandler)

            _endpoints.add(endpoint)

            return endpoint
        }
    }

    def _remove_endpoint(endpoint) {

        synchronized (_endpointLock) {

            def count = _endpoints.size()

            _endpoints = _endpoints.findAll { e -> e != endpoint }

            return (count != _endpoints.size())
        }
    }

    def shutdown() {

        synchronized (_endpointLock) {
            for (e in _endpoints) {
                e.shutdown()
            }
            _endpoints = []
        }
    }

    def addMessageChain(requestId, messageChain) {

        synchronized (_messageChainsLock) {

            _messageChains[requestId] = messageChain
        }
    }

    def removeMessageChain(requestId) {

        synchronized (_messageChainsLock) {

            _messageChains.remove(requestId)
        }
    }

    def getMessageChain(requestId) {

        synchronized (_messageChainsLock) {

            if (_messageChains.containsKey(requestId)) {

                return _messageChains[requestId]

            } else {

                return null
            }
        }
    }

    def addOrphanedHandling(handling) {

        synchronized (_messageChainsLock) {

            for (mc in _messageChains.values()) {

                mc.addOrphanedHandling(handling)
            }
        }
    }

}