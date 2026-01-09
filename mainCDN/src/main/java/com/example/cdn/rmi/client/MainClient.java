package com.example.cdn.rmi.client;

import com.example.cdn.rmi.register.RegistryRemote;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.util.Scanner;

public class MainClient {

    public static void main(String[] args) {
        Client client;
        RegistryRemote registry;
        try {
            registry = (RegistryRemote) Naming.lookup(
                "//localhost/RegistryServer"
            );
            client = registry.register();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String clientDir = "data/client_" + client.getClientId();
        createDirectory("data/");
        createDirectory(clientDir);
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        do {
            System.out.print("Enter file name: ");
            String fileName = scanner.nextLine();
            client.requestContent(fileName);
            byte[] content = client.getContent();
            if (content == null) {
                System.out.println("Content not found");
                continue;
            }
            File targetFile = new File(clientDir, fileName);
            if (targetFile.exists()) {
                System.out.print(
                    "File already exists in client directory. Overwrite? (y/n): "
                );
                String overwrite = scanner.nextLine();
                if (!overwrite.equalsIgnoreCase("y")) {
                    System.out.println("Skipping download.");
                    continue;
                }
            }
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(content);
                System.out.println("Saved to: " + targetFile.getAbsolutePath());
            } catch (IOException ioEx) {
                System.out.println("Failed to save file: " + ioEx.getMessage());
                continue;
            }
            System.out.print("Do you want to continue? (y/n): ");
            String choice = scanner.nextLine();
            exit = choice.equalsIgnoreCase("n");
        } while (!exit);
        scanner.close();
        try {
            registry.unregister(client.getClientId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}
