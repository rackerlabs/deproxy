package org.rackspace.deproxy;

/**
 *
 *  A collection class for HTTP Headers. This class combines aspects of a list
 *  and a map. Lookup is always case-insenitive. A key can be added multiple
 *  times with different values, and all of those values will be kept in the same
 *  order as entered.
 */
class HeaderCollection implements Iterable<Header> {

    List<Header> _headers = new ArrayList<Header>();

    HeaderCollection(Object... headers=null) {

        this.initWith(headers)
    }

    protected void initWith(initObject, List visited=[]) {

        if (initObject == null) return;

        if (containsReference(visited, initObject)) { return }
        visited.add(initObject)

        if (initObject instanceof Map) {

            for (entry in initObject.entrySet()) {

                def key = ( containsReference(visited, entry.key) ? "" : entry.key )

                initWith(
                        new Header(
                                key?.toString() ?: "",
                                entry.value?.toString() ?: ""),
                visited);
            }

        } else if (initObject instanceof List) {

            for (item in initObject) {

                initWith(item, visited)
            }

        } else if (initObject.class.isArray()) {

            initWith(initObject as List, visited)

        } else if (initObject instanceof HeaderCollection) {

            for (Header header : initObject._headers) {

                initWith(header, visited)
            }

        } else if (initObject instanceof Header) {

            // copy the header name and value. don't just add the reference
            _headers.add(new Header(initObject.name, initObject.value));

        } else if (initObject instanceof String) {

            def parts = initObject.split(':', 2)
            String name = parts[0].trim()
            String value = (parts.length > 1 ? parts[1].trim() : "")

            initWith(new Header(name, value), visited)

        } else {

            initWith(initObject.toString(), visited)
        }

        visited.remove(initObject)
    }

    static boolean containsReference(List list, Object item) {

        // test reference equality using ".is()", instead of value equality with ".equals()"
        // value equality tests may invoke equals or hashcode and inadvertently trigger infinite recursion
        // it's slower than hashing, but it works reliably and won't crash

        for (value in list) {

            if (value.is(item)) {
                return true
            }
        }

        return false
    }

    boolean contains(String name) {
        for (Header header : _headers) {
            if (name.equalsIgnoreCase(header.name)) {
                return true;
            }
        }

        return false;
    }

    public void each(Closure closure) {
        _headers.each(closure);
    }

    public void eachWithIndex(Closure closure) {
        _headers.eachWithIndex(closure);
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

    public int getCountByName(String name) {

        int count = 0;

        for (Header header : _headers) {
            if (header.name.equalsIgnoreCase(name)) {
                count++;
            }
        }

        return count;
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

    public String getAt(String name) {
        return getFirstValue(name);
    }

    public Header getAt(int index) {
        return _headers[index]
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

        return fromReadable(reader)
    }

    public static HeaderCollection fromReadable(Readable reader) {
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

    @Override
    Iterator<Header> iterator() {
        return _headers.iterator();
    }
}
