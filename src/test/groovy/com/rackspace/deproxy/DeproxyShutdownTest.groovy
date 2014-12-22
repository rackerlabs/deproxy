
package com.rackspace.deproxy

import org.junit.*


class DeproxyShutdownTest {

    Deproxy deproxy

    @Test
    void testShutdown() {

        /*
         *  When a Deproxy shuts down, all of its endpoints are shut down and
         *  removed, which means that the ports they were using should be available
         *  again.
         *
         */

        def port1 = PortFinder.Singleton.getNextOpenPort();
        def port2 = PortFinder.Singleton.getNextOpenPort();

        deproxy = new Deproxy();

        def e1 = deproxy.addEndpoint(port1)
        def e2 = deproxy.addEndpoint(port2)

        deproxy.shutdown()

        sleep(1000)

        try {

            def e3 = deproxy.addEndpoint(port1)
            Assert.assertNotNull(e3)

        } catch (IOException e) {
            Assert.fail("addEndpoint threw an exception")
        }

        try {

            def e4 = deproxy.addEndpoint(port2)
            Assert.assertNotNull(e4)

        } catch (IOException e) {
            Assert.fail("addEndpoint threw an exception")
        }
    }

    @After
    void tearDown() {

        if (deproxy) {
            deproxy.shutdown()
        }
    }
}
