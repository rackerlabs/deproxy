package org.rackspace.gdeproxy


interface Handler {

  public abstract Request handleRequest(Request request);

}
