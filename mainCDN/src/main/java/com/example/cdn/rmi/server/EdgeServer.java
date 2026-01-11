package com.example.cdn.rmi.server;

import com.example.cdn.rmi.dht.DHTRemote;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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
            //search in cache
            System.out.println("Get it from cache of " + serverId);
            dhtNode.add(contentId);
            return cache.get(contentId);
        } else {
            //search in DHT
            edgeServer = dhtNode.lookup(contentId);
            if (edgeServer != null && edgeServer != this) {
                System.out.println("Get it from " + edgeServer.getServerId());
                byte[] content = edgeServer.getContent(contentId);
                addInCache(contentId, content);
                return content;
            } else {
                //retrive in origin server
                byte[] content = originServer.getContent(contentId);
                if (content == null) {
                    System.out.println("Content not found");
                    return null;
                }
                addInCache(contentId, content);
                dhtNode.add(contentId);
                System.out.println("Get it from origin server");
                return content;
            }
        }
    }

    public boolean storeContent(String contentId, byte[] content)
        throws RemoteException {
        if (!originServer.hasContent(contentId)) {
            originServer.storeContent(contentId, content, true);
            return true;
        }
        return false;
    }

    public String getServerId() throws RemoteException {
        return serverId;
    }

    private void addInCache(String contentId, byte[] content) {
        String oldKey = cache.put(contentId, content);
        try {
            if (oldKey != null) dhtNode.remove(oldKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            UnicastRemoteObject.unexportObject(this, true);
            this.dhtNode.leave();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
