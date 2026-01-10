package com.example.cdn.rmi.client;

import com.example.cdn.rmi.register.RegistryRemote;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.Naming;
import java.util.Scanner;

public class MainClient {

    public static void main(String[] args) {
        // Client runtime
        Client client;
        RegistryRemote registry;
        try {
            registry = (RegistryRemote) Naming.lookup(
                "//localhost/RegistryServer"
            );
            int id = registry.register();
            client = new Client(id);
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
            System.out.println();
            System.out.println("Select an option:");
            System.out.println("1) Upload a new file");
            System.out.println("2) Retrieve a file");
            System.out.println("3) Exit");
            System.out.print("Choice: ");
            String action = scanner.nextLine();

            switch (action) {
                case "1": {
                    System.out.print("Enter local file path to upload: ");
                    String localPathStr = scanner.nextLine();
                    Path localPath = Path.of(localPathStr);
                    if (
                        !Files.exists(localPath) ||
                        !Files.isRegularFile(localPath)
                    ) {
                        System.out.println(
                            "Local file does not exist or is not a regular file: " +
                                localPath
                        );
                        break;
                    }
                    String fileName = localPath.getFileName().toString();
                    try {
                        byte[] bytes = Files.readAllBytes(localPath);
                        if (client.storeContent(fileName, bytes)) {
                            System.out.println(
                                "File uploaded to edge server: " + fileName
                            );
                        } else {
                            System.out.println(
                                "Failed to upload file to edge server: " +
                                    fileName +
                                    ", maybe it's already stored"
                            );
                        }
                    } catch (IOException ioEx) {
                        System.out.println(
                            "Failed to read local file: " + ioEx.getMessage()
                        );
                    }
                    break;
                }
                case "2": {
                    System.out.print("Enter file name to retrieve: ");
                    String fileName = scanner.nextLine();
                    client.requestContent(fileName);
                    byte[] content = client.getContent();
                    if (content == null) {
                        System.out.println("Content not found");
                        break;
                    }
                    File targetFile = new File(clientDir, fileName);
                    if (targetFile.exists()) {
                        System.out.print(
                            "File already exists in client directory. Overwrite? (y/n): "
                        );
                        String overwrite = scanner.nextLine();
                        if (!overwrite.equalsIgnoreCase("y")) {
                            System.out.println("Skipping download.");
                            break;
                        }
                    }
                    try (
                        FileOutputStream fos = new FileOutputStream(targetFile)
                    ) {
                        fos.write(content);
                        System.out.println(
                            "Saved to: " + targetFile.getAbsolutePath()
                        );
                    } catch (IOException ioEx) {
                        System.out.println(
                            "Failed to save file: " + ioEx.getMessage()
                        );
                    }
                    break;
                }
                case "3": {
                    exit = true;
                    break;
                }
                default:
                    System.out.println(
                        "Invalid choice. Please select 1, 2, or 3."
                    );
            }
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
