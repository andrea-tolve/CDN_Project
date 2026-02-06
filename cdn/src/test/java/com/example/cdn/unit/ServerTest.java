package com.example.cdn.unit;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cdn.rmi.server.Cache;
import com.example.cdn.rmi.server.EdgeServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServerTest {

    private FakeOriginRemote origin;
    private FakeDHTRemote dht;

    @BeforeEach
    void setup() {
        origin = new FakeOriginRemote();
        dht = new FakeDHTRemote();
    }

    @Test
    public void edgeServer_servesFromOrigin_thenFromCache() throws Exception {
        String id = "edge-A";
        Cache cache = new Cache(2);
        EdgeServer edge = new EdgeServer(id, cache, origin);
        edge.setDHTNode(dht); // public setter for DHT

        String contentId = "asset-3";
        byte[] bytes = "payload".getBytes();
        origin.storeContent(contentId, bytes, false);

        // First call should fetch from origin (since cache is empty)
        byte[] first = edge.getContent(contentId);

        assertArrayEquals(bytes, first);
        assertEquals(
            1,
            origin.getCalls,
            "Should have called origin exactly once on first miss"
        );
        assertTrue(
            dht.added.contains(contentId),
            "DHT.add should be called after retrieval"
        );

        // Second call should be served from cache, not calling origin again
        origin.getCalls = 0; // reset counter to observe a new call
        byte[] second = edge.getContent(contentId);

        assertArrayEquals(bytes, second);
        assertEquals(0, origin.getCalls, "Cache hit should not call origin");
        assertTrue(
            dht.added.contains(contentId),
            "DHT.add is also called on cache hits"
        );
    }

    @Test
    public void edgeServer_fetchFromPeerViaDHT_withoutTouchingOrigin()
        throws Exception {
        EdgeServer edge = new EdgeServer("edge-B", new Cache(2), origin);
        edge.setDHTNode(dht);

        String contentId = "asset-4";
        byte[] peerData = "from-peer".getBytes();

        FakeEdgeRemote peer = new FakeEdgeRemote("peer-3");
        peer.seed(contentId, peerData);
        dht.map(contentId, peer); // suppose that the peer is responsible for the content

        byte[] result = edge.getContent(contentId);

        assertArrayEquals(peerData, result);
        assertEquals(
            0,
            origin.getCalls,
            "Should not call origin when DHT returns a peer"
        );
        assertEquals(1, peer.getCalls, "Should call peer to fetch content");
        // In this branch, EdgeServer does NOT call dht.add(...)
        assertFalse(
            dht.added.contains(contentId),
            "DHT.add should not be called when fetched from peer"
        );
    }

    @Test
    public void edgeServer_eviction_removesOldKeyFromDHT() throws Exception {
        EdgeServer edge = new EdgeServer("edge-C", new Cache(2), origin);
        edge.setDHTNode(dht);

        // Seed origin for three different contents
        origin.storeContent("A", "A".getBytes(), false);
        origin.storeContent("B", "B".getBytes(), false);
        origin.storeContent("C", "C".getBytes(), false);

        // Request A and B (fills cache), then C (evicts A, the eldest)
        assertArrayEquals("A".getBytes(), edge.getContent("A"));
        assertArrayEquals("B".getBytes(), edge.getContent("B"));
        assertArrayEquals("C".getBytes(), edge.getContent("C"));

        // Eviction should have triggered dht.remove("A")
        assertTrue(
            dht.removed.contains("A"),
            "Evicting eldest should remove its key from DHT"
        );
        assertFalse(
            dht.removed.contains("B"),
            "B should not yet be removed with capacity=2 and three inserts"
        );
    }

    @Test
    public void edgeServer_notStoreDuplicateContent() throws Exception {
        EdgeServer edge = new EdgeServer("edge-D", new Cache(2), origin);
        edge.setDHTNode(dht);

        String contentId = "asset-4";
        byte[] peerData = "payload".getBytes();

        assertTrue(
            edge.storeContent(contentId, peerData),
            "Content should be stored"
        );
        assertFalse(
            edge.storeContent(contentId, peerData),
            "Duplicate content should not be stored"
        );
    }
}
