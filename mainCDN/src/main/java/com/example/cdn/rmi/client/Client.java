package com.example.cdn.rmi.client;

import com.example.cdn.rmi.lb.LoadBalancerRemote;
import com.example.cdn.rmi.server.EdgeRemote;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class Client implements Serializable {

    private String serverlb = "//localhost/LoadBalancer";
    private int clientId;
    private EdgeRemote server;
    private LoadBalancerRemote loadBalancer;
    private byte[] content;

    public Client(int clientId) {
        this.clientId = clientId;
        this.server = null;
        try {
            this.loadBalancer = (LoadBalancerRemote) Naming.lookup(serverlb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestContent(String contentId) {
        try {
            if (server == null) {
                server = loadBalancer.getEdgeWithLeastConnections();
                System.out.println(
                    "Client " +
                        clientId +
                        " connected to server " +
                        server.getServerId()
                );
                if (server == null) {
                    System.err.println("No available server");
                    return;
                }
            }
            content = server.getContent(contentId);
        } catch (RemoteException e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public byte[] getContent() {
        return content;
    }

    public int getClientId() {
        return clientId;
    }
}
