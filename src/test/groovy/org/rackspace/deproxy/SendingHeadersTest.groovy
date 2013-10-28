/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.deproxy

/**
 *
 * @author richard-sartor
 */
class SendingHeadersTest {

}


//class TestSendingHeaders(unittest.TestCase):
//    def setUp(self):
//        self.deproxy = deproxy.Deproxy()
//        self.port = get_next_deproxy_port()
//        self.deproxy.add_endpoint(self.port)
//        self.url = 'http://localhost:{0}/'.format(self.port)
//
//    def test_send_duplicate_request_headers(self):
//        headers = deproxy.HeaderCollection()
//        headers.add('Name', 'Value1')
//        headers.add('Name', 'Value2')
//        mc = self.deproxy.make_request(url=self.url, headers=headers)
//        self.assertEqual(len(mc.handlings), 1)
//        values = [value for value in
//                  mc.handlings[0].request.headers.find_all('Name')]
//        self.assertEqual(values, ['Value1', 'Value2'])
//
//    def test_send_duplicate_response_headers(self):
//        def custom_handler(request):
//            headers = deproxy.HeaderCollection()
//            headers.add('Name', 'Value1')
//            headers.add('Name', 'Value2')
//            return deproxy.Response(code=200, message='OK', headers=headers,
//                                    body=None)
//
//        mc = self.deproxy.make_request(url=self.url,
//                                       default_handler=custom_handler)
//
//        self.assertEqual(len(mc.handlings), 1)
//        values = [value for value in
//                  mc.received_response.headers.find_all('Name')]
//        self.assertEqual(values, ['Value1', 'Value2'])
//
//    def tearDown(self):
//        self.deproxy.shutdown_all_endpoints()
//
//