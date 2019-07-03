package org.sinoc.core;

import org.sinoc.crypto.HashUtil;
import org.sinoc.datasource.MemSizeEstimator;
import org.sinoc.trie.Trie;
import org.sinoc.trie.TrieImpl;
import org.sinoc.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

import static org.sinoc.crypto.HashUtil.sha3;
import static org.sinoc.datasource.MemSizeEstimator.ByteArrayEstimator;
import static org.sinoc.util.ByteUtil.toHexString;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The block in Ethereum is the collection of relevant pieces of information
 * (known as the blockheader), H, together with information corresponding to
 * the comprised transactions, R, and a set of other blockheaders U that are known
 * to have a parent equal to the present block’s parent’s parent
 * (such blocks are known as uncles).
 *
 */
public class Block {

    private static final Logger logger = LoggerFactory.getLogger("blockchain");

    private BlockHeader header;

    /* Transactions */
    private List<Transaction> transactionsList = new CopyOnWriteArrayList<>();

    /* Uncles */
    private List<BlockHeader> uncleList = new CopyOnWriteArrayList<>();

    /* Private */

    private byte[] rlpEncoded;
    private boolean parsed = false;

    /* Constructors */

    private Block() {
    }

    public Block(byte[] rawData) {
        logger.debug("new from [" + toHexString(rawData) + "]");
        this.rlpEncoded = rawData;
    }

    public Block(BlockHeader header, List<Transaction> transactionsList, List<BlockHeader> uncleList) {

        this(header.getParentHash(),
                header.getUnclesHash(),
                header.getCoinbase(),
                header.getLogsBloom(),
                header.getDifficulty(),
                header.getNumber(),
                header.getGasLimit(),
                header.getGasUsed(),
                header.getTimestamp(),
                header.getExtraData(),
                header.getGenSign(),
                header.getBaseTarget(),
                header.getNonce(),
                header.getDeadLine(),
                header.getReceiptsRoot(),
                header.getTxTrieRoot(),
                header.getStateRoot(),
                transactionsList,
                uncleList);
    }

    public Block(byte[] parentHash, byte[] unclesHash, byte[] coinbase, byte[] logsBloom,
                 byte[] difficulty, long number, byte[] gasLimit,
                 long gasUsed, long timestamp, byte[] extraData,
                 byte[] genSign,long baseTarget, long nonce,byte[] deadLine, byte[] receiptsRoot,
                 byte[] transactionsRoot, byte[] stateRoot,
                 List<Transaction> transactionsList, List<BlockHeader> uncleList) {

        this(parentHash, unclesHash, coinbase, logsBloom, difficulty, number, gasLimit,
                gasUsed, timestamp, extraData, genSign, baseTarget, nonce, deadLine, transactionsList, uncleList);

        this.header.setTransactionsRoot(BlockchainImpl.calcTxTrie(transactionsList));
        if (!Hex.toHexString(transactionsRoot).
                equals(Hex.toHexString(this.header.getTxTrieRoot())))
            logger.debug("Transaction root miss-calculate, block: {}", getNumber());

        this.header.setStateRoot(stateRoot);
        this.header.setReceiptsRoot(receiptsRoot);
    }


    public Block(byte[] parentHash, byte[] unclesHash, byte[] coinbase, byte[] logsBloom,
                 byte[] difficulty, long number, byte[] gasLimit,
                 long gasUsed, long timestamp,
                 byte[] extraData, byte[] genSign, long baseTarget, long nonce,byte[] deadLine,
                 List<Transaction> transactionsList, List<BlockHeader> uncleList) {
        this.header = new BlockHeader(parentHash, unclesHash, coinbase, logsBloom,
                difficulty, number, gasLimit, gasUsed,
                timestamp, extraData, genSign, baseTarget, nonce,deadLine);

        this.transactionsList = transactionsList;
        if (this.transactionsList == null) {
            this.transactionsList = new CopyOnWriteArrayList<>();
        }

        this.uncleList = uncleList;
        if (this.uncleList == null) {
            this.uncleList = new CopyOnWriteArrayList<>();
        }

        this.parsed = true;
    }

    private synchronized void parseRLP() {
        if (parsed) return;

        RLPList params = RLP.decode2(rlpEncoded);
        RLPList block = (RLPList) params.get(0);

        // Parse Header
        RLPList header = (RLPList) block.get(0);
        this.header = new BlockHeader(header);

        // Parse Transactions
        RLPList txTransactions = (RLPList) block.get(1);
        this.parseTxs(this.header.getTxTrieRoot(), txTransactions, false);

        // Parse Uncles
        RLPList uncleBlocks = (RLPList) block.get(2);
        for (RLPElement rawUncle : uncleBlocks) {

            RLPList uncleHeader = (RLPList) rawUncle;
            BlockHeader blockData = new BlockHeader(uncleHeader);
            this.uncleList.add(blockData);
        }
        this.parsed = true;
    }

    public BlockHeader getHeader() {
        parseRLP();
        return this.header;
    }

