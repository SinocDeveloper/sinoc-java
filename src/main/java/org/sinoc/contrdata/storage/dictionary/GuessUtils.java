package org.sinoc.contrdata.storage.dictionary;


import org.apache.commons.lang3.*;
import java.math.*;
import org.spongycastle.util.encoders.*;

public class GuessUtils
{
    public static StorageDictionary.PathElement guessPathElement(final byte[] key, final byte[] storageKey) {
        if (ArrayUtils.isEmpty(key)) {
            return null;
        }
        StorageDictionary.PathElement el = null;
        final Object value = guessValue(key);
        if (value instanceof String) {
            el = StorageDictionary.PathElement.createMapKey((String)value, storageKey);
        }
        else if (value instanceof BigInteger) {
            final BigInteger bi = (BigInteger)value;
            if (bi.bitLength() < 32) {
                el = StorageDictionary.PathElement.createMapKey(bi.intValue(), storageKey);
            }
            else {
                el = StorageDictionary.PathElement.createMapKey("0x" + bi.toString(16), storageKey);
            }
        }
        return el;
    }
    
    public static Object guessValue(final byte[] bytes) {
        int startZeroCnt = 0;
        int startNonZeroCnt = 0;
        boolean asciiOnly = true;
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] != 0) {
                if (startNonZeroCnt <= 0 && i != 0) {
                    break;
                }
                ++startNonZeroCnt;
            }
            else {
                if (startZeroCnt <= 0 && i != 0) {
                    break;
                }
                ++startZeroCnt;
            }
            asciiOnly &= (bytes[i] > 31 && bytes[i] <= 126);
        }
        if (startZeroCnt > 16) {
            return new BigInteger(bytes);
        }
        if (asciiOnly) {
            return new String(bytes, 0, startNonZeroCnt);
        }
        return Hex.toHexString(bytes);
    }
}
