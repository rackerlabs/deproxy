package org.rackspace.deproxy

import spock.lang.Specification

import java.util.concurrent.CountDownLatch


class PortFinderTest extends Specification {

    PortFinder pf

    def setup() {
        pf = PortFinder.Singleton
    }

    def "port finder always gives you a free port"() {
        when:
        int port = pf.getNextOpenPort()

        then:
        PortFinder.available(port)
    }

    def "port finder will not give you ports that are consumed"() {
        given:
        def servers = []
        //Use up like 10 more ports here -- Probably reasonably safe in this test environment
        10.times { x ->
            def server = new ServerSocket(54000 + x)
            server.setReuseAddress(true)
            servers << server
        }

        when:
        int port = pf.reservePort(54000)

        then:
        PortFinder.available(port)

        port != 54000
        port != 54001
        port != 54002
        port != 54003
        port != 54004
        port != 54005
        port != 54006
        port != 54007
        port != 54008
        port != 54009

        !PortFinder.available(54000)
        !PortFinder.available(54001)
        !PortFinder.available(54002)
        !PortFinder.available(54003)
        !PortFinder.available(54004)
        !PortFinder.available(54005)
        !PortFinder.available(54006)
        !PortFinder.available(54007)
        !PortFinder.available(54008)
        !PortFinder.available(54009)


        cleanup:
        servers.each { s ->
            s.close()
        }
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
        //All the port numbers should be unique, don't need to verify what they actually are
        // If we have any duplications, it's over!
        ports.sort().unique().size() == threads.size()
    }
}
