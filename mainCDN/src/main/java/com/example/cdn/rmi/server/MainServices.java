package com.example.cdn.rmi.server;

import com.example.cdn.rmi.dht.*;
import com.example.cdn.rmi.lb.LoadBalancer;
import com.example.cdn.rmi.register.RegistryServer;
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
                    "EdgeServer" + i,
                    new Cache(cachecapacity)
                );
                // Oggetto già esportato: ottieni lo stub senza riesportare
                stubsEdge[i] = (EdgeRemote) UnicastRemoteObject.toStub(
                    edgeServers[i]
                );
                dhtTable[i] = new DHTNode("DHTNode" + i, stubsEdge[i]);
                if (i == 0) dhtTable[i].join(null);
                else dhtTable[i].join(dhtTable[i - 1]);
                stubsDHT[i] = (DHTRemote) UnicastRemoteObject.toStub(
                    dhtTable[i]
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Associate edge servers with their respective DHT nodes
        for (int i = 0; i < edgeServers.length; i++) {
            edgeServers[i].setDHTNode(stubsDHT[i]);
        }
        System.out.println("Edge servers bound");
        // Initialize the load balancer
        LoadBalancer loadBalancer;
        try {
            loadBalancer = new LoadBalancer();
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }
        try {
            for (int i = 0; i < stubsEdge.length; i++) {
                loadBalancer.addEdge(stubsEdge[i]);
            }
            Naming.rebind("LoadBalancer", loadBalancer);
            System.out.println("LoadBalancer bound");
        } catch (Exception e) {
            System.err.println("LoadBalancer exception: " + e.toString());
            e.printStackTrace();
        }

        //Initialize the Register Service
        RegistryServer registryServer;
        try {
            registryServer = new RegistryServer();
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }
        try {
            Naming.rebind("RegistryServer", registryServer);
            System.out.println("RegisterService bound");
        } catch (Exception e) {
            System.err.println("RegisterService exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
