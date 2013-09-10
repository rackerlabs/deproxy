package org.rackspace.gdeproxy

import spock.lang.Specification

import java.util.concurrent.CountDownLatch

/**
 * Created with IntelliJ IDEA.
 * User: izrik
 *
 */
class PortFinderTest extends Specification {

    PortFinder pf

    def setup() {
        pf = new PortFinder()
    }

    def "when a port number is not in use, return it"() {

        when:
        int port = pf.getNextOpenPort(12345)

        then:
        port == 12345
    }

    def "when a port number is already in use, return the next port number"() {

        given:
        // create the listener socket
        def listener = new ServerSocket(12345)
        boolean stop = false
        def t = Thread.startDaemon("get-socket") {
            while (!stop) {
                listener.accept()
            }
        }

        when:
        int port = pf.getNextOpenPort(12345)

        then:
        port == 12346

        cleanup:
        stop = true
        t.interrupt()
        t.join(1000)
    }

    def "when instantiating without parameter, should have the default"() {

        when:
        def pf2 = new PortFinder()

        then:
        pf2.currentPort == 10000
    }

    def "when instantiating with parameter, should have the given value"() {

        when:
        def pf2 = new PortFinder(23456)

        then:
        pf2.currentPort == 23456
    }

    def "when using the singleton among multiple threads, should be thread-safe"() {

        def prevCurrentPort = PortFinder.Singleton.currentPort

        List<Thread> threads = []
        CountDownLatch startSignal = new CountDownLatch(1)
        def listLock = new Object()
        def ports = []

        for (i in 0..100) {
            threads.add(Thread.startDaemon {
                startSignal.await()
                int port = PortFinder.Singleton.getNextOpenPort()
                synchronized(listLock) {
                    ports.add(port)
                }
            })
        }

        startSignal.countDown()

        for (th in threads) {
            th.join()
        }

        expect:
        PortFinder.Singleton.currentPort == prevCurrentPort + threads.size()
        ports.unique().size() == threads.size()
    }
}
