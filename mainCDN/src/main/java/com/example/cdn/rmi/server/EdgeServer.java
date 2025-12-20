package com.example.cdn.rmi.server;

import com.example.cdn.rmi.server.Cache;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

public class EdgeServer extends UnicastRemoteObject implements EdgeRemote {

    private String serverId;
    private Cache cache;
    private Set<String> respKeys;
    private DHTRemote dhtNode;
    private OriginRemote originServer;
}
