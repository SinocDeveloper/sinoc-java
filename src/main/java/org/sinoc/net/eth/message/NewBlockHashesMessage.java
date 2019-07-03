package org.sinoc.net.eth.message;

import java.util.ArrayList;
import java.util.List;

import org.sinoc.core.BlockIdentifier;
import org.sinoc.util.RLP;
import org.sinoc.util.RLPList;

/**
 * Wrapper around an Ethereum NewBlockHashes message on the network<br>
 *
 * @see EthMessageCodes#NEW_BLOCK_HASHES
 *
 * @author Mikhail Kalinin
 * @since 05.09.2015
 */
public class NewBlockHashesMessage extends EthMessage {

    /**
     * List of identifiers holding hash and number of the blocks
     */
    private List<BlockIdentifier> blockIdentifiers;

    public NewBlockHashesMessage(byte[] payload) {
        super(payload);
    }

    public NewBlockHashesMessage(List<BlockIdentifier> blockIdentifiers) {
        this.blockIdentifiers = blockIdentifiers;
        parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        blockIdentifiers = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            RLPList rlpData = ((RLPList) paramsList.get(i));
            blockIdentifiers.add(new BlockIdentifier(rlpData));
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        for (BlockIdentifier identifier : blockIdentifiers)
            encodedElements.add(identifier.getEncoded());
        byte[][] encodedElementArray = encodedElements.toArray(new byte[encodedElements.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
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

    public List<BlockIdentifier> getBlockIdentifiers() {
        parse();
        return blockIdentifiers;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.NEW_BLOCK_HASHES;
    }

    @Override
    public String toString() {
        parse();

        return "[" + this.getCommand().name() + "] (" + blockIdentifiers.size() + ")";
    }

}
