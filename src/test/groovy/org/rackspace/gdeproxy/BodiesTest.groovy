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

    @Test
    void testDefaultRequestHeadersForTextBody() {
        def body = """ This is another body

        This is the next paragraph.
        """

        def mc = this.deproxy.makeRequest(url: this.url, method: "POST",
                                          requestBody: body);

        assertEquals(body, mc.sentRequest.body);
        assertEquals(1, mc.sentRequest.headers.getCountByName("Content-Type"))
        assertEquals("text/plain", mc.sentRequest.headers["Content-Type"])
        assertEquals(1, mc.handlings.size());
        assertEquals(body, mc.handlings[0].request.body);
        assertEquals(1, mc.handlings[0].request.headers.getCountByName("Content-Type"))
        assertEquals("text/plain", mc.handlings[0].request.headers["Content-Type"])
    }

    @Test
    void testNoDefaultRequestHeadersForTextBody() {
        def body = """ This is another body

        This is the next paragraph.
        """

        def mc = this.deproxy.makeRequest(url: this.url, method: "POST",
                                          requestBody: body,
                                          addDefaultHeaders: false);

        assertEquals(body, mc.sentRequest.body);
        assertEquals(0, mc.sentRequest.headers.getCountByName("Content-Type"))
        assertEquals(1, mc.handlings.size());
        assertEquals("", mc.handlings[0].request.body); // because there's no Content-Length header, it doesn't read the body
        assertEquals(0, mc.handlings[0].request.headers.getCountByName("Content-Type"))
    }

    @Test
    void testDefaultResponseHeadersForBinaryBody() {
        def body = """ This is another body

        This is the next paragraph.
        """

        def handler = { request ->
            return new Response(200, "OK", null, body);
        }

        def mc = this.deproxy.makeRequest(url: this.url,
                defaultHandler: handler);

        assertEquals(body, mc.receivedResponse.body);
        assertEquals(1, mc.receivedResponse.headers.getCountByName("Content-Type"))
        assertEquals("text/plain", mc.receivedResponse.headers["Content-Type"])
        assertEquals(1, mc.handlings.size());
        assertEquals(body, mc.handlings[0].response.body);
        assertEquals(1, mc.handlings[0].response.headers.getCountByName("Content-Type"))
        assertEquals("text/plain", mc.handlings[0].response.headers["Content-Type"])
    }

    // this one doesn't work correctly, because we don't have a reliable way
    // to turn of default response headers
    @Ignore
    @Test
    void testNoDefaultResponseHeadersForBinaryBody() {
        def body = """ This is another body

        This is the next paragraph.
        """

        def handler = { request ->
            return [ new Response(200, "OK", null, body), false ];
        }

        def mc = this.deproxy.makeRequest(url: this.url,
                defaultHandler: handler);

        assertEquals("", mc.receivedResponse.body);
        assertEquals(0, mc.receivedResponse.headers.getCountByName("Content-Type"))
        assertEquals(1, mc.handlings.size());
        assertEquals(body, mc.handlings[0].response.body); // because there's no Content-Length header, it doesn't read the body
        assertEquals(0, mc.handlings[0].response.headers.getCountByName("Content-Type"))
    }

  @After
  void tearDown() {
    //        self.deproxy.shutdown_all_endpoints()
    if (this.deproxy) {
      this.deproxy.shutdown();
    }
  }


}