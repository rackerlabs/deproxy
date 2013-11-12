/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.deproxy;

import org.junit.*;
import static org.junit.Assert.*;

class DelayHandlerTest {

    @Test
    void testDelayHandler() {
        def handler = Handlers.Delay(3000);

        Request request = new Request("GET", '/');

        def t1 = System.currentTimeMillis();
        Response response = handler(request);
        def t2 = System.currentTimeMillis();


        assertEquals("200", response.code);
        assertTrue(t2 - t1 >= 3000);
        assertTrue(t2 - t1 <= 3250);
    }

    @Test
    void testDelayHandlerWithNextHandler() {

        def handler = Handlers.Delay(3000, { request -> new Response(606, "Something") });

        Request request = new Request("GET", '/');

        def t1 = System.currentTimeMillis();
        Response response = handler(request);
        def t2 = System.currentTimeMillis();


        assertEquals("606", response.code);
        assertTrue(t2 - t1 >= 3000);
        assertTrue(t2 - t1 <= 3250);
    }
}
