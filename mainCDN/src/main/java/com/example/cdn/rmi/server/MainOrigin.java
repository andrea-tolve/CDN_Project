package com.example.cdn.rmi.server;

import java.io.File;
import java.nio.file.Files;
import java.rmi.Naming;

public class MainOrigin {

    public static void main(String[] args) {
        // Initialize the origin server
        try {
            OriginServer originServer = new OriginServer();
            for (File file : new File("res").listFiles()) {
                byte[] content = Files.readAllBytes(file.toPath());
                originServer.storeContent(file.getName(), content, false);
            }
            Naming.rebind("OriginServer", originServer);
            System.out.println("OriginServer bound");
        } catch (Exception e) {
            System.err.println("OriginServer exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
