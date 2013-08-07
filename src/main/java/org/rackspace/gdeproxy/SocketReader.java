/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.gdeproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

@Deprecated
public class SocketReader extends InputStreamReader {

  InputStream __stream;
  StringBuilder _sb;
  boolean _lastCharWasCR = false;

  SocketReader(InputStream stream) {
    super(stream);
    __stream = stream;
  }
  SocketReader(InputStream stream, Charset cs) {
    super(stream, cs);
    __stream = stream;
  }
  SocketReader(InputStream stream, CharsetDecoder dec) {
    super(stream, dec);
    __stream = stream;
  }
  SocketReader(InputStream stream, String charsetName) throws UnsupportedEncodingException {
    super(stream, charsetName);
    __stream = stream;
  }

  String readLine() throws IOException {

    if (_sb == null) {
      _sb = new StringBuilder();
    }

    while (true) {

//      int value0 = this.__stream.read();
      int value = this.read();

      if (value == -1) {
        if (_sb.length() > 0) {
          String retval = _sb.toString();
          _sb.setLength(0);
          _lastCharWasCR = false;
          return retval;
        }
        return null;
      }

      char ch = (char)value;

      if (ch == '\n')
      {
        String retval = _sb.toString();
        _sb.setLength(0);
        _lastCharWasCR = false;
        return retval;
      }

      if (_lastCharWasCR) {
        String retval = _sb.toString();
        _sb.setLength(0);

        _lastCharWasCR = (ch == '\r');
        if (ch != '\r') {
          _sb.append(ch);
        }

        return retval;
      }

      _sb.append(ch);
    }
  }
}

