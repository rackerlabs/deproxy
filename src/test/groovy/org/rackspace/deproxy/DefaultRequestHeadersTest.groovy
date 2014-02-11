
package org.rackspace.deproxy


import org.junit.*;
import static org.junit.Assert.*;

class DefaultRequestHeadersTest {

    def _port
    def _deproxy
    def _endpoint
    def _url

    @Before
    void setUp() {
        _port = PortFinder.Singleton.getNextOpenPort()
        _deproxy = new Deproxy()
        _endpoint = _deproxy.addEndpoint(_port)
        _url = String.format("http://localhost:%d/", _port)
    }

    @Test
    void testNotSpecified() {
        def mc = _deproxy.makeRequest(_url);
        assertTrue(mc.sentRequest.headers.contains("Host"));
        assertTrue(mc.sentRequest.headers.contains("Accept"));
        assertTrue(mc.sentRequest.headers.contains("Accept-Encoding"));
        assertTrue(mc.sentRequest.headers.contains("User-Agent"));
    }

    @Test
    void testExplicitOn() {
        def mc = _deproxy.makeRequest(url: _url, addDefaultHeaders: true);
        assertTrue(mc.sentRequest.headers.contains("Host"));
        assertTrue(mc.sentRequest.headers.contains("Accept"));
        assertTrue(mc.sentRequest.headers.contains("Accept-Encoding"));
        assertTrue(mc.sentRequest.headers.contains("User-Agent"));
    }

    @Test
    void testExplicitOff() {
        def mc = _deproxy.makeRequest(url: _url, addDefaultHeaders: false);
        assertFalse("host header exists", mc.sentRequest.headers.contains("Host"));
        assertFalse("accept header exists", mc.sentRequest.headers.contains("Accept"));
        assertFalse("accept encoding exists", mc.sentRequest.headers.contains("Accept-Encoding"));
        assertFalse("user agent exists", mc.sentRequest.headers.contains("User-Agent"));
    }

    @After
    void tearDown() {
        if (_deproxy) {
            _deproxy.shutdown();
        }
    }
}