package org.rackspace.deproxy.util

import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.InflaterInputStream

class StringExtension {

    static byte[] toGzip(String s, String encoding="UTF-8") {
        def byteStream = new ByteArrayOutputStream()
        def gzip = new GZIPOutputStream(byteStream)
        gzip.write(s.getBytes(encoding))
        gzip.close()
        return byteStream.toByteArray()
    }

    static byte[] toDeflate(String s, String encoding="UTF-8") {
        def byteStream = new ByteArrayOutputStream()
        def compress = new DeflaterOutputStream(byteStream)
        compress.write(s.getBytes(encoding))
        compress.close()
        return byteStream.toByteArray()
    }

    static String inflate(byte[] bytes) {
        def byteStream = new ByteArrayInputStream(bytes)
        def decompress = new InflaterInputStream(byteStream)
        def decompressedBytes = decompress.getBytes()
        def s = new String(decompressedBytes, "UTF-8")
        return s
    }
}
