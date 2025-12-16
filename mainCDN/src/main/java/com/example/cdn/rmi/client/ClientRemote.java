package com.example.cdn.rmi.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientRemote extends Remote {
    public void requestContent(String contentId) throws RemoteException;
}
