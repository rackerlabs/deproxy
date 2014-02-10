package org.rackspace.deproxy


class LocalSocketPair {

    public static List<Socket> createLocalSocketPair(port=null) {

        if (port == null || port instanceof PortFinder) {
            PortFinder pf
            if (port == null) {
                pf = PortFinder.Singleton
            } else {
                pf = port as PortFinder
            }

            port = pf.getNextOpenPort()
        }

        // create the listener socket
        def listener = new ServerSocket(port)
        def server

        // start listening on a separate thread
        def t = Thread.startDaemon("get-socket") {
            server = listener.accept()
        }

        // create the client socket and connect
        def client = new Socket("localhost", port)

        t.join()

        listener.close()

        return [ client, server ]
    }

}
