/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.deproxy

/**
 *
 * @author richard-sartor
 */
class DeproxyDefaultHandlerTest {

}


//class TestDeproxyDefaultHandler(unittest.TestCase):
//    def setUp(self):
//        self.port = get_next_deproxy_port()
//
//    def test_deproxy_default_handler_function(self):
//        def custom_handler(request):
//            return deproxy.Response(code='603', message='Custom', headers={},
//                                    body=None)
//        self.deproxy = deproxy.Deproxy(default_handler=custom_handler)
//        self.deproxy.add_endpoint(port=self.port)
//
//        url = 'http://localhost:{0}/'.format(self.port)
//        mc = self.deproxy.make_request(url=url)
//
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '603')
//        self.assertEqual(mc.received_response.code, '603')
//
//    def custom_handler_method(self, request):
//        return deproxy.Response(code='604', message='Custom', headers={},
//                                body=None)
//
//    def test_deproxy_default_handler_method(self):
//        self.deproxy = deproxy.Deproxy(
//            default_handler=self.custom_handler_method)
//        self.deproxy.add_endpoint(port=self.port)
//
//        url = 'http://localhost:{0}/'.format(self.port)
//        mc = self.deproxy.make_request(url=url)
//
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '604')
//        self.assertEqual(mc.received_response.code, '604')
//
//    def tearDown(self):
//        if hasattr(self, 'deproxy') and self.deproxy is not None:
//            self.deproxy.shutdown_all_endpoints()
//
//