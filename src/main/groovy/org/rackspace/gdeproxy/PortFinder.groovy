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

    public static final PortFinder Singleton = new PortFinder()

    public PortFinder(start=null) {

        if (start == null) {
            start = 10000
        }

        currentPort = start
    }

    int currentPort

    int getNextOpenPort(Map params=[:]) {

        def newStartPort = params?.newStartPort ?: null
        int sleepTime = params?.sleepTime ?: 100

        return getNextOpenPort(newStartPort, sleepTime)
    }
    int getNextOpenPort(int newStartPort) {

        return getNextOpenPort(newStartPort, 100)
    }
    int getNextOpenPort(newStartPort, int sleepTime) {

        if (newStartPort != null) {
            currentPort = newStartPort
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
