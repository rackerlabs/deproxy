/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.gdeproxy
import groovy.util.logging.Log4j
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertEquals;


@Log4j
public class DefaultHandlerTest {

  int _port;
  Deproxy _deproxy;
  DeproxyEndpoint _endpoint;

  @Before
  public void setUp() {
    log.debug("creating a deproxy")
    _deproxy = new Deproxy();

    log.debug "getting a port"
    def finder = new PortFinder()
    _port = finder.getNextOpenPort();

    log.debug "creating the endpoint"
    _endpoint = _deproxy.addEndpoint(_port);

    log.debug "setUp done"
  }

    @Ignore
  @Test
  public void TestDefaultHandler(){
    log.debug "making request"
    def mc = _deproxy.makeRequest(String.format("http://localhost:%d/", _port));
    log.debug "making assertion"
    assertEquals(mc.receivedResponse.code, "200");
    log.debug "test done"
  }

  @After
  public void tearDown(){
    log.debug "shutting down"
    _deproxy.shutdown()
    log.debug "tearDown done"
  }
}