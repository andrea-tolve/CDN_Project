package com.example.cdn.rmi.register;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistryRemote extends Remote {
    int register() throws RemoteException;
    void unregister(int id) throws RemoteException;
}
