package org.sinoc.net.p2p;

import static org.sinoc.net.message.ReasonCode.UNKNOWN;

import org.sinoc.net.message.ReasonCode;
import org.sinoc.util.RLP;
import org.sinoc.util.RLPList;

/**
 * Wrapper around an Ethereum Disconnect message on the network
 *
 * @see org.sinoc.net.p2p.P2pMessageCodes#DISCONNECT
 */
public class DisconnectMessage extends P2pMessage {

    private ReasonCode reason;

    public DisconnectMessage(byte[] encoded) {
        super(encoded);
    }

    public DisconnectMessage(ReasonCode reason) {
        this.reason = reason;
        parsed = true;
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        if (paramsList.size() > 0) {
            byte[] reasonBytes = paramsList.get(0).getRLPData();
            if (reasonBytes == null)
                this.reason = UNKNOWN;
            else
                this.reason = ReasonCode.fromInt(reasonBytes[0]);
        } else {
            this.reason = UNKNOWN;
        }

        parsed = true;
    }

    private void encode() {
        byte[] encodedReason = RLP.encodeByte(this.reason.asByte());
        this.encoded = RLP.encodeList(encodedReason);
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    @Override
    public P2pMessageCodes getCommand() {
        return P2pMessageCodes.DISCONNECT;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public ReasonCode getReason() {
        if (!parsed) parse();
        return reason;
    }

    public String toString() {
        if (!parsed) parse();
        return "[" + this.getCommand().name() + " reason=" + reason + "]";
    }
}