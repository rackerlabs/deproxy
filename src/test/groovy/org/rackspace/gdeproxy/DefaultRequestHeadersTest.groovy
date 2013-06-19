/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.gdeproxy

/**
 *
 * @author richard-sartor
 */

import org.junit.*;
import static org.junit.Assert.*;

class DefaultRequestHeadersTest {

  def _port
  def _deproxy
  def _endpoint
  def _url

  @Before
  void setUp() {
    //    def setUp(self):
    //        self.port = get_next_deproxy_port()
    PortFinder pf = new PortFinder()
    _port = pf.getNextOpenPort()
    //        self.deproxy = deproxy.Deproxy()
    _deproxy = new Deproxy()
    //        self.endpoint = self.deproxy.add_endpoint(self.port)
    _endpoint = _deproxy.addEndpoint(_port)
    //        self.url = 'http://localhost:{}/'.format(self.port)
    _url = String.format("http://localhost:%d/", _port)
    //
  }
  //    def tearDown(self):
  //        if self.deproxy is not None:
  //            self.deproxy.shutdown_all_endpoints()
  //
  //    def test_not_specified(self):
  //        mc = self.deproxy.make_request(url=self.url)
  //        self.assertIn('Host', mc.sent_request.headers)
  //        #self.assertIn('host', mc.sent_request.headers)
  //        self.assertIn('Accept', mc.sent_request.headers)
  //        self.assertIn('Accept-Encoding', mc.sent_request.headers)
  //        self.assertIn('User-Agent', mc.sent_request.headers)
  @Test
  void testNotSpecified() {
    def mc = _deproxy.makeRequest(_url);
    assertTrue(mc.sentRequest.headers.contains("Host"));
    assertTrue(mc.sentRequest.headers.contains("Accept"));
    assertTrue(mc.sentRequest.headers.contains("Accept-Encoding"));
    assertTrue(mc.sentRequest.headers.contains("User-Agent"));
  }
  //
  //    def test_explicit_on(self):
  //        mc = self.deproxy.make_request(url=self.url, add_default_headers=True)
  //        self.assertIn('Host', mc.sent_request.headers)
  //        #self.assertIn('host', mc.sent_request.headers)
  //        self.assertIn('Accept', mc.sent_request.headers)
  //        self.assertIn('Accept-Encoding', mc.sent_request.headers)
  //        self.assertIn('User-Agent', mc.sent_request.headers)
  //
  @Test
  void testExplicitOn() {
    def mc = _deproxy.makeRequest(url: _url, addDefaultHeaders: true);
    assertTrue(mc.sentRequest.headers.contains("Host"));
    assertTrue(mc.sentRequest.headers.contains("Accept"));
    assertTrue(mc.sentRequest.headers.contains("Accept-Encoding"));
    assertTrue(mc.sentRequest.headers.contains("User-Agent"));
  }
  //    def test_explicit_off(self):
  //        mc = self.deproxy.make_request(url=self.url, add_default_headers=False)
  //        self.assertNotIn('Host', mc.sent_request.headers)
  //        #self.assertNotIn('host', mc.sent_request.headers)
  //        self.assertNotIn('Accept', mc.sent_request.headers)
  //        self.assertNotIn('Accept-Encoding', mc.sent_request.headers)
  //        self.assertNotIn('User-Agent', mc.sent_request.headers)
  //
  //
  @Ignore
  @Test
  void testExplicitOff() {
    def mc = _deproxy.makeRequest(url: _url, addDefaultHeaders: false);
    assertFalse(mc.sentRequest.headers.contains("Host"));
    assertFalse(mc.sentRequest.headers.contains("Accept"));
    assertFalse(mc.sentRequest.headers.contains("Accept-Encoding"));
    assertFalse(mc.sentRequest.headers.contains("User-Agent"));
  }
  
  @After
  void tearDown() {
    if (_deproxy) {
      _deproxy.shutdown();
    }
  }
}