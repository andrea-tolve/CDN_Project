package com.example.cdn.rmi.server;

import java.io.File;
import java.nio.file.Files;
import java.rmi.Naming;

public class RunOrigin {

    public static void main(String[] args) {
        OriginServer originServer = null;
        // Initialize the origin server
        try {
            originServer = new OriginServer();
            Naming.rebind("OriginServer", originServer);
            System.out.println("OriginServer bound");
        } catch (Exception e) {
            System.err.println(
                "OriginServer Naming exception: " + e.toString()
            );
            e.printStackTrace();
        }
        try {
            File resDir = new File("res");
            if (!resDir.exists()) {
                resDir.mkdir(); // Create the directory if it doesn't exist
            }
            for (File file : resDir.listFiles()) {
                byte[] content = Files.readAllBytes(file.toPath());
                originServer.storeContent(file.getName(), content, false);
            }
        } catch (Exception e) {
            System.err.println("OriginServer exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
