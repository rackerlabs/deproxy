package com.rackspace.deproxy

import org.junit.*;
import static org.junit.Assert.*;


class EchoHandlerTest {

    @Test
    public void testEchoHandler() {

        def response = Handlers.echoHandler(new Request("GET", "/", ["x-header": 12345], "this is the body"));

        assertEquals("200", response.code);
        assertTrue(response.headers.contains("x-header"));
        assertEquals("12345", response.headers.getFirstValue("x-header"));
        assertEquals("this is the body", response.body);
    }
}
