package com.example.cdn.rmi.client;

import com.example.cdn.rmi.lb.LoadBalancerRemote;
import com.example.cdn.rmi.server.EdgeRemote;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;

public class Client implements ClientRemote, Serializable {

    private String serverlb = "//localhost/LoadBalancerService";
    private int clientId;
    private EdgeRemote server;
    private LoadBalancerRemote loadBalancer;

    public Client(int clientId, LoadBalancerRemote loadBalancer)
        throws RemoteException {
        this.clientId = clientId;
        this.server = null;
        this.loadBalancer = (LoadBalancerRemote) Naming.lookup(serverlb);
    }

    public void requestContent(String contentId) throws RemoteException {
        server = loadBalancer.getEdgeWithLeastConnections();
        server.getContent(contentId);
    }
}
