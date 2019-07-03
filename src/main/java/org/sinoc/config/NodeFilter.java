package org.sinoc.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sinoc.net.rlpx.Node;

public class NodeFilter {
    private List<Entry> entries = new ArrayList<>();

    public void add(byte[] nodeId, String hostIpPattern) {
        entries.add(new Entry(nodeId, hostIpPattern));
    }

    public boolean accept(Node node) {
        for (Entry entry : entries) {
            if (entry.accept(node)) return true;
        }
        return false;
    }

    public boolean accept(InetAddress nodeAddr) {
        for (Entry entry : entries) {
            if (entry.accept(nodeAddr)) return true;
        }
        return false;
    }

    private class Entry {
        byte[] nodeId;
        String hostIpPattern;

        public Entry(byte[] nodeId, String hostIpPattern) {
            this.nodeId = nodeId;
            if (hostIpPattern != null) {
                int idx = hostIpPattern.indexOf("*");
                if (idx > 0) {
                    hostIpPattern = hostIpPattern.substring(0, idx);
                }
            }
            this.hostIpPattern = hostIpPattern;
        }

        public boolean accept(InetAddress nodeAddr) {
            if (hostIpPattern == null) return true;
            String ip = nodeAddr.getHostAddress();
            return hostIpPattern != null && ip.startsWith(hostIpPattern);
        }

        public boolean accept(Node node) {
            try {
                boolean shouldAcceptNodeId = nodeId == null || Arrays.equals(node.getId(), nodeId);
                if (!shouldAcceptNodeId) {
                    return false;
                }
                InetAddress nodeAddress = InetAddress.getByName(node.getHost());
                return (hostIpPattern == null || accept(nodeAddress));
            } catch (UnknownHostException e) {
                return false;
            }
        }
    }
}
