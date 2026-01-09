package com.example.cdn.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OriginRemote extends Remote {
    public byte[] getContent(String key) throws RemoteException;
}
