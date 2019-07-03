package org.sinoc.net.message;

/**
 * Factory interface to create messages
 *
 */
public interface MessageFactory {

    /**
     * Creates message by absolute message codes
     * e.g. codes described in {@link org.sinoc.net.eth.message.EthMessageCodes}
     *
     * @param code message code
     * @param encoded encoded message bytes
     * @return created message
     *
     * @throws IllegalArgumentException if code is unknown
     */
    Message create(byte code, byte[] encoded);

}
