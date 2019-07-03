package org.sinoc.util;

import java.io.Serializable;

/**
 * Wrapper class for decoded elements from an RLP encoded byte array.
 */
public interface RLPElement extends Serializable {

    byte[] getRLPData();
}
