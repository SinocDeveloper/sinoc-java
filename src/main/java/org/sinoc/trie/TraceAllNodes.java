package org.sinoc.trie;

import org.sinoc.util.Value;
import org.spongycastle.util.encoders.Hex;

public class TraceAllNodes implements TrieImpl.ScanAction {

    StringBuilder output = new StringBuilder();

    @Override
    public void doOnNode(byte[] hash, TrieImpl.Node node) {

        output.append(Hex.toHexString(hash)).append(" ==> ").append(node.toString()).append("\n");
    }

    @Override
    public void doOnValue(byte[] nodeHash, TrieImpl.Node node, byte[] key, byte[] value) {}

    public String getOutput() {
        return output.toString();
    }
}
