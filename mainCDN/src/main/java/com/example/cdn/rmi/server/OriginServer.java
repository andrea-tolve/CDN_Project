package com.example.cdn.rmi.server;

import java.io.File;
import java.io.FileOutputStream;
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

    public void storeContent(String key, byte[] content, boolean saveToDisk)
        throws RemoteException {
        storage.put(key, content);
        if (saveToDisk) {
            // Save content to disk
            File file = new File("res/" + key);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    public boolean hasContent(String contentId) throws RemoteException {
        return storage.containsKey(contentId);
    }
}
