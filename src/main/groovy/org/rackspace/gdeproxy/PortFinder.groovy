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

    def _basePort = 10000
    def _currentPort = null

    def getNextOpenPort(start=null, int sleepTime=100) {

        if (start != null) {
            _currentPort = start
        } else if (_currentPort == null) {
            _currentPort = _basePort
        }

        while (_currentPort < 65536) {
            try {
                def url = String.format("http://localhost:%d/", _currentPort)
                log.debug "Trying ${_currentPort}"
                Socket socket = new Socket("localhost", _currentPort)
            } catch (java.net.ConnectException e) {
                log.debug "Didn't connect, using this one"
                _currentPort++
                return _currentPort - 1
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

            _currentPort++
        }

        throw new RuntimeException("Ran out of ports")
    }
}
