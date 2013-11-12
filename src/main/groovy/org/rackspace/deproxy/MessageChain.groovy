package org.rackspace.deproxy

/**
 *  An object containing the initial request sent via the make_request method,
 *  and all request/response pairs (Handling objects) processed by
 *  DeproxyEndpoint objects.
 */

class MessageChain {

    Request sentRequest
    Response receivedResponse
    def defaultHandler
    Map handlers = [:]
    List<Handling> handlings = []
    List<Handling> orphanedHandlings = []

    protected def lock = new Object()

    public MessageChain(defaultHandler = null, Map handlers = null) {
        this.defaultHandler = defaultHandler
        this.handlers = handlers
    }

    void addHandling(Handling handling) {
        synchronized (this.lock) {
            this.handlings.add(handling)
        }
    }

    void addOrphanedHandling(Handling handling) {
        synchronized (this.lock) {
            this.orphanedHandlings.add(handling)
        }
    }
    String toString() {
        sprintf('MessageChain(default_handler= %s sent_request=%s, handlings=%s, received_response=%s, orphaned_handlings=%s)',
                defaultHandler, sentRequest, handlings, receivedResponse, orphanedHandlings)
    }
}
