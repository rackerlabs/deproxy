package org.rackspace.deproxy

import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*


class ConnectionReuseTest {

    Deproxy deproxy;
    int port;
    String url;
    ApacheClientConnector client

    @Before
    void setUp() {

        // HttpClient re-uses connection by default
        // we don't need to roll our own code to create and re-use connections
        client = new ApacheClientConnector()

        this.deproxy = new Deproxy(null, client);

        this.port = PortFinder.Singleton.getNextOpenPort();
        this.url = "http://localhost:${this.port}/";

        this.deproxy.addEndpoint(this.port);
    }

    @Test
    void testServerSideConnectionReuse() {

        def mc1 = deproxy.makeRequest(url: url)
        assertEquals(1, mc1.handlings.size())

        def mc2 = deproxy.makeRequest(url: url)
        assertEquals(1, mc2.handlings.size())

        assertEquals(mc1.handlings[0].connection, mc2.handlings[0].connection)
    }

    @After
    void tearDown() {
        if (this.deproxy) {
            this.deproxy.shutdown();
        }
    }
}
