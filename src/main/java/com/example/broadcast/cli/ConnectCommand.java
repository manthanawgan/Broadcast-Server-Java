package com.example.broadcast.cli;

import com.example.broadcast.core.BroadcastClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "connect", description = "Connect a client to the broadcast server.")
final class ConnectCommand implements Callable<Integer> {
    @Option(
            names = {"-h", "--host"},
            description = "Server host (default: ${DEFAULT-VALUE}).",
            defaultValue = "localhost")
    private String host;

    @Option(
            names = {"-p", "--port"},
            description = "Server port (default: ${DEFAULT-VALUE}).",
            defaultValue = "8080")
    private int port;

    @Option(
            names = {"-n", "--name"},
            description = "Optional nickname (sent as first message).")
    private String nickname;

    @Override
    public Integer call() {
        URI serverUri;
        try {
            serverUri = new URI(String.format("ws://%s:%d", host, port));
        } catch (URISyntaxException ex) {
            System.err.printf("Invalid server URI: %s%n", ex.getMessage());
            return 1;
        }

        BroadcastClient client = new BroadcastClient(serverUri, System.out::println);
        try {
            System.out.printf("Connecting to %s...%n", serverUri);
            client.connectBlocking();
            if (nickname != null && !nickname.isBlank()) {
                client.send(String.format("%s joined the chat", nickname));
            }
            readInputLoop(client);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return 1;
        } catch (Exception ex) {
            System.err.printf("Connection error: %s%n", ex.getMessage());
            return 1;
        } finally {
            try {
                client.closeBlocking();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        return 0;
    }

    private void readInputLoop(BroadcastClient client) throws IOException {
        System.out.println("You can now type messages. Use /quit to exit.");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equalsIgnoreCase("/quit")) {
                break;
            }
            if (line.isBlank()) {
                continue;
            }
            client.send(line);
        }
    }
}


