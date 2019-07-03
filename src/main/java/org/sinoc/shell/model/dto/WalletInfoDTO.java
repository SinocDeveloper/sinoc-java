package org.sinoc.shell.model.dto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class WalletInfoDTO {

    private final BigInteger totalAmount;

    private final List<WalletAddressDTO> addresses = new ArrayList<>();

	public WalletInfoDTO(BigInteger totalAmount) {
		super();
		this.totalAmount = totalAmount;
	}

	public BigInteger getTotalAmount() {
		return totalAmount;
	}

	public List<WalletAddressDTO> getAddresses() {
		return addresses;
	}
}
