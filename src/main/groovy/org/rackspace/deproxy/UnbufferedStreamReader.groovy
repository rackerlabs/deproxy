package org.rackspace.deproxy

import java.nio.CharBuffer


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
