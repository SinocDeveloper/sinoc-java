package org.sinoc.net.eth.message;

import static org.sinoc.util.ByteUtil.toHexString;

import java.util.ArrayList;
import java.util.List;

import org.sinoc.util.RLP;
import org.sinoc.util.RLPElement;
import org.sinoc.util.RLPList;

/**
 * Wrapper around an Ethereum BlockBodies message on the network
 *
 * @see EthMessageCodes#BLOCK_BODIES
 *
 */
public class BlockBodiesMessage extends EthMessage {

    private List<byte[]> blockBodies;

    public BlockBodiesMessage(byte[] encoded) {
        super(encoded);
    }

    public BlockBodiesMessage(List<byte[]> blockBodies) {
        this.blockBodies = blockBodies;
        parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = RLP.unwrapList(encoded);
        this.encoded = null;

        blockBodies = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            RLPElement rlpData = paramsList.get(i);
            blockBodies.add(rlpData.getRLPData());
        }
        parsed = true;
    }

    private void encode() {

        byte[][] encodedElementArray = blockBodies
                .toArray(new byte[blockBodies.size()][]);

        this.encoded = RLP.encodeList(encodedElementArray);
    }


    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public List<byte[]> getBlockBodies() {
        parse();
        return blockBodies;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.BLOCK_BODIES;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        parse();

        StringBuilder payload = new StringBuilder();

        payload.append("count( ").append(blockBodies.size()).append(" )");

        if (logger.isTraceEnabled()) {
            payload.append(" ");
            for (byte[] body : blockBodies) {
                payload.append(toHexString(body)).append(" | ");
            }
            if (!blockBodies.isEmpty()) {
                payload.delete(payload.length() - 3, payload.length());
            }
        }

        return "[" + getCommand().name() + " " + payload + "]";
    }
}
