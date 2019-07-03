package org.sinoc.shell.model.dto;

public class MinerDTO {

    private final String address;

    private final Integer count;

	public MinerDTO(String address, Integer count) {
		super();
		this.address = address;
		this.count = count;
	}

	public String getAddress() {
		return address;
	}

	public Integer getCount() {
		return count;
	}
    
}
