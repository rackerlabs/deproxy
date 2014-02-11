package org.rackspace.deproxy


class LineReader {

    // utility method to be used by all parts of the system for reading from
    // sockets. this way, there's a consistent implementation and consistent
    // policy for end-of-line
    static String readLine(Readable reader) {
        int value = reader.read()
        if (value < 0) {
            return null
        }

        StringBuilder sb = new StringBuilder()

        // naive definition of line termination
        // just consider \n, not \r or \r\n
        while (value >= 0 && value != '\n') {
            char ch = (char)value
            if (ch != '\r') {
                sb.append(ch)
            }
            value = reader.read()
        }

        return sb.toString()
    }

    static String readLine(InputStream inStream) {
        return readLine(new UnbufferedStreamReader(inStream))
    }
}
