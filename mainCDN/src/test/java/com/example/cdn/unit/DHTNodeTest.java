package com.example.cdn.unit;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cdn.rmi.dht.DHTNode;
import com.example.cdn.rmi.dht.DHTRemote;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class DHTNodeTest {

    private static int sha1ToInt(String identifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(
                identifier.getBytes(StandardCharsets.UTF_8)
            );
            int val = ByteBuffer.wrap(digest).getInt();
            return val & 0x7fffffff;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void cleanup() {}

    @Test
    void single_node_add_lookup_remove() throws Exception {
        FakeEdgeRemote e1 = new FakeEdgeRemote("edge-1");
        DHTNode n1 = new DHTNode("edge-1", e1);
        n1.join(null);

        String key = "content-1";
        n1.add(key);
        assertTrue(n1.hasKey(key));

        assertSame(e1, n1.lookup(key));
        n1.remove(key);
        assertFalse(n1.hasKey(key));
        assertNull(n1.lookup(key));

        n1.leave();
    }

    @Test
    void two_nodes_add_and_lookup_goes_to_correct_successor() throws Exception {
        FakeEdgeRemote e1 = new FakeEdgeRemote("edge-1");
        FakeEdgeRemote e2 = new FakeEdgeRemote("edge-2");
        DHTNode n1 = new DHTNode("edge-1", e1);
        DHTNode n2 = new DHTNode("edge-2", e2);

        // 2 nodes ring
        n1.join(null);
        n2.join(n1);

        // Find a key that will be in the range of n1's successor
        String chosen = null;
        DHTRemote succ = null;
        for (int i = 0; i < 10000; i++) {
            String candidate = "k-" + i;
            int id = sha1ToInt(candidate);
            succ = n1.findSuccessor(id);
            if (succ != null) {
                chosen = candidate;
                break;
            }
        }
        assertNotNull(chosen, "No suitable key found");

        // Try to add the key to both nodes
        n1.add(chosen);
        n2.add(chosen);

        boolean inN1 = n1.hasKey(chosen);
        boolean inN2 = n2.hasKey(chosen);
        assertTrue(inN1 ^ inN2, "Exactly one node should keep the key");

        // Must be the successor of the other node
        if (inN1) {
            assertSame(e1, n2.lookup(chosen));
        } else {
            assertSame(e2, n1.lookup(chosen));
        }

        n2.leave();
        n1.leave();
    }
}
