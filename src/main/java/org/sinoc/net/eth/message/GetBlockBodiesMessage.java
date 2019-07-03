package org.sinoc.net.eth.message;

import static org.sinoc.util.ByteUtil.toHexString;

import java.util.ArrayList;
import java.util.List;

import org.sinoc.util.RLP;
import org.sinoc.util.RLPList;
import org.sinoc.util.Utils;

/**
 * Wrapper around an Ethereum GetBlockBodies message on the network
 *
 * @see EthMessageCodes#GET_BLOCK_BODIES
 *
 */
public class GetBlockBodiesMessage extends EthMessage {

    /**
     * List of block hashes for which to retrieve the block bodies
     */
    private List<byte[]> blockHashes;

    public GetBlockBodiesMessage(byte[] encoded) {
        super(encoded);
    }

    public GetBlockBodiesMessage(List<byte[]> blockHashes) {
        this.blockHashes = blockHashes;
        parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        blockHashes = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            blockHashes.add(paramsList.get(i).getRLPData());
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        for (byte[] hash : blockHashes)
            encodedElements.add(RLP.encodeElement(hash));
        byte[][] encodedElementArray = encodedElements.toArray(new byte[encodedElements.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }


    @Override
    public Class<BlockBodiesMessage> getAnswerMessage() {
        return BlockBodiesMessage.class;
    }

    public List<byte[]> getBlockHashes() {
        parse();
        return blockHashes;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.GET_BLOCK_BODIES;
    }

    public String toString() {
        parse();

        StringBuilder payload = new StringBuilder();

        payload.append("count( ").append(blockHashes.size()).append(" ) ");

        if (logger.isDebugEnabled()) {
            for (byte[] hash : blockHashes) {
                payload.append(toHexString(hash).substring(0, 6)).append(" | ");
            }
            if (!blockHashes.isEmpty()) {
                payload.delete(payload.length() - 3, payload.length());
            }
        } else {
            payload.append(Utils.getHashListShort(blockHashes));
        }

        return "[" + getCommand().name() + " " + payload + "]";
    }
}
