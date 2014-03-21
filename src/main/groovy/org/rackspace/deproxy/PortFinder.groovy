package org.rackspace.deproxy

import com.hazelcast.config.Config
import com.hazelcast.config.NetworkConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance;
import groovy.util.logging.Log4j;


@Log4j
public class PortFinder {

    //Internal lock so we don't synchronize on the object itself
    private final Object lock = new Object()

    public static final PortFinder Singleton = new PortFinder()

    HazelcastInstance hazelcast

    Set<Integer> hazelSet

    public PortFinder(start = 10000) {

        //Set up hazelcast to cluster on this host only
        // This should get us a cluster aware set so we can know what JVMs are using what ports on all things
        // There's still a possibility of a race condition if two devices reserve the port at the same time
        // I'll try to mitigate this
        Config cfg = new Config()

        //Use a UUID for the port finder name too
        def uuid = UUID.randomUUID().toString()
        cfg.setInstanceName("PortFinderCluster-${uuid}")

        // Configure it to use the log4j stuff
        cfg.setProperty("hazelcast.logging.type", "log4j")

        NetworkConfig network = cfg.getNetworkConfig();
        network.setPort(5990)
        network.setPortAutoIncrement(true) //Many will have to crawl up once we consume one of these I think
        network.getInterfaces().addInterface("127.0.0.1") //Only operate on localhost
        network.getJoin().getMulticastConfig().setEnabled(true) //Should do mulitcast on localhost

        //GIMMIE
        hazelcast = Hazelcast.newHazelcastInstance(cfg)

        //Get the distributed port set
        hazelSet = hazelcast.getSet("used-ports-set")

        currentPort = start
    }

    int currentPort

    /**
     * TODO: refactor this to use named parameters, and default values
     *
     * Gets the next open port optionally passing in the new start port and the sleep time via a map
     * @param params
     * @return
     */
    int getNextOpenPort(Map params=[:]) {

        int newStartPort = params?.newStartPort ?: -1
        int sleepTime = params?.sleepTime ?: 100

        return getNextOpenPort(newStartPort, sleepTime)
    }

    /**
     * Get the next open port starting at the new start point
     * @param newStartPort
     * @return
     */
    int getNextOpenPort(int newStartPort) {

        return getNextOpenPort(newStartPort, 100)
    }

    /**
     * Better terminology for what we should be doing
     * We should reserve a port for use by this VM, or many things on this VM
     * This implies that the port should be returned when it's done so it can be made available again
     * @param startPort
     * @param sleepTime
     * @return
     */
    int reservePort(int startPort = -1, int sleepTime = 100) {
        return getNextOpenPort(newStartPort: startPort, sleepTime: sleepTime)
    }

    /**
     * When finished with using the port, release it so that the cluster can keep going
     * @param port
     */
    def returnPort(int port){
        //We're basically going to trust the service that they're done with this, and return it back to the cluster
        //Not really caring if it's used or not
        hazelSet.remove(port)
    }


    /**
     * Used internally
     * @param newStartPort
     * @param sleepTime
     * @return
     */
    int getNextOpenPort(int newStartPort, int sleepTime) {
        synchronized (lock) {
            if (newStartPort >= 0) {
                currentPort = newStartPort
            }

            while (currentPort < 65536) {
                if(available(currentPort)) {
                    //We can use this port

                    //Throw it into hazelcast's set
                    if(hazelSet.add(currentPort)) {
                        //we added it, the cluster is aware this port has been consumed
                        return currentPort
                    } else {
                        //oh noes, couldn't add it someone beat us to it, keep hunting
                    }
                } else {
                    //Ports not available, keep hunting
                }

                Thread.sleep(sleepTime)
                currentPort ++
            }



            throw new RuntimeException("Ran out of ports")
        }
    }

    /**
     * Checks to see if a specific port is available.
     * Stole it from here: http://stackoverflow.com/a/435579/423218
     * @param port the port to check for availability
     */
    public static boolean available(int port) {
        //Set these to 1024 and max ports, because we won't be able to get a port below 1024 if we're not root
        if (port < 1024 || port > 65536) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }
}
