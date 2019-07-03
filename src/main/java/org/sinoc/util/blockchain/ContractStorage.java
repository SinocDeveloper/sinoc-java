package org.sinoc.util.blockchain;

/**
 * Represents the contract storage which is effectively the
 * mapping( uint256 => uint256 )
 */
public interface ContractStorage {
    byte[] getStorageSlot(long slot);
    byte[] getStorageSlot(byte[] slot);
}
