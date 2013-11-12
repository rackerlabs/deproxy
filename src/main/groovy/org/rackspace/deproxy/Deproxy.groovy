package org.rackspace.deproxy

import java.util.concurrent.locks.ReentrantLock

import groovy.util.logging.Log4j

/**
 * The main class.
 */
@Log4j
class Deproxy {

    public static final String REQUEST_ID_HEADER_NAME = "Deproxy-Request-ID";
    public static final String VERSION = getVersion()
    public static final String VERSION_STRING = String.format("deproxy %s", VERSION);

    public def defaultHandler = null
    public def defaultClientConnector

    protected final def messageChainsLock = new ReentrantLock()
    protected Map<String, MessageChain> messageChains = [:]
    protected final def endpointLock = new ReentrantLock()
    protected List<DeproxyEndpoint> endpoints = []

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

    Deproxy(defaultHandler=null, ClientConnector defaultClientConnector=null) {

        if (defaultClientConnector == null) {
            defaultClientConnector = new DefaultClientConnector()
        }

        this.defaultHandler = defaultHandler;
        this.defaultClientConnector = defaultClientConnector
    }

    public MessageChain makeRequest(Map params) {
        return makeRequest(
                params?.url,
                params?.host ?: "",
                params?.port,
                params?.method ?: "GET",
                params?.path ?: "",
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
            String host="",
            port=null,
            String method="GET",
            String path="",
            headers=null,
            requestBody="",
            defaultHandler=null,
            Map handlers=null,
            boolean addDefaultHeaders=true,
            boolean chunked=false,
            ClientConnector clientConnector=null) {

        // url --> https host port path
        // https host port --> connection
        // method path headers requestBody --> request

        // specifying the path param overrides the path in the url param

        log.debug "begin makeRequest"

        headers = new HeaderCollection(headers)

        if (!clientConnector) {
            clientConnector = this.defaultClientConnector;
        }

        def requestId = UUID.randomUUID().toString()

        if (!headers.contains(REQUEST_ID_HEADER_NAME)) {
            headers.add(REQUEST_ID_HEADER_NAME, requestId)
        }

        def messageChain = new MessageChain(defaultHandler, handlers)
        addMessageChain(requestId, messageChain)

        boolean https = false
        if ((!host || !path || port == null) && url) {

            def uri = new URI(url)

            if (!host) {
                host = uri.host
            }

            if (port == null) {
                port = uri.port
            }

            https = (uri.scheme == 'https');

            if (!path) {
                URI uri2 = new URI(uri.scheme, uri.authority, null, null, null)
                path = uri2.relativize(uri).toString()
                if (!path.startsWith("/")) {
                    path = "/" + path
                }
            }
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

    DeproxyEndpoint addEndpoint(int port, String name=null, String hostname=null, defaultHandler=null) {

        def endpoint = null

        synchronized (this.endpointLock) {

            if (name == null) {
                name = String.format("Endpoint-%d", this.endpoints.size())
            }

            endpoint = new DeproxyEndpoint(this, port, name, hostname, defaultHandler)

            this.endpoints.add(endpoint)

            return endpoint
        }
    }

    boolean  _removeEndpoint(DeproxyEndpoint endpoint) {

        synchronized (this.endpointLock) {

            def count = this.endpoints.size()

            this.endpoints = this.endpoints.findAll { e -> e != endpoint }

            return (count != this.endpoints.size())
        }
    }

    void shutdown() {

        synchronized (this.endpointLock) {
            for (e in this.endpoints) {
                e.shutdown()
            }
            this.endpoints = []
        }
    }

    void addMessageChain(String requestId, MessageChain messageChain) {

        synchronized (this.messageChainsLock) {

            this.messageChains[requestId] = messageChain
        }
    }

    void removeMessageChain(String requestId) {

        synchronized (this.messageChainsLock) {

            this.messageChains.remove(requestId)
        }
    }

    MessageChain getMessageChain(String requestId) {

        synchronized (this.messageChainsLock) {

            if (this.messageChains.containsKey(requestId)) {

                return this.messageChains[requestId]

            } else {

                return null
            }
        }
    }

    void addOrphanedHandling(Handling handling) {

        synchronized (this.messageChainsLock) {

            for (mc in this.messageChains.values()) {

                mc.addOrphanedHandling(handling)
            }
        }
    }

}