package com.example.cdn.rmi.dht;

import com.example.cdn.rmi.server.EdgeRemote;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;

public class DHTNode extends UnicastRemoteObject implements DHTRemote {

    public static final int FINGER_TABLE_SIZE = 31; // For integer

    // Use 31-bit non-negative ID space
    public static final int ID_SPACE_MASK = 0x7fffffff;

    private int nodeId;
    private EdgeRemote edgeServer;
    private SortedMap<Integer, DHTRemote> fingerTable;
    private HashSet<String> keySet;
    private DHTRemote successor;
    private DHTRemote predecessor;

    /**
     * Construct a DHTNode by hashing an identifier (e.g. hostname, IP:port, or any string)
     * into a 32-bit node id using SHA-1. The first 4 bytes of the SHA-1 digest are used
     * and the sign bit is cleared to produce a non-negative int.
     */
    public DHTNode(String serverId, EdgeRemote edgeServer)
        throws RemoteException {
        super();
        this.nodeId = sha1ToInt(serverId);
        this.fingerTable = new TreeMap<>();
        this.keySet = new HashSet<>();
        this.successor = null;
        this.predecessor = null;
        this.edgeServer = edgeServer;
    }

    /**
     * Hash the given identifier with SHA-1 and convert the first 4 bytes of the digest
     * to a non-negative int.
     *
     * @param identifier the identifier to hash
     * @return a non-negative 32-bit node id derived from SHA-1(identifier)
     */
    private static int sha1ToInt(String identifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(
                identifier.getBytes(StandardCharsets.UTF_8)
            );
            ByteBuffer bb = ByteBuffer.wrap(digest);
            int val = bb.getInt(); // take first 4 bytes
            return val & ID_SPACE_MASK; // make non-negative
        } catch (NoSuchAlgorithmException e) {
            // SHA-1 is expected to be available on the JVM; rethrow as unchecked if not.
            throw new RuntimeException("SHA-1 algorithm not available", e);
        }
    }

    public void join(DHTRemote bootstrapNode) throws RemoteException {
        // Chord-style join:
        // - if bootstrapNode is null => first node in the network: successor = this
        // - otherwise ask bootstrapNode to find the successor of this.nodeId
        if (bootstrapNode == null) {
            // first node in the ring
            this.successor = this;
            this.predecessor = this;
        } else {
            // find successor for this node id using bootstrap
            stabilize(bootstrapNode);
            bootstrapNode.stabilize(this);
        }
        computeFingerTable();
        if (
            this.successor != null && this.successor != this
        ) this.successor.computeFingerTable();
        if (
            this.predecessor != null && this.predecessor != this
        ) this.predecessor.computeFingerTable();
    }

    public void computeFingerTable() {
        // Compute finger table entries
        for (int i = 0; i < FINGER_TABLE_SIZE; i++) {
            int fingerId = (int) (((long) this.nodeId + (1L << i)) &
                ID_SPACE_MASK);
            try {
                fingerTable.put(i, findSuccessor(fingerId));
            } catch (RemoteException e) {
                // ignore for now; stabilize will fix relationships
            }
        }
    }

    public DHTRemote findSuccessor(int id) throws RemoteException {
        // If successor is null, we're alone
        DHTRemote succ = this.getSuccessor();
        if (succ == null) {
            return this;
        }
        int succId;
        try {
            succId = succ.getNodeId();
        } catch (RemoteException e) {
            // couldn't get successor id; treat self as successor as fallback
            return this;
        }

        if (inInterval(this.nodeId, succId, id, true)) {
            return succ;
        } else {
            DHTRemote n0 = this.closestPrecedingFinger(id);
            if (n0 == null) {
                return this;
            }
            // If closest preceding finger is self, avoid infinite recursion
            try {
                if (n0.getNodeId() == this.nodeId) {
                    return this;
                }
            } catch (RemoteException e) {
                return this;
            }
            // recursive lookup forwarded to n0
            return n0.findSuccessor(id);
        }
    }

    public DHTRemote closestPrecedingFinger(int id) throws RemoteException {
        // iterate finger table from highest to lowest
        try {
            TreeMap<Integer, DHTRemote> tm = (TreeMap<
                Integer,
                DHTRemote
            >) this.fingerTable;
            for (DHTRemote node : tm.descendingMap().values()) {
                if (node == null) continue;
                int nid;
                try {
                    nid = node.getNodeId();
                } catch (RemoteException e) {
                    continue; // skip unreachable
                }
                if (inInterval(this.nodeId, id, nid, false)) {
                    return node;
                }
            }
        } catch (ClassCastException e) {
            // fallback: iterate in insertion order
            for (DHTRemote node : this.fingerTable.values()) {
                if (node == null) continue;
                int nid;
                try {
                    nid = node.getNodeId();
                } catch (RemoteException ex) {
                    continue;
                }
                if (inInterval(this.nodeId, id, nid, false)) {
                    return node;
                }
            }
        }
        // no preceding finger found
        return this;
    }

    public void notify(DHTRemote node) throws RemoteException {
        // Another node thinks it might be our predecessor.
        // If we don't have a predecessor, or node is between predecessor and this node, update.
        if (node == null) return;
        if (this.predecessor == null) {
            this.predecessor = node;
            return;
        }
        DHTRemote pred;
        int nodeIdRemote;
        try {
            pred = this.predecessor;
            nodeIdRemote = node.getNodeId();
        } catch (RemoteException e) {
            return;
        }
        // if node is in (pred, this), update predecessor
        if (inInterval(pred.getNodeId(), this.nodeId, nodeIdRemote, false)) {
            this.predecessor = node;
        }
    }

    public void stabilize(DHTRemote bootstrapNode) throws RemoteException {
        // - ask successor for its predecessor
        // - if that predecessor is between this and successor, update successor
        // - notify successor about ourselves
        this.successor = bootstrapNode.findSuccessor(this.nodeId);
        if (this.successor != null) {
            try {
                //check if there is a better successor
                DHTRemote x = this.successor.getPredecessor();
                if (x != null && x != this && x != this.successor) {
                    int succId = this.successor.getNodeId();
                    int xId = x.getNodeId();
                    if (inInterval(this.nodeId, succId, xId, false)) {
                        this.successor = x;
                    }
                }
                this.successor.notify(this); //update successor's predecessor
            } catch (Exception e) {
                // ignore and continue
            }
        }
    }

    /**
     * Helper: check if id is in the circular interval (start, end)
     * If inclusiveEnd is true, interval is (start, end] else (start, end)
     */
    private boolean inInterval(
        int start,
        int end,
        int id,
        boolean inclusiveEnd
    ) {
        if (start < end) {
            if (inclusiveEnd) {
                return id > start && id <= end;
            } else {
                return id > start && id < end;
            }
        } else if (start > end) {
            // wrap-around
            if (inclusiveEnd) {
                return id > start || id <= end;
            } else {
                return id > start || id < end;
            }
        } else {
            return true;
        }
    }

    public EdgeRemote lookup(String contentId) throws RemoteException {
        // Map the key to an id in the identifier space and find its successor node
        int id = sha1ToInt(contentId);
        DHTRemote successor = findSuccessor(id);
        if (successor.hasKey(contentId)) return successor.getEdge();
        else return null;
    }

    public void add(String contentId) throws RemoteException {
        // Store resource in dht only if this is the correct node
        int id = sha1ToInt(contentId);
        DHTRemote successor = findSuccessor(id); //test from here which successor is returned
        if (successor == this && !successor.hasKey(contentId)) {
            System.out.println(
                "Edge: " +
                    this.edgeServer.getServerId() +
                    " with id " +
                    this.nodeId +
                    ", Adding contentId: " +
                    contentId +
                    " with id " +
                    id
            );
            this.keySet.add(contentId);
        }
    }

    public void remove(String contentId) throws RemoteException {
        //remove only if it is stored in this node
        if (this.hasKey(contentId)) {
            this.keySet.remove(contentId);
            System.out.println(
                "Edge: " +
                    this.edgeServer.getServerId() +
                    ", Removing contentId: " +
                    contentId
            );
        }
    }

    public void leave() throws RemoteException {
        // When leaving remove keys from this node
        this.keySet.clear();
        DHTRemote succ = this.successor;
        DHTRemote pred = this.predecessor;
        if (succ != null) {
            succ.setPredecessor(pred);
            succ.computeFingerTable();
        }
        if (pred != null) {
            pred.setSuccessor(succ);
            pred.computeFingerTable();
        }
        this.successor = null;
        this.predecessor = null;
        UnicastRemoteObject.unexportObject(this, true);
    }

    public int getNodeId() throws RemoteException {
        return this.nodeId;
    }

    public DHTRemote getSuccessor() throws RemoteException {
        return this.successor;
    }

    public DHTRemote getPredecessor() throws RemoteException {
        return this.predecessor;
    }

    public EdgeRemote getEdge() throws RemoteException {
        return this.edgeServer;
    }

    public void setSuccessor(DHTRemote succ) throws RemoteException {
        this.successor = succ;
    }

    public void setPredecessor(DHTRemote pred) throws RemoteException {
        this.predecessor = pred;
    }

    public boolean hasKey(String contentId) throws RemoteException {
        return this.keySet.contains(contentId);
    }
}
