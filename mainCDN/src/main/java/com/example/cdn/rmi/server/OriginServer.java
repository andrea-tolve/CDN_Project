package com.example.cdn.rmi.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class OriginServer extends UnicastRemoteObject implements OriginRemote {

    private Map<String, byte[]> storage;

    public OriginServer() throws RemoteException {
        super();
        storage = new HashMap<>();
    }

    public void storeContent(String key, byte[] content)
        throws RemoteException {
        storage.put(key, content);
    }

    public void deleteContent(String key) throws RemoteException {
        storage.remove(key);
    }

    public byte[] getContent(String key) throws RemoteException {
        if (storage.containsKey(key)) {
            return storage.get(key);
        }
        return null;
    }
}
