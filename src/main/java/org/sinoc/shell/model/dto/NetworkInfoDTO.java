package org.sinoc.shell.model.dto;

import java.util.ArrayList;
import java.util.List;

import org.sinoc.facade.SyncStatus;
import org.sinoc.facade.SyncStatus.SyncStage;

public class NetworkInfoDTO {

    private final Integer activePeers;

    private final SyncStatusDTO syncStatus;

    private final String mineStatus;

    private final Integer ethPort;

    private final Boolean ethAccessible;

    private final List<MinerDTO> miners = new ArrayList<>();
    
    public NetworkInfoDTO(Integer activePeers, SyncStatusDTO syncStatus, String mineStatus, Integer ethPort,
			Boolean ethAccessible) {
		super();
		this.activePeers = activePeers;
		this.syncStatus = syncStatus;
		this.mineStatus = mineStatus;
		this.ethPort = ethPort;
		this.ethAccessible = ethAccessible;
	}

    public static class SyncStatusDTO {

        private final org.sinoc.facade.SyncStatus.SyncStage stage;
        private final long curCnt;
        private final long knownCnt;
        private final long blockLastImported;
        private final long blockBestKnown;

        public static SyncStatusDTO instanceOf(SyncStatus status) {
            return new SyncStatusDTO(status.getStage(), status.getCurCnt(), status.getKnownCnt(),
                    status.getBlockLastImported(), status.getBlockBestKnown());
        }

		public SyncStatusDTO(SyncStage stage, long curCnt, long knownCnt, long blockLastImported, long blockBestKnown) {
			super();
			this.stage = stage;
			this.curCnt = curCnt;
			this.knownCnt = knownCnt;
			this.blockLastImported = blockLastImported;
			this.blockBestKnown = blockBestKnown;
		}

		public org.sinoc.facade.SyncStatus.SyncStage getStage() {
			return stage;
		}

		public long getCurCnt() {
			return curCnt;
		}

		public long getKnownCnt() {
			return knownCnt;
		}

		public long getBlockLastImported() {
			return blockLastImported;
		}

		public long getBlockBestKnown() {
			return blockBestKnown;
		}
    }

	public Integer getActivePeers() {
		return activePeers;
	}

	public SyncStatusDTO getSyncStatus() {
		return syncStatus;
	}

	public String getMineStatus() {
		return mineStatus;
	}

	public Integer getEthPort() {
		return ethPort;
	}

	public Boolean getEthAccessible() {
		return ethAccessible;
	}

	public List<MinerDTO> getMiners() {
		return miners;
	}
}


