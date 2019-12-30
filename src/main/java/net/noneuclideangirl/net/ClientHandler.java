package net.noneuclideangirl.net;

import net.noneuclideangirl.net.http.HttpRequestReader;
import net.noneuclideangirl.util.ThreadManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.json.JsonParseException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler {
    private static final Logger log = LogManager.getLogger(ClientHandler.class);

    private final LinkedBlockingQueue<Document> toProcess = new LinkedBlockingQueue<>();
    private final Socket socket;
    private final CommandHandler commandHandler;
    private final HostPort hostPort;
    private final List<Runnable> toRunOnClose = new ArrayList<>();
    private boolean closed = false;

    public void runOnClose(Runnable r) {
        toRunOnClose.add(r);
    }

    public ClientHandler(Socket socket, CommandHandler commandHandler) {
        this.socket = socket;
        this.commandHandler = commandHandler;
        hostPort = new HostPort(socket);

        ThreadManager.execute(this::handleRequests);
        ThreadManager.execute(this::sendResponses);
    }

    private void handleRequests() {
        try (var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while (!closed && (message = reader.readLine()) != null) {
                if (message.length() == 0) {
                    log.info(hostPort.fullAddress + ": received empty message");
                } else {
                    try {
                        toProcess.add(Document.parse(message));
                    } catch (JsonParseException e) {
                        if (message.contains("HTTP")) {
                            var httpReader = new HttpRequestReader(message, reader);
                            if (httpReader.ok()) {
                                toProcess.add(new Document("http", httpReader.toDoc()));
                            } else {
                                log.info(hostPort.fullAddress
                                    + ": received malformed HTTP request:\n\""
                                    + message + "\"");
                            }
                        } else {
                            log.info(hostPort.fullAddress
                                    + ": received malformed JSON message \""
                                    + message + "\": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (!closed) {
                log.warn("Error reading from socket: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        } finally {
            close();
        }
    }

    private void sendResponses() {
        try (var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            while (!closed) {
                commandHandler.receiveRequest(toProcess.take())
                        .ifSome(doc -> {
                            try {
                                var response = doc.marshall();
                                log.info("Sending response: " + doc.toDoc().toJson());
                                writer.write(response, 0, response.length());
                                writer.flush();
                            } catch (IOException e) {
                                log.warn("Error writing response: " + doc.marshall() + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
                            }
                        });
            }
        } catch (IOException e) {
            if (!closed) {
                log.warn("Error opening output stream for socket: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for request" + e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            close();
        }
    }

    private void close() {
        if (!closed) {
            closed = true;
            log.info(hostPort.fullAddress + ": closed client handler");
            toRunOnClose.forEach(Runnable::run);
        }
    }
}
