package com.example.broadcast.cli;

import picocli.CommandLine;

/**
 * Entrypoint for the broadcast-server CLI.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BroadcastServerCommand()).execute(args);
        System.exit(exitCode);
    }
}


