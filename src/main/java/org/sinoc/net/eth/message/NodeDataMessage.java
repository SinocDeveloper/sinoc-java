package org.sinoc.net.eth.message;

import java.util.ArrayList;
import java.util.List;

import org.sinoc.util.RLP;
import org.sinoc.util.RLPList;
import org.sinoc.util.Value;

/**
 * Wrapper around an Ethereum NodeData message on the network
 *
 * @see EthMessageCodes#NODE_DATA
 */
public class NodeDataMessage extends EthMessage {

    private List<Value> dataList;

    public NodeDataMessage(byte[] encoded) {
        super(encoded);
        parse();
    }

    public NodeDataMessage(List<Value> dataList) {
        this.dataList = dataList;
        parsed = true;
    }

    private void parse() {
        RLPList paramsList = RLP.unwrapList(encoded);

        dataList = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            // Need it AS IS
            dataList.add(new Value(paramsList.get(i).getRLPData()));
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> dataListRLP = new ArrayList<>();
        for (Value value: dataList) {
            if (value == null) continue; // Bad sign
            dataListRLP.add(RLP.encodeElement(value.asBytes()));
        }
        byte[][] encodedElementArray = dataListRLP.toArray(new byte[dataListRLP.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
    }


    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public List<Value> getDataList() {
        return dataList;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.NODE_DATA;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {

        StringBuilder payload = new StringBuilder();

        payload.append("count( ").append(dataList.size()).append(" )");

        if (logger.isTraceEnabled()) {
            payload.append(" ");
            for (Value value : dataList) {
                payload.append(value).append(" | ");
            }
            if (!dataList.isEmpty()) {
                payload.delete(payload.length() - 3, payload.length());
            }
        }

        return "[" + getCommand().name() + " " + payload + "]";
    }
}
