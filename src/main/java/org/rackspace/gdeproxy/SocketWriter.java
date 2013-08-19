/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rackspace.gdeproxy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

@Deprecated
class SocketWriter extends OutputStreamWriter {

    public SocketWriter(OutputStream out) {
        super(out);
    }

    public SocketWriter(OutputStream out, Charset cs) {
        super(out, cs);
    }

    public SocketWriter(OutputStream out, CharsetEncoder enc) {
        super(out, enc);
    }

    public SocketWriter(OutputStream out, String charsetName) throws UnsupportedEncodingException {
        super(out, charsetName);
    }

    public void writeln(String s) throws IOException {
        writeln(s, "\r\n");
    }

    public void writeln(String s, String lineEnding) throws IOException {
        int i;

        if (s != null) {
            for (i = 0; i < s.length(); i++) {
                this.write(s.charAt(i));
            }
        }

        if (lineEnding != null) {
            for (i = 0; i < lineEnding.length(); i++) {
                this.write(lineEnding.charAt(i));
            }
        }
    }


}
