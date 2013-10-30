package org.rackspace.deproxy

class HostHeader extends Header {

    public HostHeader(String host, int port, boolean https=false) {
        super("Host", CreateHostHeaderValue(host, port, https))
    }

    public static String CreateHostHeaderValue(String host, int port, https=false) {

        if ((https && port != 443) ||
                (!https && port != 80)) {

            return "${host}:${port}"

        } else {

            return host
        }
    }
}
