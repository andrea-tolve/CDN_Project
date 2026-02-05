package com.example.cdn.rmi.server;

import com.example.cdn.rmi.dht.*;
import com.example.cdn.rmi.lb.LoadBalancer;
import com.example.cdn.rmi.register.RegistryServer;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

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
        try {
            for (int i = 0; i < edgeServers.length; i++) {
                System.out.println(
                    "Edge server " +
                        i +
                        " bound to DHT node " +
                        stubsDHT[i].getNodeId()
                );
            }
        } catch (RemoteException e) {
            e.printStackTrace();
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

        boolean end = false;
        Scanner scanner = null;
        while (!end) {
            System.out.println("Waiting for user exit input...");
            scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if (input.equals("exit")) {
                end = true;
            }
        }
        System.out.println("Enter the ID of the edge server to shutdown:");
        int id = scanner.nextInt();
        scanner.close();
        //Simulate that one server will go down
        try {
            stubsEdge[id].shutdown();
            System.out.println("Edge server " + id + " shutdown");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
