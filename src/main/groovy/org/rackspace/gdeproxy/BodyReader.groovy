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

//      def reader = new InputStreamReader(inStream);

        if (headers == null)
            return null

        Logger log = Logger.getLogger(Deproxy.class.getName());

        if (headers == null)
            return null

        byte[] bindata

        //    if ('Transfer-Encoding' in headers and
        //            headers['Transfer-Encoding'] != 'identity'):
        //        # 2
        //        logger.debug('NotImplementedError - Transfer-Encoding != identity')
        //        raise NotImplementedError
        headers.findAll("Transfer-Encoding").each {
            if (it.value != "identity")
            {
                log.error "Non-identity transfer encoding, not yet supported in GDeproxy.  Unable to read response body."
                return null
            }
        }
        //    elif 'Content-Length' in headers:
        //        # 3
        //        length = int(headers['Content-Length'])
        //        body = stream.read(length)
        if (headers.contains("Content-Length")) {
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
        //    elif False:
        //        # multipart/byteranges ?
        //        logger.debug('NotImplementedError - multipart/byteranges')
        //        raise NotImplementedError

        //    else:
        //        # there is no body
        //        body = None
        //    return body

        if (bindata == null) {
            log.debug("Returning null");
            return null;
        }

        if (!headers.contains("Content-type") ||
                headers.getFirstValue("Content-type")?.toLowerCase()?.startsWith("text/")) {

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

}
