/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.deproxy

/**
 *
 * @author richard-sartor
 */
class EndpointShutdownTest {

}


//class TestEndpointShutdown(unittest.TestCase):
//    def setUp(self):
//        self.deproxy_port1 = get_next_deproxy_port()
//        self.deproxy_port2 = get_next_deproxy_port()
//        self.deproxy = deproxy.Deproxy()
//
//    def test_shutdown(self):
//        e1 = self.deproxy.add_endpoint(self.deproxy_port1)
//        e2 = self.deproxy.add_endpoint(self.deproxy_port2)
//
//        e1.shutdown()
//
//        try:
//            e3 = self.deproxy.add_endpoint(self.deproxy_port1)
//        except socket.error as e:
//            self.fail('Address already in use: %s' % e)
//
//