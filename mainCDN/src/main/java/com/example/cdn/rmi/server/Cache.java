package com.example.cdn.rmi.server;

import java.util.HashMap;

public class Cache {

    private int capacity;
    private HashMap<String, byte[]> entries;

    public Cache(int capacity) {
        this.capacity = capacity;
        this.entries = new HashMap<>();
    }

    public boolean hasContent(String contentId) {
        return entries.containsKey(contentId);
    }

    public byte[] get(String key) {
        return entries.get(key);
    }

    public String put(String key, byte[] value) {
        String old = "";
        if (entries.size() >= capacity) {
            // Least Recently Used (LRU) eviction policy
            String oldestKey = entries.keySet().iterator().next();
            old = oldestKey;
            entries.remove(oldestKey);
        }
        entries.put(key, value);
        return old;
    }

    public void remove(String key) {
        entries.remove(key);
    }
}
