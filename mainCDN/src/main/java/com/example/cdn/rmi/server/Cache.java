package com.example.cdn.rmi.server;

import java.util.HashMap;

public class Cache {

    private int capacity;
    private HashMap<String, byte[]> entries;

    public Cache(int capacity) {
        this.capacity = capacity;
        this.entries = new HashMap<>();
    }

    public byte[] get(String key) {
        return entries.get(key);
    }

    public void put(String key, byte[] value) {
        if (entries.size() >= capacity) {
            // Least Recently Used (LRU) eviction policy
            String oldestKey = entries.keySet().iterator().next();
            entries.remove(oldestKey);
        }
        entries.put(key, value);
    }

    public void remove(String key) {
        entries.remove(key);
    }
}
