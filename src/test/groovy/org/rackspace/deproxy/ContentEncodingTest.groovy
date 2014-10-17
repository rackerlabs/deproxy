package org.rackspace.deproxy

import org.junit.Test
import static org.junit.Assert.*

class ContentEncodingTest {

    @Test
    void testReadingGzip() {

        // given an input string, and the gzipped byte sequence for it
        String content = "This is a string"
        byte[] gzippedContent = content.toGzip("US-ASCII")
        def stream = new ByteArrayInputStream(gzippedContent)
        //  and appropriate headers describing the body and encodings
        def headers = new HeaderCollection([
                'Content-Encoding': 'gzip',
                'Content-Length': gzippedContent.length,
                'Transfer-Encoding': 'identity'
        ])

        // when we read the gzipped data
        def body = BodyReader.readBody(stream, headers)

        // then the result should be the original string
        assertEquals(content, body)
    }

    @Test
    void testReadingXgzip() {

        // given an input string, and the gzipped byte sequence for it
        String content = "This is a string"
        byte[] gzippedContent = content.toGzip("US-ASCII")
        def stream = new ByteArrayInputStream(gzippedContent)
        //  and appropriate headers describing the body and encodings
        //  Note: according to RFC 2616 section 3.5, "x-gzip" SHOULD be
        //  treated as equivalent to "gzip" for backwards compatibility.
        def headers = new HeaderCollection([
                'Content-Encoding': 'x-gzip',
                'Content-Length': gzippedContent.length,
                'Transfer-Encoding': 'identity'
        ])

        // when we read the gzipped data
        def body = BodyReader.readBody(stream, headers)

        // then the result should be the original string
        assertEquals(content, body)

    }

}
