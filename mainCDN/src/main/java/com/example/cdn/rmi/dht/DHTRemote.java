package com.example.cdn.rmi.dht;

import com.example.cdn.rmi.server.EdgeRemote;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DHTRemote extends Remote {
    public DHTRemote findSuccessor(int id) throws RemoteException;
    public DHTRemote closestPrecedingFinger(int id) throws RemoteException;
    public void join(DHTRemote bootstrapNode) throws RemoteException;
    public void notify(DHTRemote node) throws RemoteException;
    public void addMapping(String contentId) throws RemoteException;
    public void removeMapping(String contentId) throws RemoteException;
    public void leave(EdgeRemote edge) throws RemoteException;
    public void stabilize() throws RemoteException;
    public int getNodeId() throws RemoteException;
    public DHTRemote getSuccessor() throws RemoteException;
    public DHTRemote getPredecessor() throws RemoteException;
    public void setSuccessor(DHTRemote succ) throws RemoteException; //maybe remove
    public EdgeRemote getEdgeServer() throws RemoteException;
}
