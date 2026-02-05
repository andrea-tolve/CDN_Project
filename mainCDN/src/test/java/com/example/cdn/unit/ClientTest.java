package com.example.cdn.unit;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cdn.rmi.client.Client;
import com.example.cdn.rmi.lb.LoadBalancer;
import com.example.cdn.rmi.register.RegistryServer;
import org.junit.jupiter.api.Test;

public class ClientTest {

    @Test
    public void client_getsIncrementalId_and_getsUsedId() throws Exception {
        RegistryServer registry = new RegistryServer();
        Client client1;
        Client client2;
        Client client3;

        // Register and check clients ids
        client1 = new Client(registry.register());
        assertTrue(
            client1.getClientId() == 1,
            "First client has not the first id"
        );
        client2 = new Client(registry.register());
        assertTrue(
            client2.getClientId() == 2,
            "Second client has not the second id"
        );

        // Unregister the first client and register a new client
        registry.unregister(client1.getClientId());
        client3 = new Client(registry.register());
        assertTrue(
            client3.getClientId() == 1,
            "Third client does not reuse the id"
        );
    }

    @Test
    public void client_getsTheLeastConnectedServer() throws Exception {
        FakeEdgeRemote edgeServer = new FakeEdgeRemote("peer-1");
        FakeEdgeRemote edgeServer2 = new FakeEdgeRemote("peer-2");
        LoadBalancer loadBalancer = new LoadBalancer();

        loadBalancer.addEdge(edgeServer);
        loadBalancer.addEdge(edgeServer2);

        Client client1 = new Client(1, loadBalancer);
        client1.requestContent("asset-1"); // Connection with edgeServer established on request

        Client client2 = new Client(2, loadBalancer);
        client2.requestContent("asset-2");

        // they must have different edge servers
        assertNotEquals(
            client1.getEdgeServer(),
            client2.getEdgeServer(),
            "both client are connected to the same edge server"
        );
    }

    @Test
    public void client_connectsToAServer_and_reconnectsToAnotherServer()
        throws Exception {
        FakeEdgeRemote edgeServer = new FakeEdgeRemote("peer-1");
        FakeEdgeRemote edgeServer2 = new FakeEdgeRemote("peer-2");
        LoadBalancer loadBalancer = new LoadBalancer();

        loadBalancer.addEdge(edgeServer);

        Client client1 = new Client(1, loadBalancer);
        client1.requestContent("asset-1"); // Connection with edgeServer established on request
        assertEquals(
            edgeServer,
            client1.getEdgeServer(),
            "client is not connected to edgeServer"
        );

        loadBalancer.addEdge(edgeServer2);
        Client client2 = new Client(2, loadBalancer);
        client2.requestContent("asset-2");

        // Simulate edgeServer shutdown
        client1.setServer(null);
        loadBalancer.removeEdgePublic(edgeServer);

        // Client should reconnect to edgeServer2
        client1.requestContent("asset-3");
        assertEquals(
            client1.getEdgeServer(),
            client2.getEdgeServer(),
            "client did not reconnect to another server after server shutdown "
        );
    }
}
