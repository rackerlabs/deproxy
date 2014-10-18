
package org.rackspace.deproxy

import org.junit.Test

import static junit.framework.Assert.assertEquals

class LocalSocketPairTest {

    @Test
    void testLocalSocketPairCreation() {

        def (Socket client, Socket server) = LocalSocketPair.createLocalSocketPair()



        PrintWriter writer = new PrintWriter(client.outputStream)
        UnbufferedStreamReader reader = new UnbufferedStreamReader(server.inputStream)

        writer.println("asdf")
        writer.flush()
        def asdf = LineReader.readLine(reader)

        assertEquals(asdf, "asdf")



        writer = new PrintWriter(server.outputStream)
        reader = new UnbufferedStreamReader(client.inputStream)

        writer.println("qwerty")
        writer.flush()
        def qwerty = LineReader.readLine(reader)

        assertEquals(qwerty, "qwerty")

        client.close()
        server.close()
    }

    @Test
    void testLocalSocketPairCreationWithPortFinder() {

        PortFinder pf = new PortFinder()

        pf.getNextOpenPort(12346)
        pf.getNextOpenPort()
        pf.getNextOpenPort()

        def (Socket client, Socket server) = LocalSocketPair.createLocalSocketPair(pf)

        assertEquals(pf.currentPort, client.port)
        assertEquals(pf.currentPort, server.localPort)



        PrintWriter writer = new PrintWriter(client.outputStream)
        UnbufferedStreamReader reader = new UnbufferedStreamReader(server.inputStream)

        writer.println("asdf")
        writer.flush()
        def asdf = LineReader.readLine(reader)

        assertEquals(asdf, "asdf")



        writer = new PrintWriter(server.outputStream)
        reader = new UnbufferedStreamReader(client.inputStream)

        writer.println("qwerty")
        writer.flush()
        def qwerty = LineReader.readLine(reader)

        assertEquals(qwerty, "qwerty")

        client.close()
        server.close()
    }
}
