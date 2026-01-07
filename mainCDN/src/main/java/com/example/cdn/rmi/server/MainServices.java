package com.example.cdn.rmi.server;

import com.example.cdn.rmi.dht.*;
import com.example.cdn.rmi.lb.LoadBalancer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MainServices {

    public static void main(String[] args) {
        // Initialize the edge servers and the DHT table, with their respective stubs
        EdgeServer[] edgeServers;
        DHTNode[] dhtTable;
        try {
            edgeServers = new EdgeServer[3];
            dhtTable = new DHTNode[3];
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        EdgeRemote[] stubsEdge = new EdgeRemote[3];
        DHTRemote[] stubsDHT = new DHTRemote[3];
        int cachecapacity = 10;

        for (int i = 0; i < edgeServers.length; i++) {
            try {
                edgeServers[i] = new EdgeServer(
                    "" + i,
                    new Cache(cachecapacity)
                );
                // Export the edge server as an RMI object
                stubsEdge[i] = (EdgeRemote) UnicastRemoteObject.exportObject(
                    edgeServers[i],
                    0
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                dhtTable[i] = new DHTNode("" + i, stubsEdge[i]);
                // Export the DHT node as an RMI object
                stubsDHT[i] = (DHTRemote) UnicastRemoteObject.exportObject(
                    dhtTable[i],
                    0
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Associate edge servers with their respective DHT nodes
        for (int i = 0; i < edgeServers.length; i++) {
            edgeServers[i].setDHTNode(stubsDHT[i]);
        }

        // Initialize the load balancer
        LoadBalancer loadBalancer;
        try {
            loadBalancer = new LoadBalancer();
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }
        try {
            Naming.rebind("LoadBalancer", loadBalancer);
            System.out.println("LoadBalancer bound");
            for (int i = 0; i < stubsEdge.length; i++) {
                loadBalancer.addEdge(stubsEdge[i]);
            }
        } catch (Exception e) {
            System.err.println("LoadBalancer exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
