package com.example.cdn.rmi;

import com.example.cdn.rmi.client.RunClient;
import com.example.cdn.rmi.server.RunOrigin;
import com.example.cdn.rmi.server.RunServices;
import java.rmi.registry.LocateRegistry;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException(
                "Specify role: registry | origin | edge | client"
            );
        }

        switch (args[0]) {
            case "registry":
                try {
                    startRegistry();
                } catch (Exception e) {
                    System.err.println("CDN Service failed: " + e.toString());
                    e.printStackTrace();
                }
                break;
            case "origin":
                startOrigin();
                break;
            case "services":
                startServices();
                break;
            case "client":
                startClient();
                break;
            default:
                throw new IllegalArgumentException("Unknown role");
        }
    }

    private static void startRegistry() throws Exception {
        LocateRegistry.createRegistry(1099);
        Thread.sleep(Long.MAX_VALUE); // Wait indefinitely
    }

    private static void startOrigin() {
        RunOrigin.main(new String[0]);
    }

    private static void startServices() {
        RunServices.main(new String[0]);
    }

    private static void startClient() {
        RunClient.main(new String[0]);
    }
}
