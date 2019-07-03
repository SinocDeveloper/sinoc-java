package org.sinoc.net.shh;

import org.sinoc.net.message.Message;

public abstract class ShhMessage extends Message {

    public ShhMessage() {
    }

    public ShhMessage(byte[] encoded) {
        super(encoded);
    }

    public ShhMessageCodes getCommand() {
        return ShhMessageCodes.fromByte(code);
    }
}
