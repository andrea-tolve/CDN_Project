package com.example.cdn.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OriginRemote extends Remote {
    public void storeContent(
        String contentId,
        byte[] content,
        boolean saveToDisk
    ) throws RemoteException;
    public void deleteContent(String contentId) throws RemoteException;
    public byte[] getContent(String key) throws RemoteException;
    public boolean hasContent(String contentId) throws RemoteException;
}
