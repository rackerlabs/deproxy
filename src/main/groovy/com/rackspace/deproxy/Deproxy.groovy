package com.rackspace.deproxy

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
    public DefaultClientConnector defaultClientConnector
    public BareClientConnector bareClientConnector

    protected final def messageChainsLock = new ReentrantLock()
    protected Map<String, MessageChain> messageChains = [:]
    protected final def endpointLock = new ReentrantLock()
    protected List<Endpoint> endpoints = []

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

    Deproxy(defaultHandler=null) {

        this.defaultHandler = defaultHandler
        this.bareClientConnector = new BareClientConnector()
        this.defaultClientConnector = new DefaultClientConnector(this.bareClientConnector)
    }

    public MessageChain makeRequest(Map params) {
        return makeRequest(
                params?.url as String,
                (params?.host ?: "") as String,
                params?.port,
                (params?.method ?: "GET") as String,
                (params?.path ?: "") as String,
                params?.headers,
                params?.requestBody ?: "",
                params?.defaultHandler,
                params?.handlers as Map,
                (params?.addDefaultHeaders == null ? true : params?.addDefaultHeaders) as boolean,
                (params?.chunked ? true : false) as boolean,
                (params?.clientConnector ?: null) as ClientConnector
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
            if (addDefaultHeaders) {
                clientConnector = this.defaultClientConnector
            } else {
                clientConnector = this.bareClientConnector
            }
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

        log.debug "calling sendRequest"
        Response response = clientConnector.sendRequest(request, https, host, port, requestParams)
        log.debug "back from sendRequest"

        removeMessageChain(requestId)

        messageChain.sentRequest = request
        messageChain.receivedResponse = response

        log.debug "end makeRequest"

        return messageChain
    }

    Endpoint addEndpoint(Map params) {
        return addEndpoint(
                params?.port as Integer,
                params?.name as String,
                params?.hostname as String,
                params?.defaultHandler as Closure,
                params?.connectorFactory as Closure<ServerConnector>
        );
    }
    Endpoint addEndpoint(Integer port=null, String name=null, String hostname=null,
                         Closure defaultHandler=null, Closure<ServerConnector> connectorFactory=null) {

        synchronized (this.endpointLock) {

            Endpoint endpoint =
                new Endpoint(
                        this,
                        port: port,
                        name: name,
                        hostname: hostname,
                        defaultHandler: defaultHandler,
                        connectorFactory: connectorFactory)

            this.endpoints.add(endpoint)

            return endpoint
        }
    }

    boolean  _removeEndpoint(Endpoint endpoint) {

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