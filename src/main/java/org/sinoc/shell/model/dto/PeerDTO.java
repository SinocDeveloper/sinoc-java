package org.sinoc.shell.model.dto;

public class PeerDTO {

    private final String nodeId;

    private final String ip;

    // 3 letter code, used for map in UI
    private final String country3Code;

    // 2 letter code, used for flags in UI
    private final String country2Code;

    // seconds???
    private final Long lastPing;

    // ms
    private final Double pingLatency;

    private final Integer reputation;

    private final Boolean isActive;

    private final String details;

	public PeerDTO(String nodeId, String ip, String country3Code, String country2Code, Long lastPing,
			Double pingLatency, Integer reputation, Boolean isActive, String details) {
		super();
		this.nodeId = nodeId;
		this.ip = ip;
		this.country3Code = country3Code;
		this.country2Code = country2Code;
		this.lastPing = lastPing;
		this.pingLatency = pingLatency;
		this.reputation = reputation;
		this.isActive = isActive;
		this.details = details;
	}

	public String getNodeId() {
		return nodeId;
	}

	public String getIp() {
		return ip;
	}

	public String getCountry3Code() {
		return country3Code;
	}

	public String getCountry2Code() {
		return country2Code;
	}

	public Long getLastPing() {
		return lastPing;
	}

	public Double getPingLatency() {
		return pingLatency;
	}

	public Integer getReputation() {
		return reputation;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public String getDetails() {
		return details;
	}
}
