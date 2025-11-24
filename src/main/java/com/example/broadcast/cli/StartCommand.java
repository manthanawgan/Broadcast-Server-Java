package com.example.broadcast.cli;

import com.example.broadcast.core.BroadcastServer;
import java.util.concurrent.CountDownLatch;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "start", description = "Start the broadcast WebSocket server.")
final class StartCommand implements Runnable {
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;

    @Option(
            names = {"-p", "--port"},
            description = "Port to bind (default: ${DEFAULT-VALUE}).",
            defaultValue = "8080")
    private int port;

    @Option(
            names = {"-h", "--host"},
            description = "Hostname/interface to bind (default: ${DEFAULT-VALUE}).",
            defaultValue = "0.0.0.0")
    private String host;

    @Override
    public void run() {
        BroadcastServer server = new BroadcastServer(host, port);
        CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> stopServer(server, latch), "server-shutdown"));

        try {
            server.start();
            System.out.printf("Server ready on ws://%s:%d (%s)%n", host, port, Thread.currentThread().getName());
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            System.err.printf("Failed to start server: %s%n", ex.getMessage());
        }
    }

    private void stopServer(BroadcastServer server, CountDownLatch latch) {
        try {
            System.out.println("Shutting down server...");
            server.stop(SHUTDOWN_TIMEOUT_SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            System.err.printf("Error while stopping server: %s%n", ex.getMessage());
        } finally {
            latch.countDown();
        }
    }
}


