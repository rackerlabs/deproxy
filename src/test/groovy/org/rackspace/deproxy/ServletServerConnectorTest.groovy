package org.rackspace.deproxy

import org.apache.catalina.Context
import org.apache.catalina.Globals
import org.apache.catalina.startup.Tomcat
import org.apache.commons.io.FileUtils
import spock.lang.Specification

import javax.servlet.Servlet

class ServletServerConnectorTest extends Specification {

    def "when loaded into a servlet container, the endpoint should service and capture requests"() {

        given:
        Deproxy deproxy = new Deproxy()
        Tomcat tomcat = new Tomcat();
        int port = PortFinder.Singleton.getNextOpenPort()
        tomcat.setPort(port);
        String absPath = new File("src/test/groovy/").getAbsolutePath();
        Context rootCtx = tomcat.addContext("", absPath);
        Endpoint endpoint = deproxy.addEndpoint(
                connectorFactory: ServletServerConnector.&Factory,
                defaultHandler: { request ->
                    new Response(606, "Spoiler", ['Name':'Value'], "Snape kills Dumbledore")
                })
        Tomcat.addServlet(rootCtx, "test-servlet", endpoint.serverConnector as Servlet);
        rootCtx.addServletMapping("", "test-servlet");
        tomcat.start();

        when:
        def mc = deproxy.makeRequest(url: "http://localhost:${port}")

        then:
        mc.receivedResponse.code == "606"
        mc.receivedResponse.message == "Spoiler"
        mc.receivedResponse.headers.getCountByName('Name') == 1
        mc.receivedResponse.headers['Name'] == 'Value'
        mc.receivedResponse.body == "Snape kills Dumbledore"
        mc.handlings.size() == 1
        mc.handlings[0].response.code == "606"
        mc.handlings[0].response.message == "Spoiler"
        mc.handlings[0].response.headers.getCountByName('Name') == 1
        mc.handlings[0].response.headers['Name'] == 'Value'
        mc.handlings[0].response.body == "Snape kills Dumbledore"

        cleanup:
        if (tomcat) {
            tomcat.stop()

            // kludge: there's no good way currently to get the basedir field
            // on tomcat. instead, we take advantage of the fact that
            // initBaseDir sets CATALINA_BASE_PROP at the end. Ideally, we
            // could do something like
            //      FileUtils.deleteQuietly(tomcat.getBaseDir())
            // but until then, this will have to do.
            def basedir = new File(System.getProperty(Globals.CATALINA_BASE_PROP))
            FileUtils.deleteQuietly(basedir)
        }
        if (deproxy) {
            deproxy.shutdown()
        }
    }
}
