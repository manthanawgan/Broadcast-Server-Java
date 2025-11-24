package com.example.broadcast.core;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * Simple WebSocket client that forwards incoming messages to the console.
 */
public final class BroadcastClient extends WebSocketClient {
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Consumer<String> messageHandler;

    public BroadcastClient(URI serverUri, Consumer<String> messageHandler) {
        super(serverUri);
        this.messageHandler = messageHandler;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        messageHandler.accept(format("Connected to server (%s)", handshakedata.getHttpStatusMessage()));
    }

    @Override
    public void onMessage(String message) {
        messageHandler.accept(format(message));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        messageHandler.accept(format(
                "Connection closed (code=%d, remote=%s, reason=%s)", code, remote, reason));
    }

    @Override
    public void onError(Exception ex) {
        messageHandler.accept(format("Error: %s", ex.getMessage()));
    }

    private static String format(String template, Object... args) {
        return String.format("[%s] %s", TS_FORMAT.format(LocalDateTime.now()), String.format(template, args));
    }
}


