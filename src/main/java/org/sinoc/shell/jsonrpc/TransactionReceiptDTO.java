package org.sinoc.shell.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.sinoc.core.Block;
import org.sinoc.core.TransactionInfo;
import org.sinoc.core.TransactionReceipt;
import org.sinoc.vm.LogInfo;
import static org.sinoc.shell.jsonrpc.TypeConverter.toJsonHex;

public class TransactionReceiptDTO {

    public String transactionHash;          // hash of the transaction.
    public String transactionIndex;         // integer of the transactions index position in the block.
    public String blockHash;                // hash of the block where this transaction was in.
    public String blockNumber;              // block number where this transaction was in.
    public String from;                     // 20 Bytes - address of the sender.
    public String to;                       // 20 Bytes - address of the receiver. null when its a contract creation transaction.
    public String cumulativeGasUsed;        // The total amount of gas used when this transaction was executed in the block.
    public String gasUsed;                  // The amount of gas used by this specific transaction alone.
    public String contractAddress;          // The contract address created, if the transaction was a contract creation, otherwise  null .
    public JsonRpc.LogFilterElement[] logs;         // Array of log objects, which this transaction generated.
    public String logsBloom;                       // 256 Bytes - Bloom filter for light clients to quickly retrieve related logs.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String root;  // 32 bytes of post-transaction stateroot (pre Byzantium)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String status;  //  either 1 (success) or 0 (failure) (post Byzantium)

    public TransactionReceiptDTO(Block block, TransactionInfo txInfo){
        TransactionReceipt receipt = txInfo.getReceipt();

        transactionHash = toJsonHex(receipt.getTransaction().getHash());
        transactionIndex = toJsonHex(new Integer(txInfo.getIndex()).longValue());
        cumulativeGasUsed = toJsonHex(receipt.getCumulativeGas());
        gasUsed = toJsonHex(receipt.getGasUsed());
        contractAddress = toJsonHex(receipt.getTransaction().getContractAddress());
        from = toJsonHex(receipt.getTransaction().getSender());
        to = toJsonHex(receipt.getTransaction().getReceiveAddress());
        logs = new JsonRpc.LogFilterElement[receipt.getLogInfoList().size()];
        if (block != null) {
            blockNumber = toJsonHex(block.getNumber());
            blockHash = toJsonHex(txInfo.getBlockHash());
        } else {
            blockNumber = null;
            blockHash = null;
        }

        for (int i = 0; i < logs.length; i++) {
            LogInfo logInfo = receipt.getLogInfoList().get(i);
            logs[i] = new JsonRpc.LogFilterElement(logInfo, block, txInfo.getIndex(),
                    txInfo.getReceipt().getTransaction(), i);
        }
        logsBloom = toJsonHex(receipt.getBloomFilter().getData());

        if (receipt.hasTxStatus()) { // post Byzantium
            root = null;
            status = receipt.isTxStatusOK() ? "0x1" : "0x0";
        } else { // pre Byzantium
            root = toJsonHex(receipt.getPostTxState());
            status = null;
        }
    }

	public String getTransactionHash() {
		return transactionHash;
	}

	public void setTransactionHash(String transactionHash) {
		this.transactionHash = transactionHash;
	}

	public String getTransactionIndex() {
		return transactionIndex;
	}

	public void setTransactionIndex(String transactionIndex) {
		this.transactionIndex = transactionIndex;
	}

	public String getBlockHash() {
		return blockHash;
	}

	public void setBlockHash(String blockHash) {
		this.blockHash = blockHash;
	}

	public String getBlockNumber() {
		return blockNumber;
	}

	public void setBlockNumber(String blockNumber) {
		this.blockNumber = blockNumber;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCumulativeGasUsed() {
		return cumulativeGasUsed;
	}

	public void setCumulativeGasUsed(String cumulativeGasUsed) {
		this.cumulativeGasUsed = cumulativeGasUsed;
	}

	public String getGasUsed() {
		return gasUsed;
	}

	public void setGasUsed(String gasUsed) {
		this.gasUsed = gasUsed;
	}

	public String getContractAddress() {
		return contractAddress;
	}

	public void setContractAddress(String contractAddress) {
		this.contractAddress = contractAddress;
	}

	public JsonRpc.LogFilterElement[] getLogs() {
		return logs;
	}

	public void setLogs(JsonRpc.LogFilterElement[] logs) {
		this.logs = logs;
	}

	public String getLogsBloom() {
		return logsBloom;
	}

	public void setLogsBloom(String logsBloom) {
		this.logsBloom = logsBloom;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