    public byte[] getHash() {
        parseRLP();
        return this.header.getHash();
    }

    public byte[] getParentHash() {
        parseRLP();
        return this.header.getParentHash();
    }

    public byte[] getUnclesHash() {
        parseRLP();
        return this.header.getUnclesHash();
    }

    public byte[] getCoinbase() {
        parseRLP();
        return this.header.getCoinbase();
    }

    public byte[] getStateRoot() {
        parseRLP();
        return this.header.getStateRoot();
    }

    public void setStateRoot(byte[] stateRoot) {
        parseRLP();
        this.header.setStateRoot(stateRoot);
    }

    public byte[] getTxTrieRoot() {
        parseRLP();
        return this.header.getTxTrieRoot();
    }

    public byte[] getReceiptsRoot() {
        parseRLP();
        return this.header.getReceiptsRoot();
    }


    public byte[] getLogBloom() {
        parseRLP();
        return this.header.getLogsBloom();
    }

    public byte[] getDifficulty() {
        parseRLP();
        return this.header.getDifficulty();
    }

    public BigInteger getDifficultyBI() {
        parseRLP();
        return this.header.getDifficultyBI();
    }
    
    public BigInteger getDeadLineBI(){
    	parseRLP();
    	return this.header.getDeadLineBI();
    }

    public BigInteger getCumulativeDifficulty() {
        parseRLP();
        BigInteger calcDifficulty = new BigInteger(1, this.header.getDifficulty());
        for (BlockHeader uncle : uncleList) {
            calcDifficulty = calcDifficulty.add(new BigInteger(1, uncle.getDifficulty()));
        }
        return calcDifficulty;
    }

    public long getTimestamp() {
        parseRLP();
        return this.header.getTimestamp();
    }

    public long getNumber() {
        parseRLP();
        return this.header.getNumber();
    }

    public byte[] getGasLimit() {
        parseRLP();
        return this.header.getGasLimit();
    }

    public long getGasUsed() {
        parseRLP();
        return this.header.getGasUsed();
    }


    public byte[] getExtraData() {
        parseRLP();
        return this.header.getExtraData();
    }

    public byte[] getGenSign() {
        parseRLP();
        return this.header.getGenSign();
    }
    
    public long getBaseTarget() {
        parseRLP();
        return this.header.getBaseTarget();
    }


    public long getNonce() {
        parseRLP();
        return this.header.getNonce();
    }
    
    public void setBaseTarget(long baseTarget) {
        this.header.setBaseTarget(baseTarget);
        rlpEncoded = null;
    }
    
    public void setGenSign(byte[] genSign) {
        this.header.setGenSign(genSign);
        rlpEncoded = null;
    }

    public void setNonce(long nonce) {
        this.header.setNonce(nonce);
        rlpEncoded = null;
    }
    
    public void setDeadLine(byte[] deadLine) {
        this.header.setDeadLine(deadLine);
        rlpEncoded = null;
    }
    
    public void setExtraData(byte[] data) {
        this.header.setExtraData(data);
        rlpEncoded = null;
    }

    public List<Transaction> getTransactionsList() {
        parseRLP();
        return transactionsList;
    }

    public List<BlockHeader> getUncleList() {
        parseRLP();
        return uncleList;
    }

    private StringBuffer toStringBuff = new StringBuffer();

    @Override
    public String toString() {
        parseRLP();

        toStringBuff.setLength(0);
        toStringBuff.append(toHexString(this.getEncoded())).append("\n");
        toStringBuff.append("BlockData [ ");
        toStringBuff.append(header.toString());

        if (!getUncleList().isEmpty()) {
            toStringBuff.append("Uncles [\n");
            for (BlockHeader uncle : getUncleList()) {
                toStringBuff.append(uncle.toString());
                toStringBuff.append("\n");
            }
            toStringBuff.append("]\n");
        } else {
            toStringBuff.append("Uncles []\n");
        }
        if (!getTransactionsList().isEmpty()) {
            toStringBuff.append("Txs [\n");
            for (Transaction tx : getTransactionsList()) {
                toStringBuff.append(tx);
                toStringBuff.append("\n");
            }
            toStringBuff.append("]\n");
        } else {
            toStringBuff.append("Txs []\n");
        }
        toStringBuff.append("]");

        return toStringBuff.toString();
    }

    public String toFlatString() {
        parseRLP();

        toStringBuff.setLength(0);
        toStringBuff.append("BlockData [");
        toStringBuff.append(header.toFlatString());

        for (Transaction tx : getTransactionsList()) {
            toStringBuff.append("\n");
            toStringBuff.append(tx.toString());
        }

        toStringBuff.append("]");
        return toStringBuff.toString();
    }

    private byte[] parseTxs(RLPList txTransactions, boolean validate) {

        Trie<byte[]> txsState = new TrieImpl();
        for (int i = 0; i < txTransactions.size(); i++) {
            RLPElement transactionRaw = txTransactions.get(i);
            Transaction tx = new Transaction(transactionRaw.getRLPData());
            if (validate) tx.verify();
            this.transactionsList.add(tx);
            txsState.put(RLP.encodeInt(i), transactionRaw.getRLPData());
        }
        return txsState.getRootHash();
    }


