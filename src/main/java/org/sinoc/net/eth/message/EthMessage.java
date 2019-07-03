package org.sinoc.net.eth.message;

import org.sinoc.net.message.Message;

public abstract class EthMessage extends Message {

    public EthMessage() {
    }

    public EthMessage(byte[] encoded) {
        super(encoded);
    }

    abstract public EthMessageCodes getCommand();
}
