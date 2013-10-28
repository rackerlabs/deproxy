package org.rackspace.deproxy


interface Handler {

  public abstract Request handleRequest(Request request);

}
