package org.sinoc.shell.service;

/**
 * Dummy for usage when ClientMessageService is not available
 */
public class ClientMessageServiceDummy implements ClientMessageService {

    @Override
    public void sendToTopic(String topic, Object dto) {
        // nothing to do
    }
}
