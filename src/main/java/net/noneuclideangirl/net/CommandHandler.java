package net.noneuclideangirl.net;

import net.noneuclideangirl.functional.Functional;
import net.noneuclideangirl.functional.Option;
import net.noneuclideangirl.net.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandHandler {
    private static final Logger log = LogManager.getLogger(CommandHandler.class);

    private final Map<String, AbstractRequestHandler> requestHandlers = new ConcurrentHashMap<>();

    public void registerRequestHandler(AbstractRequestHandler handler) {
        String commandName = handler.getCommandName().toUpperCase();
        if (requestHandlers.containsKey(commandName)) {
            log.warn("Asked to register handler for command \"" + commandName + "\" but handler was already registered");
        } else {
            requestHandlers.put(commandName, handler);
            log.info("Registered " + handler.getClass().getSimpleName());
        }
    }

    public Option<Document> process(Document request) {
        if (request.containsKey("command")) {
            String command = request.getString("command");
            return Option.of(requestHandlers.get(command.toUpperCase()))
                    .map(handler -> handler.process(request))
                    .ifNone(() -> log.warn("Unrecognised command \"" + command + "\""));
        } else {
            log.warn("Received document " + request.toJson() + " does not contain \"command\".");
            return Option.none();
        }
    }

    public Option<INetworkDocument> receiveRequest(Document request) {
        if (request.containsKey("http")) {
            return HttpResponse.respond(this, (Document) request.get("http"))
                    // Need to coerce types, so use identity
                               .map(Functional.id());
        } else {
            return process(request).map(INetworkDocument::from);
        }
    }
}
