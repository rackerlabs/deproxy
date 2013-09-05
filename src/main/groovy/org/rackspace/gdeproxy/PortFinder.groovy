/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rackspace.gdeproxy;

import groovy.util.logging.Log4j;

/**
 *
 * @author richard-sartor
 */
@Log4j
public class PortFinder {

    def basePort = 10000
    def currentPort = null

    def getNextOpenPort(start=null, int sleepTime=100) {

        if (start != null) {
            currentPort = start
        } else if (currentPort == null) {
            currentPort = basePort
        }

        while (currentPort < 65536) {
            try {
                def url = String.format("http://localhost:%d/", currentPort)
                log.debug "Trying ${currentPort}"
                Socket socket = new Socket("localhost", currentPort)
            } catch (java.net.ConnectException e) {
                log.debug "Didn't connect, using this one"
                currentPort++
                return currentPort - 1
            } catch (SocketException e) {
                // ignore the exception
                log.warn "Got a SocketException: " + e.toString();
            } catch (IOException e) {
                // ignore the exception
                log.warn "Got a IOException: " + e.toString();
            } catch (Exception e) {
                log.warn "Got an Exception: " + e.toString();
                throw e
            }

            Thread.sleep(sleepTime)
            log.debug "Connected"

            currentPort++
        }

        throw new RuntimeException("Ran out of ports")
    }
}
