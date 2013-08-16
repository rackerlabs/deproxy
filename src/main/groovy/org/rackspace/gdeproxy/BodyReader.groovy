package org.rackspace.gdeproxy

import org.apache.log4j.Logger

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
class BodyReader {
    static def readBody(InputStream inStream, headers) {

        if (headers == null)
            return null

        Logger log = Logger.getLogger(Deproxy.class.getName());

        if (headers == null)
            return null

        byte[] bindata

        /*
           RFC 2616 section 4.4, Message Length
           1. Any response message that must not return a body (1xx, 204,
                304, HEAD) should be terminated by the first empty line
                after the header fields, regardless of entity headers.
           2. If the Transfer-Encoding header is present and has a value
                other than "identity", then it uses chunked encoding.
           3. If Content-Length is present, it specifies both the
                transfer-length and entity-length in octets (these must be
                the same)
           4. multipart/byteranges
           5. server closes the connection, for response bodies
         */


        // # 2
        if (headers.contains("Transfer-Encoding")) {
            if (headers["Transfer-Encoding"] == "identity") {

                // ignore Transfer-Encoding. proceed to #3

            } else if (headers["Transfer-Encoding"] == "chunked") {

                bindata = readChunkedBody(inStream)

            } else {

                // rfc 2616 § 3.6
                //
                // A server which receives an entity-body with a transfer-coding it does
                // not understand SHOULD return 501 (Unimplemented), and close the
                // connection. A server MUST NOT send transfer-codings to an HTTP/1.0
                // client.

                log.error "Non-identity transfer encoding, not yet supported in GDeproxy.  Unable to read response body."
                return null
            }
        }

        // # 3
        if (bindata == null &&  headers.contains("Content-Length")) {
            int length = headers.getFirstValue("Content-Length").toInteger();
            log.debug("Headers contain Content-Length: ${length}")

            if (length > 0) {
                bindata = new byte[length]
                int i;
                def count = 0
                log.debug("  starting to read body")
                for (i = 0; i < length; i++) {
                    int ii = inStream.read()
                    log.debug("   [${i}] = ${ii}")
                    byte bb = (byte)ii
                    bindata[i] = bb
                    count++;
                }
//            def count = inStream.read(bindata);

                if (count != length) {
                    // end of stream or some error
                    // TODO: what does the spec say should happen in this case?
                }
            }
        }

        // # 4 multipart/byteranges

        // else, there is no body (?)

        if (bindata == null) {
            log.debug("Returning null");
            return null;
        }

        // TODO: switch this to true, and always try to read chardata unless
        // it's a known binary content type
        boolean tryCharData = false

        if (!headers.contains("Content-type")) {
            tryCharData = true;
        } else {
            String contentType = headers["Content-Type"]?.toLowerCase()

            // use startsWith in order to ignore any charset or other
            // parameters on the header value
            if (contentType?.startsWith("text/") ||
                    contentType?.startsWith("application/atom+xml") ||
                    contentType?.startsWith("application/ecmascript") ||
                    contentType?.startsWith("application/json") ||
                    contentType?.startsWith("application/javascript") ||
                    contentType?.startsWith("application/rdf+xml") ||
                    contentType?.startsWith("application/rss+xml") ||
                    contentType?.startsWith("application/soap+xml") ||
                    contentType?.startsWith("application/xhtml+xml") ||
                    contentType?.startsWith("application/xml") ||
                    contentType?.startsWith("application/xml-dtd") ||
                    contentType?.startsWith("application/xop+xml") ||
                    contentType?.startsWith("image/svg+xml") ||
                    contentType?.startsWith("message/http") ||
                    contentType?.startsWith("message/imdn+xml")) {

                tryCharData = true
            }
        }

        if (tryCharData) {
            String chardata = null

            try {
                def decoder = Charset.forName("US-ASCII").newDecoder()
                decoder.onMalformedInput(CodingErrorAction.REPORT)
                decoder.onUnmappableCharacter(CodingErrorAction.REPORT)
                chardata = decoder.decode(ByteBuffer.wrap(bindata)).toString()
            } catch (Exception e) {
            }

            if (chardata != null) {
                return chardata
            }
        }

        return bindata
    }

    static byte[] readChunkedBody(InputStream inStream) {

        return new byte[0];
    }
}
