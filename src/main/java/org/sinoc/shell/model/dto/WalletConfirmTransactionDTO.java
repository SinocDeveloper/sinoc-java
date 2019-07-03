package org.sinoc.shell.model.dto;


import java.math.BigInteger;

public class WalletConfirmTransactionDTO {

    private final String hash;

    private final BigInteger amount;

    private final boolean sending;

	public WalletConfirmTransactionDTO(String hash, BigInteger amount, boolean sending) {
		super();
		this.hash = hash;
		this.amount = amount;
		this.sending = sending;
	}

	public String getHash() {
		return hash;
	}

	public BigInteger getAmount() {
		return amount;
	}

	public boolean isSending() {
		return sending;
	}
}
