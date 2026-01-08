package com.example.cdn.rmi.client;

public class MainClient {

    public static void main(String[] args) {
        Client client = new Client(1);
        client.requestContent("Hello.txt");
        byte[] content = client.getContent();
        System.out.println(new String(content)); //suppose that files are a text files
    }
}
