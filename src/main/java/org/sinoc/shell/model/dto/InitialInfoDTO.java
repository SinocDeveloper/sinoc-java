package org.sinoc.shell.model.dto;

public class InitialInfoDTO {

    private final String ethereumJVersion;

    private final String ethereumJBuildInfo;

    private final String appVersion;

    private final String networkName;

    /**
     * Link to Ether.Camp block explore site for contracts import
     */
    private final String explorerUrl;

    private final String genesisHash;

    private final Long serverStartTime;

    private final String nodeId;

    private final Integer rpcPort;

    private final boolean privateNetwork;

    private final String portCheckerUrl;

    private final String publicIp;

    private final boolean featureContracts;

    private final boolean featureRpc;

	public InitialInfoDTO(String ethereumJVersion, String ethereumJBuildInfo, String appVersion, String networkName,
			String explorerUrl, String genesisHash, Long serverStartTime, String nodeId, Integer rpcPort,
			boolean privateNetwork, String portCheckerUrl, String publicIp, boolean featureContracts,
			boolean featureRpc) {
		super();
		this.ethereumJVersion = ethereumJVersion;
		this.ethereumJBuildInfo = ethereumJBuildInfo;
		this.appVersion = appVersion;
		this.networkName = networkName;
		this.explorerUrl = explorerUrl;
		this.genesisHash = genesisHash;
		this.serverStartTime = serverStartTime;
		this.nodeId = nodeId;
		this.rpcPort = rpcPort;
		this.privateNetwork = privateNetwork;
		this.portCheckerUrl = portCheckerUrl;
		this.publicIp = publicIp;
		this.featureContracts = featureContracts;
		this.featureRpc = featureRpc;
	}

	public String getEthereumJVersion() {
		return ethereumJVersion;
	}

	public String getEthereumJBuildInfo() {
		return ethereumJBuildInfo;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public String getNetworkName() {
		return networkName;
	}

	public String getExplorerUrl() {
		return explorerUrl;
	}

	public String getGenesisHash() {
		return genesisHash;
	}

	public Long getServerStartTime() {
		return serverStartTime;
	}

	public String getNodeId() {
		return nodeId;
	}

	public Integer getRpcPort() {
		return rpcPort;
	}

	public boolean isPrivateNetwork() {
		return privateNetwork;
	}

	public String getPortCheckerUrl() {
		return portCheckerUrl;
	}

	public String getPublicIp() {
		return publicIp;
	}

	public boolean isFeatureContracts() {
		return featureContracts;
	}

	public boolean isFeatureRpc() {
		return featureRpc;
	}
}
