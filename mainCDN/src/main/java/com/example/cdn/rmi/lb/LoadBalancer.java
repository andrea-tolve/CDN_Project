package com.example.cdn.rmi.lb;

import com.example.cdn.rmi.server.EdgeRemote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class LoadBalancer
    extends UnicastRemoteObject
    implements LoadBalancerRemote
{

    Map<EdgeRemote, Integer> activeConnections = new HashMap<>();

    public LoadBalancer() throws RemoteException {
        super();
    }

    public void addEdge(EdgeRemote edge) throws RemoteException {
        activeConnections.put(edge, 0);
    }

    /*
     * This method sorts the requests based on the number of active connections.
     * It returns the edge with the least number of active connections.
     */
    public EdgeRemote getEdgeWithLeastConnections() throws RemoteException {
        EdgeRemote bestEdge = null;
        int minConnections = Integer.MAX_VALUE;

        for (EdgeRemote edge : activeConnections.keySet()) {
            int connections = activeConnections.get(edge);
            if (connections < minConnections) {
                minConnections = connections;
                bestEdge = edge;
            }
        }

        if (bestEdge != null) {
            activeConnections.put(bestEdge, minConnections + 1);
        }

        return bestEdge;
    }
}
