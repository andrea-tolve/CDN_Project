package com.example.cdn.rmi.client;

import com.example.cdn.rmi.server.OriginRemote;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.util.Scanner;

public class MainClient {

    public static void main(String[] args) {
        int id;
        try {
            OriginRemote originServer = (OriginRemote) Naming.lookup(
                "//localhost/OriginServer"
            );
            id = originServer.registerClient();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }
        Client client = new Client(id);
        String clientDir = "data/client_" + id;
        createDirectory("data/");
        createDirectory(clientDir);
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        do {
            System.out.print("Enter file name: ");
            String fileName = scanner.nextLine();
            if (!fileName.toLowerCase().endsWith(".txt")) {
                System.out.println("Only .txt files are allowed.");
                continue;
            }
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
            System.out.println(new String(content)); // suppose that files are text files
            System.out.print("Do you want to continue? (y/n): ");
            String choice = scanner.nextLine();
            exit = choice.equalsIgnoreCase("n");
        } while (!exit);
        scanner.close();
    }

    private static void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}
