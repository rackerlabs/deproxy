/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.gdeproxy

import org.junit.*

import static org.junit.Assert.*

/**
 *
 * @author richard-sartor
 */
class OrphanedHandlingsTest {

    Deproxy deproxy
    DeproxyEndpoint endpoint

    // this just acts as another HTTP client to make requests with
    Deproxy otherClient

    @Before
    void setup() {
        this.deproxy = new Deproxy()
        PortFinder pf = new PortFinder()
        this.endpoint = this.deproxy.addEndpoint(pf.getNextOpenPort())
        this.otherClient = new Deproxy()
    }

    @Test
    void testOrphanedHandlings() {

        def handler = Handlers.Delay(2000)

        MessageChain mc
        def t = Thread.start {
            mc = this.deproxy.makeRequest(url: "http://localhost:${this.endpoint.port}/",
                                          defaultHandler: handler)
        }

        // the first request will take a few seconds to finish. during that
        // time, we'll make another request to the same endpoint from another
        // client. because it won't have a record of the Deproxy-Request-ID,
        // the other client's request will be orphaned from the perspective
        // of the first deproxy.

        MessageChain otherClientMc
        otherClientMc = this.otherClient.makeRequest(
                "http://localhost:${this.endpoint.port}/")

        t.join()

        assertEquals(1, mc.orphanedHandlings.size())
        assertEquals(1, mc.handlings.size())
        assertEquals(1, mc.orphanedHandlings[0].request.headers.getCountByName(Deproxy.REQUEST_ID_HEADER_NAME))
        assertEquals(0, otherClientMc.handlings.size())
        assertEquals(0, otherClientMc.orphanedHandlings.size())
        assertEquals(1, otherClientMc.sentRequest.headers.getCountByName(Deproxy.REQUEST_ID_HEADER_NAME))
        assertEquals(mc.orphanedHandlings[0].request.headers[Deproxy.REQUEST_ID_HEADER_NAME],
                     otherClientMc.sentRequest.headers[Deproxy.REQUEST_ID_HEADER_NAME])
    }

    @After
    void cleanup() {

        if (this.deproxy) {
            this.deproxy.shutdown()
        }

        if (this.otherClient) {
            this.otherClient.shutdown()
        }
    }

}


