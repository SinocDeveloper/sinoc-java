package org.sinoc.net.dht;

import org.sinoc.crypto.HashUtil;
import org.spongycastle.util.BigIntegers;

import static org.sinoc.util.ByteUtil.toHexString;

import java.math.BigInteger;

public class Peer {
    byte[] id;
    String host = "127.0.0.1";
    int port = 0;

    public Peer(byte[] id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public Peer(byte[] ip) {
        this.id= ip;
    }

    public Peer() {
        HashUtil.randomPeerId();
    }

    public byte nextBit(String startPattern) {

        if (this.toBinaryString().startsWith(startPattern + "1"))
            return 1;
        else
            return 0;
    }

    public byte[] calcDistance(Peer toPeer) {

        BigInteger aPeer = new BigInteger(getId());
        BigInteger bPeer = new BigInteger(toPeer.getId());

        BigInteger distance = aPeer.xor(bPeer);
        return BigIntegers.asUnsignedByteArray(distance);
    }


    public byte[] getId() {
        return id;
    }

    public void setId(byte[] id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("Peer {\n id=%s, \n host=%s, \n port=%d\n}", toHexString(id), host, port);
    }

    public String toBinaryString() {

        BigInteger bi = new BigInteger(1, id);
        String out = String.format("%512s", bi.toString(2));
        out = out.replace(' ', '0');

        return out;
    }

}