    private boolean parseTxs(byte[] expectedRoot, RLPList txTransactions, boolean validate) {

        byte[] rootHash = parseTxs(txTransactions, validate);
        String calculatedRoot = Hex.toHexString(rootHash);
        if (!calculatedRoot.equals(Hex.toHexString(expectedRoot))) {
            logger.debug("Transactions trie root validation failed for block #{}", this.header.getNumber());
            return false;
        }

        return true;
    }

    /**
     * check if param block is son of this block
     *
     * @param block - possible a son of this
     * @return - true if this block is parent of param block
     */
    public boolean isParentOf(Block block) {
        return Arrays.areEqual(this.getHash(), block.getParentHash());
    }

    public boolean isGenesis() {
        return this.header.isGenesis();
    }

    public boolean isEqual(Block block) {
        return Arrays.areEqual(this.getHash(), block.getHash());
    }

    private byte[] getTransactionsEncoded() {

        byte[][] transactionsEncoded = new byte[transactionsList.size()][];
        int i = 0;
        for (Transaction tx : transactionsList) {
            transactionsEncoded[i] = tx.getEncoded();
            ++i;
        }
        return RLP.encodeList(transactionsEncoded);
    }

    private byte[] getUnclesEncoded() {

        byte[][] unclesEncoded = new byte[uncleList.size()][];
        int i = 0;
        for (BlockHeader uncle : uncleList) {
            unclesEncoded[i] = uncle.getEncoded();
            ++i;
        }
        return RLP.encodeList(unclesEncoded);
    }

    public void addUncle(BlockHeader uncle) {
        uncleList.add(uncle);
        this.getHeader().setUnclesHash(sha3(getUnclesEncoded()));
        rlpEncoded = null;
    }

    public byte[] getEncoded() {
        if (rlpEncoded == null) {
            byte[] header = this.header.getEncoded();

            List<byte[]> block = getBodyElements();
            block.add(0, header);
            byte[][] elements = block.toArray(new byte[block.size()][]);

            this.rlpEncoded = RLP.encodeList(elements);
        }
        return rlpEncoded;
    }

    public byte[] getEncodedWithoutNonce() {
        parseRLP();
        return this.header.getEncodedWithoutNonce();
    }

    public byte[] getEncodedBody() {
        List<byte[]> body = getBodyElements();
        byte[][] elements = body.toArray(new byte[body.size()][]);
        return RLP.encodeList(elements);
    }

    private List<byte[]> getBodyElements() {
        parseRLP();

        byte[] transactions = getTransactionsEncoded();
        byte[] uncles = getUnclesEncoded();

        List<byte[]> body = new ArrayList<>();
        body.add(transactions);
        body.add(uncles);

        return body;
    }

    public String getShortHash() {
        parseRLP();
        return Hex.toHexString(getHash()).substring(0, 6);
    }

    public String getShortDescr() {
        return "#" + getNumber() + " (" + Hex.toHexString(getHash()).substring(0,6) + " <~ "
                + Hex.toHexString(getParentHash()).substring(0,6) + ") Txs:" + getTransactionsList().size() +
                ", Unc: " + getUncleList().size();
    }

    public static class Builder {

        private BlockHeader header;
        private byte[] body;

        public Builder withHeader(BlockHeader header) {
            this.header = header;
            return this;
        }

        public Builder withBody(byte[] body) {
            this.body = body;
            return this;
        }

        public Block create() {
            if (header == null || body == null) {
                return null;
            }

            Block block = new Block();
            block.header = header;
            block.parsed = true;

            RLPList items = (RLPList) RLP.decode2(body).get(0);

            RLPList transactions = (RLPList) items.get(0);
            RLPList uncles = (RLPList) items.get(1);

            if (!block.parseTxs(header.getTxTrieRoot(), transactions, false)) {
                return null;
            }

            byte[] unclesHash = HashUtil.sha3(uncles.getRLPData());
            if (!java.util.Arrays.equals(header.getUnclesHash(), unclesHash)) {
                return null;
            }

            for (RLPElement rawUncle : uncles) {

                RLPList uncleHeader = (RLPList) rawUncle;
                BlockHeader blockData = new BlockHeader(uncleHeader);
                block.uncleList.add(blockData);
            }

            return block;
        }
    }

    public static final MemSizeEstimator<Block> MemEstimator = block -> {
        if (block == null) return 0;
        long txSize = block.transactionsList.stream().mapToLong(Transaction.MemEstimator::estimateSize).sum() + 16;
        return BlockHeader.MAX_HEADER_SIZE +
                block.uncleList.size() * BlockHeader.MAX_HEADER_SIZE + 16 +
                txSize +
                ByteArrayEstimator.estimateSize(block.rlpEncoded) +
                1 + // parsed flag
                16; // Object header + ref
    };
}
