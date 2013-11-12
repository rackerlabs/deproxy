package org.rackspace.deproxy

class HostHeader extends Header {

    // http://tools.ietf.org/html/rfc2616#section-14.23

    public HostHeader(String host, port=null) {
        super("Host", CreateHostHeaderValue(host, port))

        this.host = host
        this.port = port
    }

    final String host
    final def port

    public static String CreateHostHeaderValue(String host, port=null, https=null) {

        if (port != null &&
                port instanceof Integer) {

            return "${host}:${port}"

        } else if (https != null) {
            if (https) {
                return "${host}:443"
            } else {
                return "${host}:80"
            }
        }
        else
        {
            return host
        }
    }

    public static HostHeader fromString(String value) {

        """ Takes a header value of the for 'hostname:port' and returns a HostHeader"""

        // RFC 2616 § 14.23
        // Host = "Host" ":" host [ ":" port ]

        // RFC 2396 § 3.2.2
        // host          = hostname | IPv4address
        // hostname      = *( domainlabel "." ) toplabel [ "." ]
        // domainlabel   = alphanum | alphanum *( alphanum | "-" ) alphanum
        // toplabel      = alpha    | alpha *( alphanum | "-" ) alphanum
        // IPv4address   = 1*digit "." 1*digit "." 1*digit "." 1*digit
        // port          = *digit

        value = value.trim()

        String host
        String portStr
        if (value.contains(":")) {
            def parts = value.split(":", 2)
            host = parts[0].trim()
            portStr = parts[1].trim()
        } else {
            host = value
            portStr = ''
        }

        String domainlabelPattern   = /(?x) ( [\w\d] | ( [\w\d] ( [\w\d] | \- )* [\w\d] ) )/
        String toplabelPattern      = /(?x) ( [\w]  | ( [\w] ( [\w\d] | \- )* [\w\d] ) )/
        String hostnamePattern      = /(?x) ( ${domainlabelPattern} \. )* ( ${toplabelPattern} ) (\.?) /
        String IPv4addressPattern   = /(?x) ( [\d]+ \. [\d]+ \. [\d]+ \. [\d]+ ) /
        String hostPattern          = /(?x) ( ${hostnamePattern} | ${IPv4addressPattern} )/
        String portPattern          = /(?x) ( [\d]* )/

        if (!(host ==~ hostPattern)) {
            throw new IllegalArgumentException("The value provided does not contain a valid hostname")
        }

        if (!(portStr ==~ portPattern)) {
            throw new IllegalArgumentException("The value provided contains an invalid port")
        }

        if (portStr != null && portStr.length() > 0) {
            int port = portStr.toInteger()
            return new HostHeader(host, port)
        } else {
            return new HostHeader(host)
        }
    }
}
