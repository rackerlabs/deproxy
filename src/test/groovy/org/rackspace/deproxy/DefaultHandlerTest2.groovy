package org.rackspace.deproxy

import spock.lang.Specification

class DefaultHandlerTest2 extends Specification {

    def "when request is handled, should return 200 OK response"() {

        given:
        def pf = new PortFinder()
        def deproxyPort = pf.getNextOpenPort()
        def deproxy = new Deproxy()
        def endpoint = deproxy.addEndpoint(deproxyPort)

        when:
        MessageChain mc = deproxy.makeRequest("http://localhost:${deproxyPort}")

        then:
        mc.receivedResponse.code == "200"

        cleanup:
        if (deproxy) {
            deproxy.shutdown()
        }
    }

}
