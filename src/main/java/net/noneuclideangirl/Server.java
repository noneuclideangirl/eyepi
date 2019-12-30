package net.noneuclideangirl;

import net.noneuclideangirl.requests.*;
import net.noneuclideangirl.net.ClientHandler;
import net.noneuclideangirl.net.CommandHandler;
import net.noneuclideangirl.util.ConfigManager;
import net.noneuclideangirl.util.ThreadManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Server {
    private static final Logger log = LogManager.getLogger(Server.class);
    private final CommandHandler commandHandler;
    private final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        // TODO: command line arg handling e.g. alternate properties location
        ConfigManager.loadProperties("/var/eyepi/eyepi.properties");
        DatabaseManager.load();

        ThreadManager.execute(new Server()::run);
    }

    private Server() {
        DatabaseManager.get().findAllServices().forEach(ServiceMonitor::new);
        log.info("Service monitors created.");

        commandHandler = new CommandHandler();
        commandHandler.registerRequestHandler(new ServerStatusRequestHandler());
        commandHandler.registerRequestHandler(new ListServicesRequestHandler());
        commandHandler.registerRequestHandler(new StopServiceRequestHandler());
        commandHandler.registerRequestHandler(new StartServiceRequestHandler());
        commandHandler.registerRequestHandler(new ServiceLogsRequestHandler());
        commandHandler.registerRequestHandler(new ServiceStartTimeRequestHandler());
        commandHandler.registerRequestHandler(new CreateServiceRequestHandler());
        commandHandler.registerRequestHandler(new EditServiceRequestHandler());
        commandHandler.registerRequestHandler(new DeleteServiceRequestHandler());
        log.info("Command handlers created.");
    }

    private void run() {
        var port = ConfigManager.getInt("port").unwrap();
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            log.error("Failed to open server socket on port " + port + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return;
        }

        while (!socket.isClosed()) {
            try {
                var clientSocket = socket.accept();
                var client = new ClientHandler(clientSocket, commandHandler);
                log.info("Accepted client connection from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                operateAndCount(clients::add, client);
                client.runOnClose(() -> operateAndCount(clients::remove, client));
            } catch (IOException e) {
                log.warn("Failed to accept client connection" + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    private void operateAndCount(Consumer<ClientHandler> op, ClientHandler client) {
        int count;
        synchronized (clients) {
            op.accept(client);
            count = clients.size();
        }
        log.info("(currently " + count + " client" + (count == 1 ? "" : "s") + ")");
    }
}
