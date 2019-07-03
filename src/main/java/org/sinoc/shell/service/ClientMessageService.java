package org.sinoc.shell.service;

public interface ClientMessageService {
    void sendToTopic(String topic, Object dto);
}
