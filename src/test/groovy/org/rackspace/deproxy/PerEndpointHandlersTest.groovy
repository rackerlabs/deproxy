/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.deproxy

/**
 *
 * @author richard-sartor
 */
class PerEndpointHandlersTest {

}


//class TestPerEndpointHandlers(unittest.TestCase):
//    def setUp(self):
//        self.deproxy = deproxy.Deproxy()
//        self.endpoint1 = self.deproxy.add_endpoint(
//            name='test-endpoint-1',
//            port=get_next_deproxy_port())
//        self.endpoint2 = self.deproxy.add_endpoint(
//            name='test-endpoint-2',
//            port=get_next_deproxy_port())
//
//        def custom_handler1(request):
//            return deproxy.Response(code='605', message='Custom', headers={},
//                                    body=None)
//
//        def custom_handler2(request):
//            return deproxy.Response(code='606', message='Spoiler', headers={},
//                                    body=None)
//        self.custom_handler1 = custom_handler1
//        self.custom_handler2 = custom_handler2
//        self.url1 = 'http://localhost:{0}/'.format(self.endpoint1.port)
//        self.url2 = 'http://localhost:{0}/'.format(self.endpoint2.port)
//
//    def test_no_handlers(self):
//        mc = self.deproxy.make_request(url=self.url1)
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '200')
//        self.assertEqual(mc.received_response.code, '200')
//
//        mc = self.deproxy.make_request(url=self.url2)
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '200')
//        self.assertEqual(mc.received_response.code, '200')
//
//    def test_empty_handlers(self):
//        mc = self.deproxy.make_request(url=self.url1, handlers={})
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '200')
//        self.assertEqual(mc.received_response.code, '200')
//
//        mc = self.deproxy.make_request(url=self.url2, handlers={})
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '200')
//        self.assertEqual(mc.received_response.code, '200')
//
//    def test_both_handlers(self):
//        handlers = {self.endpoint1: self.custom_handler1,
//                    self.endpoint2: self.custom_handler2}
//        mc = self.deproxy.make_request(url=self.url1, handlers=handlers)
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '605')
//        self.assertEqual(mc.received_response.code, '605')
//
//        mc = self.deproxy.make_request(url=self.url2, handlers=handlers)
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '606')
//        self.assertEqual(mc.received_response.code, '606')
//
//    def test_one_handler(self):
//        handlers = {self.endpoint1: self.custom_handler1}
//        mc = self.deproxy.make_request(url=self.url1, handlers=handlers)
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '605')
//        self.assertEqual(mc.received_response.code, '605')
//
//        mc = self.deproxy.make_request(url=self.url2, handlers=handlers)
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '200')
//        self.assertEqual(mc.received_response.code, '200')
//
//    def test_handlers_by_name(self):
//        handlers = {'test-endpoint-1': self.custom_handler1,
//                    'test-endpoint-2': self.custom_handler2}
//        mc = self.deproxy.make_request(url=self.url1, handlers=handlers)
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '605')
//        self.assertEqual(mc.received_response.code, '605')
//
//        mc = self.deproxy.make_request(url=self.url2, handlers=handlers)
//        self.assertEqual(len(mc.handlings), 1)
//        self.assertEqual(mc.handlings[0].response.code, '606')
//        self.assertEqual(mc.received_response.code, '606')
//
//    def tearDown(self):
//        self.deproxy.shutdown_all_endpoints()
//
//