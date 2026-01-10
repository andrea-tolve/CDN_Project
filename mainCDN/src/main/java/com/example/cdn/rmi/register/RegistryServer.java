package com.example.cdn.rmi.register;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RegistryServer
    extends UnicastRemoteObject
    implements RegistryRemote
{

    private AtomicInteger clientId;
    private PriorityQueue<Integer> freeIds;

    public RegistryServer() throws RemoteException {
        super();
        clientId = new AtomicInteger(0);
        freeIds = new PriorityQueue<>();
    }

    public int register() throws RemoteException {
        Integer reused = freeIds.poll();
        int id = (reused != null) ? reused : clientId.incrementAndGet();
        return id;
    }

    public void unregister(int id) throws RemoteException {
        freeIds.offer(id);
    }
}
