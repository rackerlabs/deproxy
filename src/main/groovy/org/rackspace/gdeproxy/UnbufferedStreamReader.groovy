package org.rackspace.gdeproxy

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
class UnbufferedStreamReader implements Readable {

    InputStream inStream

    public UnbufferedStreamReader(InputStream inputStream) {
        this.inStream = inputStream
    }

    int read() throws IOException {

        int value = inStream.read()

        if (value < 0) return -1

        return (byte)value
    }

    @Override
    int read(CharBuffer charBuffer) throws IOException {
        throw new UnsupportedOperationException()
    }
}
