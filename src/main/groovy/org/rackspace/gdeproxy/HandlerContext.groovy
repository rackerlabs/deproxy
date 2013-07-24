package org.rackspace.gdeproxy

/**
 * Created with IntelliJ IDEA.
 * User: rich4632
 * Date: 7/24/13
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */
class HandlerContext {

    boolean requestWasChunked;

    boolean sendChunkedResponse = false;
    boolean sendDefaultResponseHeaders = false;
}
