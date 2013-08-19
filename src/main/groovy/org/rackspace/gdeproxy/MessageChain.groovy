package org.rackspace.gdeproxy

/**
 *  An object containing the initial request sent via the make_request method,
 *  and all request/response pairs (Handling objects) processed by
 *  DeproxyEndpoint objects.
 */

class MessageChain {

    Request sentRequest
    Response receivedResponse
    def defaultHandler
    def handlers = [:]
    List<Handling> handlings = []
    List<Handling> orphanedHandlings = []

    protected def lock = new Object()

    public MessageChain(defaultHandler = null, handlers = null) {
        this.defaultHandler = defaultHandler
        this.handlers = handlers
    this.defaultHandler = defaultHandler;
    this.handlers = handlers;
    }

    def addHandling(handling) {
        synchronized (this.lock) {
            this.handlings.add(handling)
        }
    }

    def addOrphanedHandling(handling) {
        synchronized (this.lock) {
            this.orphanedHandlings.add(handling)
        }
    }
    String toString() {
        sprintf('MessageChain(default_handler= %s sent_request=%s, handlings=%s, received_response=%s, orphaned_handlings=%s)',
                defaultHandler, sentRequest, handlings, receivedResponse, orphanedHandlings)
    }
}
