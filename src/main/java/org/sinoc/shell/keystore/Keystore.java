package org.sinoc.shell.keystore;

import org.sinoc.crypto.ECKey;

/**
 * Each method could throw {RuntimeException}, because of access to IO and crypto functions.
 */
public interface Keystore {

    void removeKey(String address);

    void storeKey(ECKey key, String password) throws RuntimeException;

    void storeRawKeystore(String content, String address) throws RuntimeException;

    String[] listStoredKeys();

    ECKey loadStoredKey(String address, String password) throws RuntimeException;

    /**
     * Check if keystore has file with key for passed address.
     * @param address - 40 chars
     * @return
     */
    boolean hasStoredKey(String address);
}
