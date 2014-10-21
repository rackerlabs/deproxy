package org.rackspace.deproxy

import groovy.util.logging.Log4j

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetEncoder
import java.nio.charset.CodingErrorAction


@Log4j
class BodyWriter {
    def static void writeBody(body, OutputStream outStream, boolean chunked=false) {

        outStream.flush()

        if (body != null) {
            if (chunked) {

                writeBodyChunked(body, outStream)

            } else if (body instanceof String) {

                log.debug("sending string body, length ${body.length()}")
                log.debug(body)
                if (body.length() > 0) {
                    def bytes = body.getBytes("US-ASCII")
                    outStream.write(bytes)
                    outStream.flush()
                }

            } else if (body instanceof byte[]) {

                log.debug("sending binary body, length ${body.length}")
                log.debug(body.toString())
                if (body.length > 0) {
                    outStream.write(body)
                    outStream.flush()
                }

            } else {

                throw new UnsupportedOperationException("Unknown data type in message body")
            }

        } else {
            log.debug("No body to send");
        }

        outStream.flush();
    }

    def static void writeBodyChunked(body, OutputStream outStream) {

        // see rfc 2616, section 3.6.1

        Charset asciiCharset = Charset.forName("US-ASCII")
        CharsetEncoder asciiEncoder = asciiCharset.newEncoder()
        asciiEncoder.onMalformedInput(CodingErrorAction.REPORT)
        asciiEncoder.onUnmappableCharacter(CodingErrorAction.REPORT)

        ByteBuffer buffer
        if (body instanceof String) {

            buffer = asciiEncoder.encode(CharBuffer.wrap(body))

        } else if (body instanceof byte[]) {

            buffer = ByteBuffer.wrap(body)

        } else {

            throw new UnsupportedOperationException("Unknown data type in message body")
        }

        PrintWriter writer = new PrintWriter(outStream);
        int maxChunkDataSize = 4096; // in octets
        byte[] bytes = new byte[maxChunkDataSize]
        int i = 0;

        // *chunk
        while (i < buffer.limit()) {
            int nbytes = Math.min(buffer.remaining(), maxChunkDataSize);
            // chunk-size
            writer.write(Integer.toHexString(nbytes));
            // [ chunk-extension ] will go here in the future
            // CRLF
            writer.write("\r\n");
            writer.flush();

            // chunk-data
            buffer.get(bytes, 0, nbytes)
            outStream.write(bytes, 0, nbytes);
            outStream.flush();

            // CRLF
            writer.write("\r\n");
            writer.flush();
            i += nbytes;
        }

        // last-chunk
        // 1*("0")
        writer.write("0");
        // [ chunk-extension ] will go here in the future
        // CRLF
        writer.write("\r\n");
        writer.flush();

        // trailer will go here in the future

        // CRLF
        writer.write("\r\n");
        writer.flush();
    }
}
