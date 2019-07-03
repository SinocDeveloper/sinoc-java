package org.sinoc.net.rlpx;

import org.sinoc.crypto.ECKey;
import org.sinoc.util.ByteUtil;
import org.sinoc.util.RLP;
import org.sinoc.util.RLPItem;
import org.sinoc.util.RLPList;

public class PongMessage extends Message {

    byte[] token; // token is the MDC of the ping
    long expires;

    public static PongMessage create(byte[] token, Node toNode, ECKey privKey) {

        long expiration = 90 * 60 + System.currentTimeMillis() / 1000;

        byte[] rlpToList = toNode.getBriefRLP();

        /* RLP Encode data */
        byte[] rlpToken = RLP.encodeElement(token);
        byte[] tmpExp = ByteUtil.longToBytes(expiration);
        byte[] rlpExp = RLP.encodeElement(ByteUtil.stripLeadingZeroes(tmpExp));

        byte[] type = new byte[]{2};
        byte[] data = RLP.encodeList(rlpToList, rlpToken, rlpExp);

        PongMessage pong = new PongMessage();
        pong.encode(type, data, privKey);

        pong.token = token;
        pong.expires = expiration;

        return pong;
    }

    public static PongMessage create(byte[] token, ECKey privKey) {
        return create(token, privKey, 3 + System.currentTimeMillis() / 1000);
    }

    static PongMessage create(byte[] token, ECKey privKey, long expiration) {

        /* RLP Encode data */
        byte[] rlpToken = RLP.encodeElement(token);
        byte[] rlpExp = RLP.encodeElement(ByteUtil.longToBytes(expiration));

        byte[] type = new byte[]{2};
        byte[] data = RLP.encodeList(rlpToken, rlpExp);

        PongMessage pong = new PongMessage();
        pong.encode(type, data, privKey);

        pong.token = token;
        pong.expires = expiration;

        return pong;
    }


    @Override
    public void parse(byte[] data) {
        RLPList list = (RLPList) RLP.decode2OneItem(data, 0);

        this.token = list.get(0).getRLPData();
        RLPItem expires = (RLPItem) list.get(1);
        this.expires = ByteUtil.byteArrayToLong(expires.getRLPData());
    }


    public byte[] getToken() {
        return token;
    }

    public long getExpires() {
        return expires;
    }

    @Override
    public String toString() {
        long currTime = System.currentTimeMillis() / 1000;

        String out = String.format("[PongMessage] \n token: %s \n expires in %d seconds \n %s\n",
                ByteUtil.toHexString(token), (expires - currTime), super.toString());

        return out;
    }
}
