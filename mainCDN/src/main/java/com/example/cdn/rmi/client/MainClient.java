package com.example.cdn.rmi.client;

import java.util.Scanner;

public class MainClient {

    public static void main(String[] args) {
        Client client = new Client(1);
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        do {
            System.out.print("Enter file name: ");
            String fileName = scanner.nextLine();
            client.requestContent(fileName);
            byte[] content = client.getContent();
            System.out.println(new String(content)); //suppose that files are a text files
            System.out.print("Do you want to continue? (y/n): ");
            String choice = scanner.nextLine();
            exit = choice.equalsIgnoreCase("n");
        } while (!exit);
        scanner.close();
    }
}
