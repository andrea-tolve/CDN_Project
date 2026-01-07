package com.example.cdn.rmi.client;

import com.example.cdn.rmi.lb.LoadBalancerRemote;
import com.example.cdn.rmi.server.EdgeRemote;
import java.rmi.server.UnicastRemoteObject;

public class MainClient {

    public static void main(String[] args) {
        Client client = new Client(1);
        client.requestContent("Hello.txt");
        byte[] content = client.getContent();
        System.out.println(new String(content)); //suppose that files are a text files
    }
}
