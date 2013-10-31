package org.rackspace.deproxy

class HandlerContext {

//    boolean requestWasChunked;

    boolean usedChunkedTransferEncoding = false;
    boolean sendDefaultResponseHeaders = true;
}
