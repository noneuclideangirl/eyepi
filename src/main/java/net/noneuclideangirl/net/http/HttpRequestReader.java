package net.noneuclideangirl.net.http;

import net.noneuclideangirl.functional.Option;
import net.noneuclideangirl.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BSONException;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;

public class HttpRequestReader {
    private static final Logger log = LogManager.getLogger(HttpRequestReader.class);
    public final String type;
    private final Option<Document> header;
    private final Option<Document> body;

    public HttpRequestReader(String firstLine, BufferedReader socketReader) {
        type = firstLine.split(" ")[0];
        header = processHeader(socketReader);
        body = header.andThen(d -> {
            var contentLength = d.getString("Content-Length");
            if (Util.isInteger(contentLength)) {
                return processBody(socketReader, Integer.parseInt(contentLength));
            } else if (contentLength == null) {
                return Option.some(new Document());
            } else {
                log.warn("Received malformed Content-Length: " + contentLength);
                return Option.none();
            }
        });
        body.ifSome(b -> log.info("Received request: " + type + " " + b.toJson()));
    }

    public boolean ok() {
        return header.isSome() && body.isSome();
    }

    private Option<Document> processHeader(BufferedReader socketReader) {
        try {
            String line;
            var doc = new Document();
            while (!(line = socketReader.readLine()).equals("")) {
                var splitLine = line.split(": ");
                doc.append(splitLine[0], splitLine[1]);
            }
            return Option.some(doc);
        } catch (IOException e) {
            return Option.none();
        }
    }

    private Option<Document> processBody(BufferedReader socketReader, int length) {
        try {
            if (length == 0) {
                return Option.some(new Document());
            } else {
                var buffer = new char[length];
                var result = socketReader.read(buffer, 0, length);
                var json = String.valueOf(buffer);

                if (result == -1) {
                    return Option.some(new Document());
                } else {
                    try {
                        return Option.some(Document.parse(json));
                    } catch (BSONException e) {
                        log.warn("Failed to parse JSON `" + json + "`: " + e.getClass().getCanonicalName() + ": " + e.getMessage());
                        return Option.none();
                    }
                }
            }
        } catch (IOException e) {
            return Option.none();
        }
    }

    public Option<Document> getHeader() {
        return header;
    }

    public Option<Document> getBody() {
        return body;
    }

    public Document toDoc() {
        return new Document("type", type)
                .append("header", header.orElse(new Document()))
                .append("body", body.orElse(new Document()));
    }
}
