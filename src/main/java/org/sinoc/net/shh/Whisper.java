package org.sinoc.net.shh;

import org.sinoc.crypto.ECKey;
import org.springframework.stereotype.Component;

@Component
public abstract class Whisper {

    public abstract String addIdentity(ECKey key);

    public abstract String newIdentity();

    public abstract void watch(MessageWatcher f);

    public abstract void unwatch(MessageWatcher f);

    public void send(byte[] payload, Topic[] topics) {
        send(null, null, payload, topics, 50, 50);
    }
    public void send(byte[] payload, Topic[] topics, int ttl, int workToProve) {
        send(null, null, payload, topics, ttl, workToProve);
    }
    public void send(String toIdentity, byte[] payload, Topic[] topics) {
        send(null, toIdentity, payload, topics, 50, 50);
    }
    public void send(String toIdentity, byte[] payload, Topic[] topics, int ttl, int workToProve) {
        send(null, toIdentity, payload, topics, ttl, workToProve);
    }

    public void send(String fromIdentity, String toIdentity, byte[] payload, Topic[] topics) {
        send(fromIdentity, toIdentity, payload, topics, 50, 50);
    }

    public abstract void send(String fromIdentity, String toIdentity, byte[] payload, Topic[] topics, int ttl, int workToProve);
}
