package com.rackspace.deproxy


class LocalSocketPair {

    public static List<Socket> createLocalSocketPair(port=null) {

        if (port == null || port instanceof PortFinder) {
            port = 0
        }

        // create the listener socket
        def listener = new ServerSocket(port)
        def localPort = listener.getLocalPort()
        def server

        // start listening on a separate thread
        def t = Thread.startDaemon("get-socket") {
            server = listener.accept()
        }

        // create the client socket and connect
        def client = new Socket("localhost", localPort)

        t.join()

        listener.close()

        return [ client, server ]
    }

}
