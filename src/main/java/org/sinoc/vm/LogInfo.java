package org.sinoc.vm;

import static org.sinoc.datasource.MemSizeEstimator.ByteArrayEstimator;
import static org.sinoc.util.ByteUtil.toHexString;

import java.util.ArrayList;
import java.util.List;

import org.sinoc.core.Bloom;
import org.sinoc.crypto.HashUtil;
import org.sinoc.datasource.MemSizeEstimator;
import org.sinoc.util.RLP;
import org.sinoc.util.RLPElement;
import org.sinoc.util.RLPItem;
import org.sinoc.util.RLPList;

public class LogInfo {

    byte[] address = new byte[]{};
    List<DataWord> topics = new ArrayList<>();
    byte[] data = new byte[]{};

    public LogInfo(byte[] rlp) {

        RLPList params = RLP.decode2(rlp);
        RLPList logInfo = (RLPList) params.get(0);

        RLPItem address = (RLPItem) logInfo.get(0);
        RLPList topics = (RLPList) logInfo.get(1);
        RLPItem data = (RLPItem) logInfo.get(2);

        this.address = address.getRLPData() != null ? address.getRLPData() : new byte[]{};
        this.data = data.getRLPData() != null ? data.getRLPData() : new byte[]{};

        for (RLPElement topic1 : topics) {
            byte[] topic = topic1.getRLPData();
            this.topics.add(new DataWord(topic));
        }
    }

    public LogInfo(byte[] address, List<DataWord> topics, byte[] data) {
        this.address = (address != null) ? address : new byte[]{};
        this.topics = (topics != null) ? topics : new ArrayList<DataWord>();
        this.data = (data != null) ? data : new byte[]{};
    }

    public byte[] getAddress() {
        return address;
    }

    public List<DataWord> getTopics() {
        return topics;
    }

    public byte[] getData() {
        return data;
    }

    /*  [address, [topic, topic ...] data] */
    public byte[] getEncoded() {

        byte[] addressEncoded = RLP.encodeElement(this.address);

        byte[][] topicsEncoded = null;
        if (topics != null) {
            topicsEncoded = new byte[topics.size()][];
            int i = 0;
            for (DataWord topic : topics) {
                byte[] topicData = topic.getData();
                topicsEncoded[i] = RLP.encodeElement(topicData);
                ++i;
            }
        }

        byte[] dataEncoded = RLP.encodeElement(data);
        return RLP.encodeList(addressEncoded, RLP.encodeList(topicsEncoded), dataEncoded);
    }

    public Bloom getBloom() {
        Bloom ret = Bloom.create(HashUtil.sha3(address));
        for (DataWord topic : topics) {
            byte[] topicData = topic.getData();
            ret.or(Bloom.create(HashUtil.sha3(topicData)));
        }
        return ret;
    }

    @Override
    public String toString() {

        StringBuilder topicsStr = new StringBuilder();
        topicsStr.append("[");

        for (DataWord topic : topics) {
            String topicStr = toHexString(topic.getData());
            topicsStr.append(topicStr).append(" ");
        }
        topicsStr.append("]");


        return "LogInfo{" +
                "address=" + toHexString(address) +
                ", topics=" + topicsStr +
                ", data=" + toHexString(data) +
                '}';
    }

    public static final MemSizeEstimator<LogInfo> MemEstimator = log ->
            ByteArrayEstimator.estimateSize(log.address) +
            ByteArrayEstimator.estimateSize(log.data) +
            log.topics.size() * DataWord.MEM_SIZE + 16;
}
