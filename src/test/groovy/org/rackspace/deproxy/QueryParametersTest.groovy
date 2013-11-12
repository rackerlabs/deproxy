package org.rackspace.deproxy

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
class QueryParametersTest {

    Deproxy deproxy;
    int port;
    String url;

    @Before
    void setUp() {

        this.deproxy = new Deproxy();
        PortFinder pf = new PortFinder();
        this.port = pf.getNextOpenPort();
        this.url = "http://localhost:${this.port}/";
        this.deproxy.addEndpoint(this.port);
    }

    @Test
    void TestIncludesQueryParamsInRequestImplicitDefaultConnector() {

        def mc = this.deproxy.makeRequest(url: "${url}?a=b&c=d")

        Assert.assertEquals("/?a=b&c=d", mc.sentRequest.path)
        Assert.assertEquals(1, mc.handlings.size())
        Assert.assertEquals("/?a=b&c=d", mc.handlings[0].request.path)
    }

    @Test
    void TestIncludesQueryParamsInRequestExplicitDefaultConnector() {

        def mc = this.deproxy.makeRequest(url: "${url}?a=b&c=d", clientConnector: new DefaultClientConnector())

        Assert.assertEquals("/?a=b&c=d", mc.sentRequest.path)
        Assert.assertEquals(1, mc.handlings.size())
        Assert.assertEquals("/?a=b&c=d", mc.handlings[0].request.path)
    }

    @Test
    void TestIncludesQueryParamsInRequestBareConnector() {

        def mc = this.deproxy.makeRequest(url: "${url}?a=b&c=d", clientConnector: new BareClientConnector())

        Assert.assertEquals("/?a=b&c=d", mc.sentRequest.path)
        Assert.assertEquals(1, mc.handlings.size())
        Assert.assertEquals("/?a=b&c=d", mc.handlings[0].request.path)
    }

    // TODO: Test for non-ascii characters, illegal characters, escaping, etc.

    @After
    void tearDown() {
        if (this.deproxy) {
            this.deproxy.shutdown();
        }
    }

}
