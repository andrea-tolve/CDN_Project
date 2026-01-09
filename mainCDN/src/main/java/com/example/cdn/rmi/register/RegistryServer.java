package com.example.cdn.rmi.register;

import com.example.cdn.rmi.client.Client;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class RegistryServer
    extends UnicastRemoteObject
    implements RegistryRemote
{

    private AtomicInteger clientId;
    private Map<Integer, String> clientTokens;

    public RegistryServer() throws RemoteException {
        super();
        clientId = new AtomicInteger(0);
        clientTokens = new HashMap<>();
    }

    /**
     * Registers a new client and returns its ID.
     */
    public Client register() throws RemoteException {
        int id = clientId.incrementAndGet();
        String token = UUID.randomUUID().toString();
        clientTokens.put(id, token);

        return new Client(id, token);
    }

    public String getClientToken(int id) throws RemoteException {
        return clientTokens.get(id);
    }

    public void updateClientToken(int id) throws RemoteException {
        //TODO: implement a better token generation strategy
        String token = UUID.randomUUID().toString();
        clientTokens.put(id, token);
    }

    /**
     * Unregisters a client by its ID.
     * Frees up the ID for reuse.
     */
    public void unregister(int id) throws RemoteException {
        clientTokens.remove(id);
    }
}
