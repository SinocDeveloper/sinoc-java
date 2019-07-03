package org.sinoc.shell.model.dto;

public class MachineInfoDTO {

    /**
     * Percentage 0..100
     */
    private final Integer cpuUsage;

    /**
     * In bytes.
     */
    private final Long memoryFree;

    /**
     * In bytes.
     */
    private final Long memoryTotal;

    /**
     * In bytes.
     */
    private final  Long dbSize;

    /**
     * In bytes.
     */
    private final  Long freeSpace;

	public MachineInfoDTO(Integer cpuUsage, Long memoryFree, Long memoryTotal, Long dbSize, Long freeSpace) {
		this.cpuUsage = cpuUsage;
		this.memoryFree = memoryFree;
		this.memoryTotal = memoryTotal;
		this.dbSize = dbSize;
		this.freeSpace = freeSpace;
	}

	public Integer getCpuUsage() {
		return cpuUsage;
	}

	public Long getMemoryFree() {
		return memoryFree;
	}

	public Long getMemoryTotal() {
		return memoryTotal;
	}

	public Long getDbSize() {
		return dbSize;
	}

	public Long getFreeSpace() {
		return freeSpace;
	}
    
}
