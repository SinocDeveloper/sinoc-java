package org.sinoc.net.eth.handler;

import com.google.common.util.concurrent.SettableFuture;

import java.util.List;

import org.sinoc.core.BlockHeader;
import org.sinoc.net.eth.message.GetBlockHeadersMessage;

/**
 * Wraps {@link GetBlockHeadersMessage},
 * adds some additional info required by get headers queue
 */
public class GetBlockHeadersMessageWrapper {

    private GetBlockHeadersMessage message;
    private boolean newHashesHandling = false;
    private boolean sent = false;
    private SettableFuture<List<BlockHeader>> futureHeaders = SettableFuture.create();

    public GetBlockHeadersMessageWrapper(GetBlockHeadersMessage message) {
        this.message = message;
    }

    public GetBlockHeadersMessageWrapper(GetBlockHeadersMessage message, boolean newHashesHandling) {
        this.message = message;
        this.newHashesHandling = newHashesHandling;
    }

    public GetBlockHeadersMessage getMessage() {
        return message;
    }

    public boolean isNewHashesHandling() {
        return newHashesHandling;
    }

    public boolean isSent() {
        return sent;
    }

    public void send() {
        this.sent = true;
    }

    public SettableFuture<List<BlockHeader>> getFutureHeaders() {
        return futureHeaders;
    }
}
