package org.sinoc.net.shh;

import static org.sinoc.net.shh.ShhMessageCodes.MESSAGE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sinoc.util.RLP;
import org.sinoc.util.RLPList;

public class ShhEnvelopeMessage extends ShhMessage {

    private List<WhisperMessage> messages = new ArrayList<>();

    public ShhEnvelopeMessage(byte[] encoded) {
        super(encoded);
        parse();
    }

    public ShhEnvelopeMessage(WhisperMessage ... msg) {
        Collections.addAll(messages, msg);
        parsed = true;
    }

    public ShhEnvelopeMessage(Collection<WhisperMessage> msg) {
        messages.addAll(msg);
        parsed = true;
    }

    @Override
    public ShhMessageCodes getCommand() {
        return MESSAGE;
    }

    public void addMessage(WhisperMessage msg) {
        messages.add(msg);
    }

    public void parse() {
        if (!parsed) {
            RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

            for (int i = 0; i < paramsList.size(); i++) {
                messages.add(new WhisperMessage(paramsList.get(i).getRLPData()));
            }
            this.parsed = true;
        }
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) {
            byte[][] encodedMessages = new byte[messages.size()][];
            for (int i = 0; i < encodedMessages.length; i++) {
                encodedMessages[i] = messages.get(i).getEncoded();
            }
            encoded = RLP.encodeList(encodedMessages);
        }
        return encoded;
    }

    public List<WhisperMessage> getMessages() {
        return messages;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public String toString() {
        return "[ENVELOPE " + messages.toString() + "]";
    }
}
