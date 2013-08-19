package org.rackspace.gdeproxy

class HandlerContext {

//    boolean requestWasChunked;

    boolean usedChunkedTransferEncoding = false;
    boolean sendDefaultResponseHeaders = true;
}
