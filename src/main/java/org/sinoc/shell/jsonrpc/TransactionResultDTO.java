package org.sinoc.shell.jsonrpc;

import org.sinoc.core.Block;
import org.sinoc.core.Transaction;

import static org.sinoc.shell.jsonrpc.TypeConverter.toJsonHex;
import static org.sinoc.shell.jsonrpc.TypeConverter.toJsonHexNumber;

public class TransactionResultDTO {

    public String hash;
    public String nonce;
    public String blockHash;
    public String blockNumber;
    public String transactionIndex;

    public String from;
    public String to;
    public String gas;
    public String gasPrice;
    public String value;
    public String input;

    public TransactionResultDTO(Block b, int index, Transaction tx) {
        hash =  toJsonHex(tx.getHash());
        nonce = toJsonHex(tx.getNonce());
        blockHash = toJsonHex(b.getHash());
        blockNumber = toJsonHex(b.getNumber());
        transactionIndex = toJsonHex(index);
        from = toJsonHex(tx.getSender());
        to = toJsonHex(tx.getReceiveAddress());
        gas = toJsonHex(tx.getGasLimit());
        gasPrice = toJsonHex(tx.getGasPrice());
        value = toJsonHexNumber(tx.getValue());
        input = toJsonHex(tx.getData());
    }
    
    

    @Override
    public String toString() {
        return "TransactionResultDTO{" +
                "hash='" + hash + '\'' +
                ", nonce='" + nonce + '\'' +
                ", blockHash='" + blockHash + '\'' +
                ", blockNumber='" + blockNumber + '\'' +
                ", transactionIndex='" + transactionIndex + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", gas='" + gas + '\'' +
                ", gasPrice='" + gasPrice + '\'' +
                ", value='" + value + '\'' +
                ", input='" + input + '\'' +
                '}';
    }

	public String getHash() {
		return hash;
	}



	public void setHash(String hash) {
		this.hash = hash;
	}



	public String getNonce() {
		return nonce;
	}



	public void setNonce(String nonce) {
		this.nonce = nonce;
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



	public String getTransactionIndex() {
		return transactionIndex;
	}



	public void setTransactionIndex(String transactionIndex) {
		this.transactionIndex = transactionIndex;
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



	public String getGas() {
		return gas;
	}



	public void setGas(String gas) {
		this.gas = gas;
	}



	public String getGasPrice() {
		return gasPrice;
	}



	public void setGasPrice(String gasPrice) {
		this.gasPrice = gasPrice;
	}



	public String getValue() {
		return value;
	}



	public void setValue(String value) {
		this.value = value;
	}



	public String getInput() {
		return input;
	}



	public void setInput(String input) {
		this.input = input;
	}
}