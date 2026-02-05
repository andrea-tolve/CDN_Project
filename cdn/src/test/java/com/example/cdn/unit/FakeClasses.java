package com.example.cdn.unit;

import com.example.cdn.rmi.dht.DHTRemote;
import com.example.cdn.rmi.server.EdgeRemote;
import com.example.cdn.rmi.server.OriginRemote;
import java.rmi.RemoteException;
import java.util.*;

/**
 * These classes are used for testing purposes only.
 * They simulate the behavior of the real objects.
 */

final class FakeOriginRemote implements OriginRemote {

    Map<String, byte[]> storage = new HashMap<>();
    int getCalls = 0;
    int storeCalls = 0;
    int hasCalls = 0;

    @Override
    public void storeContent(
        String contentId,
        byte[] content,
        boolean saveToDisk
    ) throws RemoteException {
        storeCalls++;
        storage.put(contentId, content);
    }

    @Override
    public void deleteContent(String contentId) throws RemoteException {
        storage.remove(contentId);
    }

    @Override
    public byte[] getContent(String key) throws RemoteException {
        getCalls++;
        return storage.get(key);
    }

    @Override
    public boolean hasContent(String contentId) throws RemoteException {
        hasCalls++;
        return storage.containsKey(contentId);
    }
}

final class FakeEdgeRemote implements EdgeRemote {

    private final String serverId;
    private final Map<String, byte[]> contents = new HashMap<>(); // internal storage
    int getCalls = 0;

    FakeEdgeRemote(String serverId) {
        this.serverId = serverId;
    }

    void seed(String contentId, byte[] bytes) {
        contents.put(contentId, bytes);
    }

    @Override
    public byte[] getContent(String contentId) throws RemoteException {
        getCalls++;
        return contents.get(contentId);
    }

    @Override
    public boolean hasContent(String contentId) throws RemoteException {
        return contents.containsKey(contentId);
    }

    @Override
    public String getServerId() throws RemoteException {
        return serverId;
    }

    @Override
    public boolean storeContent(String contentId, byte[] content)
        throws RemoteException {
        contents.put(contentId, content);
        return true;
    }

    @Override
    public void shutdown() throws RemoteException {}
}

final class FakeDHTRemote implements DHTRemote {

    final Map<String, EdgeRemote> directory = new HashMap<>();
    final List<String> added = new ArrayList<>();
    final List<String> removed = new ArrayList<>();
    boolean left = false;

    void map(String contentId, EdgeRemote edge) {
        directory.put(contentId, edge);
    }

    @Override
    public DHTRemote findSuccessor(int id) throws RemoteException {
        return null;
    }

    @Override
    public DHTRemote closestPrecedingFinger(int id) throws RemoteException {
        return null;
    }

    @Override
    public EdgeRemote lookup(String contentId) throws RemoteException {
        return directory.get(contentId);
    }

    @Override
    public void join(DHTRemote bootstrapNode) throws RemoteException {}

    @Override
    public void notify(DHTRemote node) throws RemoteException {}

    @Override
    public void add(String contentId) throws RemoteException {
        added.add(contentId);
    }

    @Override
    public void remove(String contentId) throws RemoteException {
        removed.add(contentId);
        directory.remove(contentId);
    }

    @Override
    public boolean hasKey(String contentId) throws RemoteException {
        return directory.containsKey(contentId);
    }

    @Override
    public void leave() throws RemoteException {
        left = true;
    }

    @Override
    public void stabilize(DHTRemote bootstrapNode) throws RemoteException {}

    @Override
    public int getNodeId() throws RemoteException {
        return 0;
    }

    @Override
    public DHTRemote getSuccessor() throws RemoteException {
        return null;
    }

    @Override
    public DHTRemote getPredecessor() throws RemoteException {
        return null;
    }

    @Override
    public EdgeRemote getEdge() throws RemoteException {
        return null;
    }

    @Override
    public void setSuccessor(DHTRemote succ) throws RemoteException {}

    @Override
    public void setPredecessor(DHTRemote pred) throws RemoteException {}

    @Override
    public void computeFingerTable() throws RemoteException {}
}
