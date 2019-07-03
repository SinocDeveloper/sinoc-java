package org.sinoc.net.shh;

import static org.sinoc.net.shh.ShhMessageCodes.STATUS;

import org.sinoc.util.RLP;
import org.sinoc.util.RLPList;

public class ShhStatusMessage extends ShhMessage {

    private byte protocolVersion;

    public ShhStatusMessage(byte[] encoded) {
        super(encoded);
    }

    public ShhStatusMessage(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.parsed = true;
    }

    private void encode() {
        byte[] protocolVersion = RLP.encodeByte(this.protocolVersion);
        this.encoded = RLP.encodeList(protocolVersion);
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
        this.protocolVersion = paramsList.get(0).getRLPData()[0];
        parsed = true;
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public ShhMessageCodes getCommand() {
        return STATUS;
    }

    @Override
    public String toString() {
        if (!parsed) parse();
        return "[" + this.getCommand().name() +
            " protocolVersion=" + this.protocolVersion + "]";
    }

}
