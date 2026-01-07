package com.example.cdn.rmi.server;

import com.example.cdn.rmi.dht.DHTRemote;
import com.example.cdn.rmi.server.Cache;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

public class EdgeServer extends UnicastRemoteObject implements EdgeRemote {

    private String serverAddress = "//localhost/OriginServer";
    private String serverId;
    private Cache cache;
    private DHTRemote dhtNode;
    private OriginRemote originServer;
    private EdgeRemote edgeServer;

    public EdgeServer(String serverId, Cache cache) throws RemoteException {
        super();
        this.serverId = serverId;
        this.cache = cache;
        try {
            this.originServer = (OriginRemote) Naming.lookup(serverAddress);
        } catch (Exception e) {
            System.err.println("EdgeServer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setDHTNode(DHTRemote stub) {
        this.dhtNode = stub;
    }

    public boolean hasContent(String contentId) throws RemoteException {
        return cache.hasContent(contentId);
    }

    public byte[] getContent(String contentId) throws RemoteException {
        if (cache.hasContent(contentId)) {
            return cache.get(contentId);
        } else {
            //search in DHT
            edgeServer = dhtNode.lookup(contentId);
            if (edgeServer != null && edgeServer != this) {
                return edgeServer.getContent(contentId);
            } else {
                byte[] content = originServer.getContent(contentId);
                String oldKey = cache.put(contentId, content);
                if (oldKey != "") dhtNode.remove(oldKey);
                dhtNode.add(contentId);
                return content;
            }
        }
    }
}
