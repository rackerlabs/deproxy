package com.rackspace.deproxy.util

import by.dev.madhead.lzwj.compress.LZW

import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.InflaterInputStream

class StringExtension {

    static byte[] gzip(String s, String encoding="UTF-8") {
        def byteStream = new ByteArrayOutputStream()
        def gzip = new GZIPOutputStream(byteStream)
        gzip.write(s.getBytes(encoding))
        gzip.close()
        return byteStream.toByteArray()
    }

    static byte[] deflate(String s, String encoding="UTF-8") {
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

    static byte[] compress(String s, String charset="UTF-8") {
        def uncompressedStream = new ByteArrayInputStream(s.getBytes(charset))
        def compressedStream = new ByteArrayOutputStream()
        LZW lzw = new LZW()
        lzw.compress(uncompressedStream, compressedStream)
        def compressedBytes = compressedStream.toByteArray()
        uncompressedStream.close()
        compressedStream.close()
        return compressedBytes
    }
}
