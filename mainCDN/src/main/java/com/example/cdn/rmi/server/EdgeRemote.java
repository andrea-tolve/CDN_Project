package com.example.cdn.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EdgeRemote extends Remote {
    byte[] getContent(String contentId) throws RemoteException;
    boolean hasContent(String contentId) throws RemoteException;
    String getServerId() throws RemoteException;
    boolean storeContent(String contentId, byte[] content)
        throws RemoteException;
}
