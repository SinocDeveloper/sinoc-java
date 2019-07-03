package org.sinoc.contrdata.storage.dictionary;


import java.util.concurrent.atomic.*;
import org.sinoc.crypto.*;
import org.springframework.stereotype.*;
import java.util.*;
import java.math.*;
import org.sinoc.util.*;

public interface Layout
{
    public enum Lang
    {
        solidity, 
        serpent;
        
        private final AtomicReference<Object> fingerprint;
        
        private Lang() {
            this.fingerprint = new AtomicReference<Object>();
        }
        
        private byte[] fingerprint() {
            return HashUtil.sha3(this.name().getBytes());
        }
        
        public byte[] getFingerprint() {
            Object value = this.fingerprint.get();
            if (value == null) {
                synchronized (this.fingerprint) {
                    value = this.fingerprint.get();
                    if (value == null) {
                        final byte[] actualValue = this.fingerprint();
                        value = ((actualValue == null) ? this.fingerprint : actualValue);
                        this.fingerprint.set(value);
                    }
                }
            }
            return (byte[])((value == this.fingerprint) ? null : value);
        }
    }
    
    public static class SerpentDictPathResolver implements DictPathResolver
    {
        @Override
        public Lang getLang() {
            return Lang.serpent;
        }
        
        @Override
        public StorageDictionary.PathElement[] resolvePath(final byte[] key, final Sha3Index index) {
            final Sha3Index.Entry entry = index.get(key);
            if (entry != null && entry.getInput().length > 32 && entry.getInput().length % 32 == 0 && Arrays.equals(key, entry.getOutput())) {
                final int pathLength = entry.getInput().length / 32;
                final StorageDictionary.PathElement[] ret = new StorageDictionary.PathElement[pathLength];
                for (int i = 0; i < ret.length; ++i) {
                    final byte[] storageKey = HashUtil.sha3(entry.getInput(), 0, (i + 1) * 32);
                    ret[i] = GuessUtils.guessPathElement(Arrays.copyOfRange(entry.getInput(), i * 32, (i + 1) * 32), storageKey);
                    ret[i].type = StorageDictionary.PathElement.Type.MapKey;
                }
                return ret;
            }
            final StorageDictionary.PathElement storageIndex = GuessUtils.guessPathElement(key, key);
            storageIndex.type = StorageDictionary.PathElement.Type.StorageIndex;
            return StorageDictionary.pathElements(storageIndex);
        }
    }
    
    @Component
    public static class SolidityDictPathResolver implements DictPathResolver
    {
        @Override
        public Lang getLang() {
            return Lang.solidity;
        }
        
        @Override
        public StorageDictionary.PathElement[] resolvePath(final byte[] key, final Sha3Index index) {
            final Sha3Index.Entry sha3 = index.get(key);
            if (Objects.isNull(sha3)) {
                final StorageDictionary.PathElement pathElement = GuessUtils.guessPathElement(key, key);
                pathElement.type = StorageDictionary.PathElement.Type.StorageIndex;
                pathElement.storageKey = key;
                return StorageDictionary.pathElements(pathElement);
            }
            final byte[] subKey = Arrays.copyOfRange(sha3.getInput(), 0, sha3.getInput().length - 32);
            final byte[] nxtKey = Arrays.copyOfRange(sha3.getInput(), sha3.getInput().length - 32, sha3.getInput().length);
            final StorageDictionary.PathElement containerKey = GuessUtils.guessPathElement(subKey, StorageDictionary.PathElement.toVirtualStorageKey(sha3.getOutput()));
            final StorageDictionary.PathElement.Type type = (subKey.length == 0) ? StorageDictionary.PathElement.Type.ArrayIndex : StorageDictionary.PathElement.Type.Offset;
            final int offset = new BigInteger(key).subtract(new BigInteger(sha3.getOutput())).intValue();
            final StorageDictionary.PathElement containerValKey = new StorageDictionary.PathElement(type, offset, key);
            return (StorageDictionary.PathElement[])Utils.mergeArrays((Object[][])new StorageDictionary.PathElement[][] { this.resolvePath(nxtKey, index), StorageDictionary.pathElements(Objects.isNull(containerKey) ? StorageDictionary.emptyPathElements() : StorageDictionary.pathElements(containerKey)), StorageDictionary.pathElements(containerValKey) });
        }
    }
    
    public interface DictPathResolver
    {
        Lang getLang();
        
        StorageDictionary.PathElement[] resolvePath(final byte[] p0, final Sha3Index p1);
    }
}

