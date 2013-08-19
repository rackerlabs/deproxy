package org.rackspace.gdeproxy

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
class StaticTcpServer {

    def static handleOneRequest(Socket socket, String responseString, int requestLength) {

        byte[] bytes = new byte[requestLength]
        int n = 0;
        while (n < bytes.length) {
            def count = socket.inputStream.read(bytes, n, bytes.length - n)
            n += count
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes)
        CharBuffer cb = Charset.forName("US-ASCII").decode(bb)
        def serverSideRequest = cb.toString()


        bytes = new byte[responseString.length()]
        Charset.forName("US-ASCII").encode(responseString).get(bytes)
        socket.outputStream.write(bytes)
        socket.outputStream.flush()

        return  serverSideRequest
    }
}
