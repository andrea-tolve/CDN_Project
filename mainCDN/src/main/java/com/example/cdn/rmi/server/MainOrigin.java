package com.example.cdn.rmi.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.Naming;

public class MainOrigin {

    public static void main(String[] args) {
        // Initialize the origin server
        try {
            OriginServer originServer = new OriginServer();
            File file = new File("../res/Hello.txt");
            byte[] content = Files.readAllBytes(file.toPath());
            originServer.storeContent("Hello.txt", content);
            Naming.rebind("OriginServer", originServer);
            System.out.println("OriginServer bound");
        } catch (Exception e) {
            System.err.println("OriginServer exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
