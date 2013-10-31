/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.deproxy

/**
 *
 * @author richard-sartor
 */
class EndpointDefaultHandlerTest {

}


//class TestEndpointDefaultHandler(unittest.TestCase):
//    def setUp(self):
//        self.port = get_next_deproxy_port()
//        self.deproxy = deproxy.Deproxy()
//
//    def test_endpoint_default_handler_function(self):
//        def custom_handler(request):
//            return deproxy.Response(code='601', message='Custom', headers={},
//                                    body=None)
//        self.deproxy.add_endpoint(port=self.port,
//                                  default_handler=custom_handler)
//        url = 'http://localhost:{0}/'.format(self.port)
//        mc = self.deproxy.make_request(url=url)
//
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '601')
//        self.assertEqual(mc.received_response.code, '601')
//
//    def custom_handler_method(self, request):
//        return deproxy.Response(code='602', message='Custom', headers={},
//                                body=None)
//
//    def test_endpoint_default_handler_method(self):
//        self.deproxy.add_endpoint(port=self.port,
//                                  default_handler=self.custom_handler_method)
//        url = 'http://localhost:{0}/'.format(self.port)
//        mc = self.deproxy.make_request(url=url)
//
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '602')
//        self.assertEqual(mc.received_response.code, '602')
//
//    def tearDown(self):
//        self.deproxy.shutdown_all_endpoints()
//
//