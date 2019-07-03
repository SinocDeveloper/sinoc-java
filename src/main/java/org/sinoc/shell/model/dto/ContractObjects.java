package org.sinoc.shell.model.dto;

public class ContractObjects {

    public static class ContractInfoDTO {

        private final String address;

        private final String name;

        /**
         * Block number when contract was introduced or -1.
         */
        private final long blockNumber;

		public ContractInfoDTO(String address, String name, long blockNumber) {
			super();
			this.address = address;
			this.name = name;
			this.blockNumber = blockNumber;
		}

		public String getAddress() {
			return address;
		}

		public String getName() {
			return name;
		}

		public long getBlockNumber() {
			return blockNumber;
		}
    }

    public static class IndexStatusDTO {

        private final long indexSize;

        private final String solcVersion;

        /**
         * Block number when indexing started or -1.
         * Zero value is not possible
         */
        private final long syncedBlock;

		public IndexStatusDTO(long indexSize, String solcVersion, long syncedBlock) {
			this.indexSize = indexSize;
			this.solcVersion = solcVersion;
			this.syncedBlock = syncedBlock;
		}

		public long getIndexSize() {
			return indexSize;
		}

		public String getSolcVersion() {
			return solcVersion;
		}

		public long getSyncedBlock() {
			return syncedBlock;
		}
    }
}


