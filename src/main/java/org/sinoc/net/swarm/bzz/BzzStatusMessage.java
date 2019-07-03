package org.sinoc.net.swarm.bzz;

import static org.sinoc.net.swarm.Util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sinoc.net.client.Capability;
import org.sinoc.net.swarm.Util;
import org.sinoc.util.RLP;
import org.sinoc.util.RLPElement;
import org.sinoc.util.RLPList;

/**
 * BZZ handshake message
 */
public class BzzStatusMessage extends BzzMessage {

    private long version;
    private String id;
    private PeerAddress addr;
    private long networkId;
    private List<Capability> capabilities;

    public BzzStatusMessage(byte[] encoded) {
        super(encoded);
    }

    public BzzStatusMessage(int version, String id, PeerAddress addr, long networkId, List<Capability> capabilities) {
        this.version = version;
        this.id = id;
        this.addr = addr;
        this.networkId = networkId;
        this.capabilities = capabilities;
        parsed = true;
    }

    @Override
    protected void decode() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        version = rlpDecodeLong(paramsList.get(0));
        id = rlpDecodeString(paramsList.get(1));
        addr = PeerAddress.parse((RLPList) paramsList.get(2));
        networkId = rlpDecodeInt(paramsList.get(3));

        capabilities = new ArrayList<>();
        RLPList caps = (RLPList) paramsList.get(4);
        for (RLPElement c : caps) {
            RLPList e = (RLPList) c;
            capabilities.add(new Capability(rlpDecodeString(e.get(0)), rlpDecodeByte(e.get(1))));
        }

        parsed = true;
    }

    private void encode() {
        byte[][] capabilities = new byte[this.capabilities.size()][];
        for (int i = 0; i < this.capabilities.size(); i++) {
            Capability capability = this.capabilities.get(i);
            capabilities[i] = rlpEncodeList(capability.getName(),capability.getVersion());
        }
        this.encoded = rlpEncodeList(version, id, addr.encodeRlp(), networkId, rlpEncodeList(capabilities));
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    /**
     * BZZ protocol version
     */
    public long getVersion() {
        return version;
    }

    /**
     * Gets the remote peer address
     */
    public PeerAddress getAddr() {
        return addr;
    }

    public long getNetworkId() {
        return networkId;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public BzzMessageCodes getCommand() {
        return BzzMessageCodes.STATUS;
    }


    @Override
    public String toString() {
        return "BzzStatusMessage{" +
                "version=" + version +
                ", id='" + id + '\'' +
                ", addr=" + addr +
                ", networkId=" + networkId +
                ", capabilities=" + capabilities +
                '}';
    }
}
