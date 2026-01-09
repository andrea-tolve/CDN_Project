package com.example.cdn.rmi.register;

import com.example.cdn.rmi.client.Client;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistryRemote extends Remote {
    Client register() throws RemoteException;
    void unregister(int id) throws RemoteException;
    String getClientToken(int id) throws RemoteException;
    void updateClientToken(int id) throws RemoteException;
}
