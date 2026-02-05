package com.example.cdn.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cdn.rmi.client.Client;
import com.example.cdn.rmi.dht.DHTNode;
import com.example.cdn.rmi.lb.LoadBalancer;
import com.example.cdn.rmi.server.Cache;
import com.example.cdn.rmi.server.EdgeServer;
import com.example.cdn.rmi.server.OriginServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IntegrationTest {

    private static OriginServer origin;
    private static EdgeServer edge1;
    private static EdgeServer edge2;
    private static DHTNode node1;
    private static DHTNode node2;
    private static LoadBalancer lb;

    @BeforeEach
    public void setUp() throws Exception {
        origin = new OriginServer();
        edge1 = new EdgeServer("edge-1", new Cache(2));
        edge2 = new EdgeServer("edge-2", new Cache(2));

        edge1.setOriginServer(origin);
        edge2.setOriginServer(origin);

        node1 = new DHTNode("edge-1", edge1);
        node2 = new DHTNode("edge-2", edge2);

        edge1.setDHTNode(node1);
        edge2.setDHTNode(node2);

        node1.join(null);
        node2.join(node1);

        lb = new LoadBalancer();

        lb.addEdge(edge1);
        lb.addEdge(edge2);
    }

    @Test
    public void client_fetches_from_origin_then_cache_and_dht()
        throws Exception {
        origin.storeContent("A.txt", "A".getBytes(), false);
        origin.storeContent("B.txt", "B".getBytes(), false);

        Client client = new Client(100, lb);

        // At first request, edge1 will fetch from origin and cache the content
        client.requestContent("A.txt");
        assertArrayEquals(
            "A".getBytes(),
            client.getContent(),
            "A.txt needs to be retrieved"
        );

        // Second fetch of A.txt: edge1 will serve from cache, no origin
        client.requestContent("A.txt");
        assertArrayEquals(
            "A".getBytes(),
            client.getContent(),
            "A.txt needs to be served from cache"
        );

        // Get B.txt from edge2
        Client client2 = new Client(101, lb);
        client2.requestContent("B.txt");
        assertArrayEquals(
            "B".getBytes(),
            client2.getContent(),
            "B.txt needs to be retrieved"
        );

        // Get A.txt from edge1's cache using DHT
        client2.requestContent("A.txt");
        assertArrayEquals(
            "A".getBytes(),
            client2.getContent(),
            "A.txt via DHT/peer"
        );
    }

    @Test
    public void client_storeContent() throws Exception {
        Client client = new Client(102, lb);
        client.storeContent("C.txt", "C".getBytes());
        assertArrayEquals(
            "C".getBytes(),
            origin.getContent("C.txt"),
            "C.txt needs to be stored"
        );

        Client client2 = new Client(103, lb);
        client2.requestContent("C.txt");
        assertArrayEquals(
            "C".getBytes(),
            client2.getContent(),
            "C.txt needs to be retrieved"
        );
    }

    @AfterAll
    public static void shutdown() {
        try {
            edge1.shutdown();
            edge2.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
