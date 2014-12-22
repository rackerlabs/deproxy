package com.rackspace.deproxy

import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*


class ConnectionReuseTest {

    Deproxy deproxy;
    int port;
    String url;
    ClientConnector client;

    @Before
    void setUp() {

        this.deproxy = new Deproxy();

        this.port = PortFinder.Singleton.getNextOpenPort();
        this.url = "http://localhost:${this.port}/";

        def endpoint = this.deproxy.addEndpoint(this.port);

        def socket = (endpoint.serverConnector as SocketServerConnector).createRawConnection()
        client = new BareClientConnector(socket)

    }

    @Test
    void testServerSideConnectionReuse() {

        def mc1 = deproxy.makeRequest(url: url, clientConnector: client, headers: ['Content-length': '0'])
        assertEquals(1, mc1.handlings.size())

        def mc2 = deproxy.makeRequest(url: url, clientConnector: client, headers: ['Content-length': '0'])
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
