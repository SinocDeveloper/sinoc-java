package org.sinoc.net.shh;

import static org.sinoc.net.shh.ShhMessageCodes.FILTER;
import static org.sinoc.util.ByteUtil.toHexString;

import org.sinoc.db.ByteArrayWrapper;
import org.sinoc.util.RLP;
import org.sinoc.util.RLPList;

public class ShhFilterMessage extends ShhMessage {

    private byte[] bloomFilter;

    private ShhFilterMessage() {
    }

    public ShhFilterMessage(byte[] encoded) {
        super(encoded);
        parse();
    }

    static ShhFilterMessage createFromFilter(byte[] bloomFilter) {
        ShhFilterMessage ret = new ShhFilterMessage();
        ret.bloomFilter = bloomFilter;
        ret.parsed = true;
        return ret;
    }

    private void encode() {
        byte[] protocolVersion = RLP.encodeElement(this.bloomFilter);
        this.encoded = RLP.encodeList(protocolVersion);
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
        this.bloomFilter = paramsList.get(0).getRLPData();
        parsed = true;
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public byte[] getBloomFilter() {
        return bloomFilter;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public ShhMessageCodes getCommand() {
        return FILTER;
    }

    @Override
    public String toString() {
        if (!parsed) parse();
        return "[" + this.getCommand().name() +
            " hash=" + toHexString(bloomFilter) + "]";
    }

}
