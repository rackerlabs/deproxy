package com.rackspace.deproxy

import org.junit.Test
import static org.junit.Assert.*

class ContentEncodingTest {

    @Test
    void testReadingGzip() {

        // given an input string, and the gzipped byte sequence for it
        String content = "This is a string"
        byte[] gzippedContent = content.gzip("US-ASCII")
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
        byte[] gzippedContent = content.gzip("US-ASCII")
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

    @Test
    void testReadingDeflate() {

        // given an input string, and the compressed byte sequence for it
        String content = "This is a string"
        byte[] compressedContent = content.deflate("US-ASCII")
        def stream = new ByteArrayInputStream(compressedContent)
        //  and appropriate headers describing the body and encodings
        def headers = new HeaderCollection([
                'Content-Encoding': 'deflate',
                'Content-Length': compressedContent.length,
                'Transfer-Encoding': 'identity'
        ])

        // when we read the compressed data
        def body = BodyReader.readBody(stream, headers)

        // then the result should be the original string
        assertEquals(content, body)
    }

    @Test
    void testReadingCompress() {

        // given an input string, and the compressed byte sequence for it
        String content = "This is a string"
        byte[] compressedContent = content.compress("US-ASCII")
        def stream = new ByteArrayInputStream(compressedContent)
        //  and appropriate headers describing the body and encodings
        def headers = new HeaderCollection([
                'Content-Encoding': 'compress',
                'Content-Length': compressedContent.length,
                'Transfer-Encoding': 'identity'
        ])

        // when we read the compressed data
        def body = BodyReader.readBody(stream, headers)

        // then the result should be the original string
        assertEquals(content, body)
    }

    @Test
    void testReadingXcompress() {

        // given an input string, and the compressed byte sequence for it
        String content = "This is a string"
        byte[] compressedContent = content.compress("US-ASCII")
        def stream = new ByteArrayInputStream(compressedContent)
        //  and appropriate headers describing the body and encodings
        //  Note: according to RFC 2616 section 3.5, "x-compress" SHOULD be
        //  treated as equivalent to "compress" for backwards compatibility.
        def headers = new HeaderCollection([
                'Content-Encoding': 'x-compress',
                'Content-Length': compressedContent.length,
                'Transfer-Encoding': 'identity'
        ])

        // when we read the compressed data
        def body = BodyReader.readBody(stream, headers)

        // then the result should be the original string
        assertEquals(content, body)
    }
}
