package com.example.cdn.rmi.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class OriginServer extends UnicastRemoteObject implements OriginRemote {

    private Map<String, byte[]> storage;
    private AtomicInteger clientId;

    public OriginServer() throws RemoteException {
        super();
        clientId = new AtomicInteger(0);
        storage = new HashMap<>();
    }

    public void storeContent(String key, byte[] content)
        throws RemoteException {
        storage.put(key, content);
    }

    public void deleteContent(String key) throws RemoteException {
        //Future feature
    }

    public byte[] getContent(String key) throws RemoteException {
        if (storage.containsKey(key)) {
            return storage.get(key);
        }
        return null;
    }

    public int registerClient() throws RemoteException {
        return clientId.incrementAndGet();
    }
}
