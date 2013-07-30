/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.gdeproxy

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author richard-sartor
 */
class BodiesTest {

  Deproxy deproxy;
  int port;
  String url;

  //class TestBodies(unittest.TestCase):
  //    def setUp(self):
  @Before
  void setUp() {
    //        self.deproxy = deproxy.Deproxy()
    //        self.port = get_next_deproxy_port()
    //        self.deproxy.add_endpoint(self.port)
    //        self.url = 'http://localhost:{0}/'.format(self.port)
    //
    this.deproxy = new Deproxy();
    PortFinder pf = new PortFinder();
    this.port = pf.getNextOpenPort();
    this.url = "http://localhost:${this.port}/";
    this.deproxy.addEndpoint(this.port);
  }

  //    def test_request_body(self):
  @Test
  void testRequestBody() {
    def body = """ This is another body

        This is the next paragraph.
        """
    //        mc = self.deproxy.make_request(url=self.url, method='POST',
    //                                       request_body=body)
    def mc = this.deproxy.makeRequest(url: this.url, method: "POST", requestBody: body);
    //        self.assertEqual(mc.sent_request.body, body)
    //        self.assertEqual(len(mc.handlings), 1)
    //        self.assertEqual(mc.handlings[0].request.body, body)
    //
    assertEquals(1, mc.handlings.size());
    assertEquals(body, mc.sentRequest.body);
    assertEquals(body, mc.handlings[0].request.body);
  }

  //    def test_response_body(self):
  @Test
  void testResponseBody() {
    def body = """ This is another body

        This is the next paragraph.
        """
    //
    //        def custom_handler(request):
    //            return deproxy.Response(code=200, message='OK', headers=None,
    //                                    body=body)
    def handler = { request ->
      return new Response(200, "OK", ['Content-type': 'text/plain'], body);
    }
    //        mc = self.deproxy.make_request(url=self.url,
    //                                       default_handler=custom_handler)
    def mc = this.deproxy.makeRequest(url: this.url, defaultHandler: handler);
    //        self.assertEqual(mc.received_response.body, body)
    //        self.assertEqual(len(mc.handlings), 1)
    //        self.assertEqual(mc.handlings[0].response.body, body)
    assertEquals(1, mc.handlings.size());
    assertEquals(body, mc.handlings[0].response.body);
    assertEquals(body, mc.receivedResponse.body);
    //
  }

  //    @unittest.expectedFailure
  //    def test_request_body_chunked(self):
  //        self.fail()
  //
  //    @unittest.expectedFailure
  //    def test_response_body_chunked(self):
  //        self.fail()
  //

  //    def tearDown(self):
  @After
  void tearDown() {
    //        self.deproxy.shutdown_all_endpoints()
    if (this.deproxy) {
      this.deproxy.shutdown();
    }
  }
  //
}