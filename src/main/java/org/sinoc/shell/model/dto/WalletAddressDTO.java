package org.sinoc.shell.model.dto;


import java.math.BigInteger;

public class WalletAddressDTO {

    private final String name;

    private final String publicAddress;

    private final BigInteger amount;

    private final BigInteger pendingAmount;

    private final boolean hasKeystoreKey;

	public WalletAddressDTO(String name, String publicAddress, BigInteger amount, BigInteger pendingAmount,
			boolean hasKeystoreKey) {
		super();
		this.name = name;
		this.publicAddress = publicAddress;
		this.amount = amount;
		this.pendingAmount = pendingAmount;
		this.hasKeystoreKey = hasKeystoreKey;
	}

	public String getName() {
		return name;
	}

	public String getPublicAddress() {
		return publicAddress;
	}

	public BigInteger getAmount() {
		return amount;
	}

	public BigInteger getPendingAmount() {
		return pendingAmount;
	}

	public boolean isHasKeystoreKey() {
		return hasKeystoreKey;
	}
}
