package org.rackspace.deproxy.util

import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class StringExtension {

    static byte[] toGzip(String s, String encoding="US-ASCII") {
        def byteStream = new ByteArrayOutputStream()
        def gzip = new GZIPOutputStream(byteStream)
        gzip.write(s.getBytes(encoding))
        gzip.close()
        return byteStream.toByteArray()
    }
}
