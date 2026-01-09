package com.example.cdn.rmi.server;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cache {

    private final int capacity;
    private final LinkedHashMap<String, byte[]> entries; // LinkedHashMap consists of entries in insertion order
    private String oldKey;

    public Cache(int capacity) {
        this.capacity = capacity;
        this.entries = new LinkedHashMap<String, byte[]>(
            capacity,
            0.75f,
            true
        ) {
            @Override
            protected boolean removeEldestEntry(
                Map.Entry<String, byte[]> eldest
            ) {
                if (size() > Cache.this.capacity) {
                    oldKey = eldest.getKey();
                    return true;
                }
                return false;
            }
        };
    }

    public boolean hasContent(String contentId) {
        return entries.containsKey(contentId);
    }

    public byte[] get(String key) {
        return entries.get(key);
    }

    public String put(String key, byte[] value) {
        entries.put(key, value);
        return oldKey;
    }

    public void remove(String key) {
        entries.remove(key);
    }
}
