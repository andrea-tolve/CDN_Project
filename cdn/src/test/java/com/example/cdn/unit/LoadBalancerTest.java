package com.example.cdn.unit;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cdn.rmi.lb.LoadBalancer;
import java.rmi.RemoteException;
import org.junit.jupiter.api.Test;

public class LoadBalancerTest {

    @Test
    public void loadbalancer_noAvailableServer() throws Exception {
        LoadBalancer loadBalancer = new LoadBalancer();
        assertThrows(RemoteException.class, () ->
            loadBalancer.getEdgeWithLeastConnections()
        );
    }
}
