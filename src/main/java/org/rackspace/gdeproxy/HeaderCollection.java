package org.rackspace.gdeproxy;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 *
 * @author ricahrd-sartor
 *
 * A collection class for HTTP Headers. This class combines aspects of a list
 * and a map. Lookup is always case-insenitive. A key can be added multiple
 * times with different values, and all of those values will be kept in the same
 * order as entered.
 *
 */
class HeaderCollection {

  List<Header> _headers = new ArrayList<Header>();

  HeaderCollection() {
  }

  HeaderCollection(Map<? extends Object, ? extends Object> map) {
    for (Map.Entry entry : map.entrySet()) {
      _headers.add(new Header(entry.getKey().toString(), entry.getValue().toString()));
    }
  }

    HeaderCollection(List<Header> list) {
        for (Header header : list) {
            _headers.add(header);
        }
    }

  HeaderCollection(HeaderCollection headers) {
    for (Header header : headers._headers) {
      _headers.add(new Header(header.name, header.value));
    }
  }

  boolean contains(String name) {
    for (Header header : _headers) {
      if (name.equalsIgnoreCase(header.name)) {
        return true;
      }
    }

    return false;
  }

  public Object each(Closure closure) {
    return DefaultGroovyMethods.each(_headers, closure);
  }

  public Object eachWithIndex(Closure closure) {
    return DefaultGroovyMethods.eachWithIndex(_headers, closure);
  }

  public int size() {
    return _headers.size();
  }

  public void add(Object name, Object value) {
    add(new Header(name.toString(), value.toString()));
  }

  public void add(String name, String value) {
    add(new Header(name, value));
  }

  public void add(Header header) {
    _headers.add(header);
  }

  public List<String> findAll(String name) {

    List<String> values = new ArrayList<String>();

    for (Header header : _headers) {
      if (header.name.equalsIgnoreCase(name)) {
        values.add(header.value);
      }
    }

    return values;
  }

  public void deleteAll(String name) {

    ArrayList<Header> toRemove = new ArrayList<Header>();

    for (Header header : _headers) {
      if (name.equalsIgnoreCase(header.name)) {
        toRemove.add(header);
      }
    }

    _headers.removeAll(toRemove);
  }

  public String[] getNames() {
    ArrayList<String> names = new ArrayList<String>();
    for (Header header : _headers) {
      names.add(header.name);
    }

    return names.toArray(new String[0]);
  }

  public String[] getValues() {
    ArrayList<String> values = new ArrayList<String>();
    for (Header header : _headers) {
      values.add(header.value);
    }

    return values.toArray(new String[0]);
  }

  public Header[] getItems() {
    return _headers.toArray(new Header[0]);
  }

  public String getFirstValue(String name) {
    return getFirstValue(name, null);
  }
  public String getFirstValue(String name, String defaultValue) {
    for (Header header : _headers) {
      if (name.equalsIgnoreCase(header.name)) {
        return header.value;
      }
    }

    return defaultValue;
  }

  public static HeaderCollection fromStream(InputStream inStream) throws IOException {

    UnbufferedStreamReader reader = new UnbufferedStreamReader(inStream);

    HeaderCollection headers = new HeaderCollection();
    String line = LineReader.readLine(reader);
    while (line != null && !line.equals("") && !line.equals("\r\n")) {
      String[] parts = line.split(":", 2);
      String name = parts[0];
      String value = (parts.length > 1 ? parts[1] : "");
      name = name.trim();
      line = LineReader.readLine(reader);
      while (line.startsWith(" ") || line.startsWith("\t")) {
        // Continuation lines - see RFC 2616, section 4.2
        value += " " + line;
        line = LineReader.readLine(reader);
      }
      headers.add(name, value.trim());
    }
    return headers;

  }

    public String toString() {
        return _headers.toString();
    }
}
