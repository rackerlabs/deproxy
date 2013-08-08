/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.gdeproxy;

import org.junit.*;
import static org.junit.Assert.*;

class DelayHandlerTest {

  @Test
  void testDelayHandler() {
    def handler = Handlers.Delay(3000);

    Request request = new Request("GET", '/');

    def t1 = System.currentTimeMillis();
    Response response = handler(request);
    def t2 = System.currentTimeMillis();


    assertEquals("200", response.code);
    assertTrue(t2 - t1 >= 3000);
    assertTrue(t2 - t1 <= 3250);
  }

  @Test
  void testDelayHandlerWithNextHandler() {

    def handler = Handlers.Delay(3000, { request -> new Response(606, "Something") });

    Request request = new Request("GET", '/');

    def t1 = System.currentTimeMillis();
    Response response = handler(request);
    def t2 = System.currentTimeMillis();


    assertEquals("606", response.code);
    assertTrue(t2 - t1 >= 3000);
    assertTrue(t2 - t1 <= 3250);
  }
}


//class TestDelayHandler(unittest.TestCase):
//    def setUp(self):
//        self.deproxy_port = get_next_deproxy_port()
//        self.deproxy = deproxy.Deproxy()
//        self.end_point = self.deproxy.add_endpoint(self.deproxy_port)
//
//    def tearDown(self):
//        self.deproxy.shutdown_all_endpoints()
//
//    def test_delay_handler(self):
//        handler = deproxy.delay(3, deproxy.simple_handler)
//        t1 = time.time()
//        mc = self.deproxy.make_request('http://localhost:%i/' %
//                                       self.deproxy_port,
//                                       default_handler=handler)
//        t2 = time.time()
//        self.assertEquals(int(mc.received_response.code), 200)
//        self.assertGreaterEqual(t2 - t1, 3)
//        self.assertLessEqual(t2 - t1, 3.5)
//
//