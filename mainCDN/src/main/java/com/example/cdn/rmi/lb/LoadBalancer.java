package com.example.cdn.rmi.lb;

import com.example.cdn.rmi.server.EdgeRemote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoadBalancer
    extends UnicastRemoteObject
    implements LoadBalancerRemote
{

    private final Map<EdgeRemote, Integer> activeConnections =
        new ConcurrentHashMap<>();

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

        for (Map.Entry<
            EdgeRemote,
            Integer
        > entry : activeConnections.entrySet()) {
            EdgeRemote edge = entry.getKey();
            if (!isAlive(edge)) {
                removeEdge(edge);
                continue;
            }
            int connections = entry.getValue();
            if (connections < minConnections) {
                minConnections = connections;
                bestEdge = edge;
            }
        }

        if (bestEdge != null) {
            activeConnections.put(bestEdge, minConnections + 1);
        } else {
            throw new RemoteException("No available Edge Servers.");
        }

        return bestEdge;
    }

    private void removeEdge(EdgeRemote edge) throws RemoteException {
        activeConnections.remove(edge);
    }

    private boolean isAlive(EdgeRemote edge) {
        try {
            if (edge == null) return false;
            edge.getServerId(); // lightweight ping
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
