package net.noneuclideangirl.net.http;

import net.noneuclideangirl.functional.Option;
import net.noneuclideangirl.net.CommandHandler;
import net.noneuclideangirl.net.INetworkDocument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

public abstract class HttpResponse implements INetworkDocument {
    private static final Logger log = LogManager.getLogger(HttpResponse.class);

    public static Option<HttpResponse> respond(CommandHandler handler, Document request) {
        // Check if request is a valid document
        try {
            request.getString("type");
        } catch (ClassCastException unused) {
            log.warn("Invalid value for request field `http`");
            return Option.none();
        }

        var type = request.getString("type");
        var body = (Document) request.get("body");

        switch (type) {
            case "OPTIONS":
                return Option.some(new CorsHttpResponse());
            case "POST":
                // Weird behaviour of types in Java means we have to check here if the body was correctly formatted
                try {
                    body.getString("command");
                } catch (ClassCastException unused) {
                    log.warn("Invalid value for HTTP request body");
                    return Option.none();
                }

                return handler.process(body).map(PostHttpResponse::new);
            default:
                log.warn("Unrecognised HTTP request type: " + type);
                return Option.none();
        }
    }
}
