package org.rackspace.deproxy

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
class StaticTcpServer {

    static String handleOneRequest(Socket socket, String responseString, int requestLength) {

        byte[] bytes = new byte[requestLength]
        int n = 0;
        while (n < bytes.length) {
            def count = socket.inputStream.read(bytes, n, bytes.length - n)
            n += count
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes)
        CharBuffer cb = Charset.forName("US-ASCII").decode(bb)
        String serverSideRequest = cb.toString()


        bytes = new byte[responseString.length()]
        Charset.forName("US-ASCII").encode(responseString).get(bytes)
        socket.outputStream.write(bytes)
        socket.outputStream.flush()

        return serverSideRequest
    }

    def static handleOneRequestTimeout(Socket socket, String responseString, int timeoutMillis) {

        int oldTimeout = socket.soTimeout
        long startTimeMillis = System.currentTimeMillis()

        List<Byte> bytes = []

        try {

            socket.soTimeout = 100

            while (true) {
                try {

                    def value = socket.inputStream.read()
                    if (value >= 0) {
                        bytes.add(value as byte)
                    }

                } catch (SocketTimeoutException ignored) {

                    long currentTimeMillis = System.currentTimeMillis()

                    if (currentTimeMillis >= startTimeMillis + timeoutMillis) {
                        break;
                    }
                }
            }

        } finally {

            if (socket &&
                    !socket.isClosed()) {
                socket.soTimeout = oldTimeout
            }
        }


        ByteBuffer bb = ByteBuffer.wrap(bytes as byte[])
        CharBuffer cb = Charset.forName("US-ASCII").decode(bb)
        def serverSideRequest = cb.toString()


        byte[] bytesOut = new byte[responseString.length()]
        Charset.forName("US-ASCII").encode(responseString).get(bytesOut)
        socket.outputStream.write(bytesOut)
        socket.outputStream.flush()

        return serverSideRequest
    }
}
