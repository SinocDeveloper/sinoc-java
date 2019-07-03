package org.sinoc.net.swarm;

/**
 * Interface similar to ByteBuffer for reading large streaming or random access data
 *
 */
public interface SectionReader {

    long seek(long offset, int whence /* ??? */);

    int read(byte[] dest, int destOff);

    int readAt(byte[] dest, int destOff, long readerOffset);

    long getSize();
}
