package com.example.broadcast.cli;

import picocli.CommandLine.Command;

@Command(
        name = "broadcast-server",
        description = "Utility CLI for running the WebSocket broadcast demo.",
        mixinStandardHelpOptions = true,
        version = "broadcast-server 1.0.0",
        subcommands = {StartCommand.class, ConnectCommand.class})
final class BroadcastServerCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Specify a subcommand: start or connect (use --help for details).");
    }
}


