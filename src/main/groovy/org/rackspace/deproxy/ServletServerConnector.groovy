package org.rackspace.deproxy

import groovy.servlet.AbstractHttpServlet
import groovy.util.logging.Log4j

import javax.servlet.GenericServlet
import javax.servlet.ServletConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Log4j
class ServletServerConnector extends HttpServlet implements ServerConnector {

    Endpoint endpoint

    ServletServerConnector(Endpoint endpoint, String name) {

        this.endpoint = endpoint
    }

    public static ServletServerConnector Factory(Endpoint endpoint, String name) {

        return new ServletServerConnector(endpoint, name)
    }

    void shutdown() {

    }

    @Override
    protected void service(HttpServletRequest request,
                           HttpServletResponse response) throws ServletException, IOException {

        log.debug("service(HttpServletRequest, HttpServletResponse)")

        // convert the servlet request to a Deproxy request
        log.debug("convert the servlet request to a Deproxy request")

        String method = request.method
        String path = request.requestURI
        String query = request.queryString
        if (query != null) {
            path = path + "?" + query
        }

        HeaderCollection headers = new HeaderCollection()
        for (name in request.headerNames) {
            for (value in request.getHeaders(name)) {
                headers.add(name, value)
            }
        }

        def inStream = request.getInputStream()
        def body = BodyReader.readBody(inStream, headers)

        def drequest = new Request(method, path, headers, body)

        // handle the request
        log.debug("handle the request")

        ResponseWithContext rwc = endpoint.handleRequest(drequest, "servlet")
        Response dresponse = rwc.response
        HandlerContext context = rwc.context


        // convert the Deproxy response to a servlet response
        log.debug("convert the Deproxy response to a servlet response")

        response.setStatus(dresponse.code.toInteger(), dresponse.message);

        for (header in dresponse.headers.getItems()) {
            response.addHeader(header.name, header.value)
        }

        if (dresponse.body != null) {
            if (dresponse.body instanceof byte[]) {
                response.outputStream.write(dresponse.body)
            } else if (dresponse.body instanceof String) {
                response.writer.write(dresponse.body as String)
            } else {
                response.writer.write(dresponse.body.toString())
            }
        }
    }
}
