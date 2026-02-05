package com.example.cdn.rmi.lb;

import com.example.cdn.rmi.server.EdgeRemote;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LoadBalancerRemote extends Remote {
    public EdgeRemote getEdgeWithLeastConnections() throws RemoteException;
    public void addEdge(EdgeRemote edge) throws RemoteException;
}
