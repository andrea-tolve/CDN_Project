package com.example.cdn.rmi.server;

import com.example.cdn.rmi.dht.DHTRemote;
import com.example.cdn.rmi.server.Cache;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

public class EdgeServer extends UnicastRemoteObject implements EdgeRemote {

    //private String serveredg = "//localhost/EdgeServer";
    private String serverId;
    private Cache cache;
    private Set<String> respKeys;
    private DHTRemote dhtNode;
    private OriginRemote originServer;
    private EdgeRemote edgeServer;

    public EdgeServer(
        String serverId,
        Cache cache,
        Set<String> respKeys,
        DHTRemote dhtNode,
        OriginRemote originServer
    ) throws RemoteException {
        this.serverId = serverId;
        this.cache = cache;
        this.respKeys = respKeys;
        this.dhtNode = dhtNode;
        this.originServer = originServer;
    }

    public boolean hasContent(String contentId) throws RemoteException {
        return cache.hasContent(contentId);
    }

    public byte[] getContent(String contentId) throws RemoteException {
        return cache.get(contentId);
    }
}
