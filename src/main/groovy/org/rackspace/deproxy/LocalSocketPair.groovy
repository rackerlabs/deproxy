package org.rackspace.deproxy


class LocalSocketPair {

    /**
     * I cannot find any usages of this method within any of deproxy. It's possible
     * that it's used outside.
     *
     * However, it will not work with the cluster awareness, because the ports iwll conflict with ones that might be in
     * use.
     *
     * This must either use the Singleton for PortFinder, or have one passed in. It will break the clustering if not
     * @param port
     * @return
     */
    public static List<Socket> createLocalSocketPair(PortFinder port=null) {

        int chosenPort = -1

        if (port == null || port instanceof PortFinder) {
            PortFinder pf
            if (port == null) {
                pf = PortFinder.Singleton
            } else {
                pf = port as PortFinder
            }

            chosenPort = pf.getNextOpenPort()
        }

        // create the listener socket
        def listener = new ServerSocket(chosenPort)
        def server

        // start listening on a separate thread
        def t = Thread.startDaemon("get-socket") {
            server = listener.accept()
        }

        // create the client socket and connect
        def client = new Socket("localhost", chosenPort)

        t.join()

        listener.close()

        return [ client, server ]
    }

}
