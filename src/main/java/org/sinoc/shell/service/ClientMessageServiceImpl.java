package org.sinoc.shell.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Created by Stan Reshetnyk on 11.07.16.
 *
 * Encapsulates specific code for sending messages to client side.
 */
public class ClientMessageServiceImpl implements ClientMessageService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendToTopic(String topic, Object dto) {
        messagingTemplate.convertAndSend(topic, dto);
    }
}
