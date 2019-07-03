package org.sinoc.sync;

/**
 * Manages sync measurements
 *
 */
public class SyncStatistics {
    private long updatedAt;
    private long blocksCount;
    private long headersCount;
    private int headerBunchesCount;

    public SyncStatistics() {
        reset();
    }

    public void reset() {
        updatedAt = System.currentTimeMillis();
        blocksCount = 0;
        headersCount = 0;
        headerBunchesCount = 0;
    }

    public void addBlocks(long cnt) {
        blocksCount += cnt;
        fixCommon(cnt);
    }

    public void addHeaders(long cnt) {
        headerBunchesCount++;
        headersCount += cnt;
        fixCommon(cnt);
    }

    private void fixCommon(long cnt) {
        updatedAt = System.currentTimeMillis();
    }

    public long getBlocksCount() {
        return blocksCount;
    }

    public long getHeadersCount() {
        return headersCount;
    }

    public long secondsSinceLastUpdate() {
        return (System.currentTimeMillis() - updatedAt) / 1000;
    }

    public int getHeaderBunchesCount() {
        return headerBunchesCount;
    }
}
