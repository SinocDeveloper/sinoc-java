package org.sinoc.sync;

import java.util.*;

import org.sinoc.core.Block;
import org.sinoc.core.BlockHeaderWrapper;
import org.sinoc.util.ByteArrayMap;
import org.sinoc.util.FastByteComparisons;
import org.sinoc.util.MinMaxMap;

public class SyncQueueReverseImpl implements SyncQueueIfc {

    byte[] curHeaderHash;

    MinMaxMap<BlockHeaderWrapper> headers = new MinMaxMap<>();
    long minValidated = -1;
    long finishValidated = 0;

    ByteArrayMap<Block> blocks = new ByteArrayMap<>();

    boolean headersOnly;

    public SyncQueueReverseImpl(byte[] startHash) {
        this.curHeaderHash = startHash;
    }

    public SyncQueueReverseImpl(byte[] startHash, long finishValidated) {
        this.curHeaderHash = startHash;
        this.finishValidated = finishValidated;
    }

    public SyncQueueReverseImpl(byte[] startHash, boolean headersOnly) {
        this.curHeaderHash = startHash;
        this.headersOnly = headersOnly;
    }

    @Override
    public synchronized List<HeadersRequest> requestHeaders(int maxSize, int maxRequests, int maxTotalHeaders) {
        List<HeadersRequest> ret = new ArrayList<>();
        if (maxTotalHeaders == 0) return ret;
        int totalHeaders = 0;

        if (minValidated < 0) {
            ret.add(new SyncQueueImpl.HeadersRequestImpl(curHeaderHash, maxSize, true, maxSize - 1));
            totalHeaders += maxSize;
            if (totalHeaders >= maxTotalHeaders) return ret;
        } else if (minValidated == finishValidated) {
            // genesis reached
            return null;
        } else {
            if (minValidated - headers.getMin() < maxSize * maxSize && minValidated > maxSize) {
                ret.add(new SyncQueueImpl.HeadersRequestImpl(
                        headers.get(headers.getMin()).getHash(), maxSize, true, maxSize - 1));
                maxRequests--;
                totalHeaders += maxSize;
            }

            Set<Map.Entry<Long, BlockHeaderWrapper>> entries =
                    headers.descendingMap().subMap(minValidated, true, headers.getMin(), true).entrySet();
            Iterator<Map.Entry<Long, BlockHeaderWrapper>> it = entries.iterator();
            BlockHeaderWrapper prevEntry = it.next().getValue();
            while(maxRequests > 0 && totalHeaders < maxTotalHeaders && it.hasNext()) {
                BlockHeaderWrapper entry = it.next().getValue();
                if (prevEntry.getNumber() - entry.getNumber() > 1) {
                    ret.add(new SyncQueueImpl.HeadersRequestImpl(prevEntry.getHash(), maxSize, true));
                    totalHeaders += maxSize;
                    maxRequests--;
                }
                prevEntry = entry;
            }
            if (maxRequests > 0 && totalHeaders < maxTotalHeaders) {
                ret.add(new SyncQueueImpl.HeadersRequestImpl(prevEntry.getHash(), maxSize, true));
            }
        }

        return ret;
    }

    @Override
    public synchronized List<BlockHeaderWrapper> addHeaders(Collection<BlockHeaderWrapper> newHeaders) {
        if (minValidated < 0) {
            // need to fetch initial header
            for (BlockHeaderWrapper header : newHeaders) {
                if (FastByteComparisons.equal(curHeaderHash, header.getHash())) {
                    minValidated = header.getNumber();
                    headers.put(header.getNumber(), header);
                }
            }
        }

        // start header not found or we are already done
        if (minValidated <= finishValidated) return Collections.emptyList();

        for (BlockHeaderWrapper header : newHeaders) {
            if (header.getNumber() < minValidated) {
                headers.put(header.getNumber(), header);
            }
        }

        for (; minValidated >= headers.getMin() && minValidated >= finishValidated; minValidated--) {
            BlockHeaderWrapper header = headers.get(minValidated);
            BlockHeaderWrapper parent = headers.get(minValidated - 1);
            if (parent == null) {
                // Some peers doesn't return 0 block header
                if (minValidated == 1 && finishValidated == 0) minValidated = 0;
                break;
            }
            if (!FastByteComparisons.equal(header.getHeader().getParentHash(), parent.getHash())) {
                // chain is broken here (unlikely) - refetch the rest
                headers.clearAllBefore(header.getNumber());
                break;
            }
        }
        if (headersOnly) {
            List<BlockHeaderWrapper> ret = new ArrayList<>();
            for (long i = headers.getMax(); i > minValidated; i--) {
                ret.add(headers.remove(i));
            }
            return ret;
        } else {
            return null;
        }
    }

    @Override
    public synchronized BlocksRequest requestBlocks(int maxSize) {
        List<BlockHeaderWrapper> reqHeaders = new ArrayList<>();
        for (BlockHeaderWrapper header : headers.descendingMap().values()) {
            if (maxSize == 0) break;
            if (blocks.get(header.getHash()) == null) {
                reqHeaders.add(header);
                maxSize--;
            }
        }
        return new SyncQueueImpl.BlocksRequestImpl(reqHeaders);
    }

    @Override
    public synchronized List<Block> addBlocks(Collection<Block> newBlocks) {
        for (Block block : newBlocks) {
            blocks.put(block.getHash(), block);
        }
        List<Block> ret = new ArrayList<>();
        for (long i = headers.getMax(); i > minValidated; i--) {
            Block block = blocks.get(headers.get(i).getHash());
            if (block == null) break;
            ret.add(block);
            blocks.remove(headers.get(i).getHash());
            headers.remove(i);
        }
        return ret;
    }

    @Override
    public synchronized int getHeadersCount() {
        return headers.size();
    }

    public synchronized int getValidatedHeadersCount() {
        return headers.getMax() == null ? 0 : (int) (headers.getMax() - minValidated + 1);
    }
}
