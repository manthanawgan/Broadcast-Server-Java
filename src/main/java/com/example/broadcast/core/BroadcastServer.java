package com.example.broadcast.core;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * Minimal WebSocket broadcast server extending the Java-WebSocket library.
 */
public final class BroadcastServer extends WebSocketServer {
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Map<WebSocket, String> clientNames = new ConcurrentHashMap<>();
    private final AtomicInteger clientCounter = new AtomicInteger(1);

    public BroadcastServer(String host, int port) {
        super(new InetSocketAddress(host, port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String clientId = "client-" + clientCounter.getAndIncrement();
        clientNames.put(conn, clientId);
        log("%s connected from %s", clientId, conn.getRemoteSocketAddress());
        broadcastSystemMessage(clientId + " joined");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String clientId = clientNames.remove(conn);
        if (clientId != null) {
            broadcastSystemMessage(clientId + " left");
            log("%s disconnected (code=%d, remote=%s, reason=%s)", clientId, code, remote, reason);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String clientId = clientNames.getOrDefault(conn, "unknown");
        String payload = String.format("[%s] %s", clientId, message);
        broadcast(payload);
        log("Broadcast %s", payload);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        String owner = conn != null ? clientNames.getOrDefault(conn, conn.toString()) : "server";
        log("Error for %s: %s", owner, ex.getMessage());
    }

    @Override
    public void onStart() {
        log("Broadcast server started on %s", getAddress());
        setConnectionLostTimeout(10);
    }

    private void broadcastSystemMessage(String message) {
        broadcast(String.format("[system] %s", message));
    }

    private static void log(String template, Object... args) {
        String ts = TS_FORMAT.format(LocalDateTime.now());
        System.out.printf("[%s] %s%n", ts, String.format(template, args));
    }
}


