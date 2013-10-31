/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.deproxy

/**
 *
 * @author richard-sartor
 */
class DefaultResponseHeadersTest {

}


//class TestDefaultResponseHeaders(unittest.TestCase):
//    @classmethod
//    def setUpClass(self):
//        self.port = get_next_deproxy_port()
//        self.deproxy = deproxy.Deproxy()
//        self.endpoint = self.deproxy.add_endpoint(self.port)
//        self.url = 'http://localhost:{}/'.format(self.port)
//
//    @classmethod
//    def tearDownClass(self):
//        if self.deproxy is not None:
//            self.deproxy.shutdown_all_endpoints()
//
//    def handler1(self, request):
//        return deproxy.Response(code=606, message="Spoiler",
//                                headers={"Header-Name": "Header-Value"},
//                                body='Snape Kills Dumbledore')
//
//    def handler2(self, request):
//        return (deproxy.Response(code=606, message="Spoiler",
//                                 headers={"Header-Name": "Header-Value"},
//                                 body='Snape Kills Dumbledore'), True)
//
//    def handler3(self, request):
//        return (deproxy.Response(code=606, message="Spoiler",
//                                 headers={"Header-Name": "Header-Value"},
//                                 body='Snape Kills Dumbledore'), False)
//
//    def test_not_specified(self):
//        mc = self.deproxy.make_request(url=self.url,
//                                       default_handler=self.handler1)
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertIn('server', mc.received_response.headers)
//        self.assertIn('date', mc.received_response.headers)
//        self.assertIn('Server', mc.handlings[0].response.headers)
//        self.assertIn('Date', mc.handlings[0].response.headers)
//
//    def test_explicit_on(self):
//        mc = self.deproxy.make_request(url=self.url,
//                                       default_handler=self.handler2)
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertIn('server', mc.received_response.headers)
//        self.assertIn('date', mc.received_response.headers)
//        self.assertIn('Server', mc.handlings[0].response.headers)
//        self.assertIn('Date', mc.handlings[0].response.headers)
//
//    def test_explicit_off(self):
//        mc = self.deproxy.make_request(url=self.url,
//                                       default_handler=self.handler3)
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertNotIn('server', mc.received_response.headers)
//        self.assertNotIn('date', mc.received_response.headers)
//        self.assertNotIn('server', mc.handlings[0].response.headers)
//        self.assertNotIn('date', mc.handlings[0].response.headers)
//        self.assertNotIn('Server', mc.received_response.headers)
//        self.assertNotIn('Date', mc.received_response.headers)
//        self.assertNotIn('Server', mc.handlings[0].response.headers)
//        self.assertNotIn('Date', mc.handlings[0].response.headers)
//
//