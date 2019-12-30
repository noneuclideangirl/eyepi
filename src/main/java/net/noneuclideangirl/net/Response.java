package net.noneuclideangirl.net;

import org.bson.Document;

public class Response {
    public static Document empty() {
        return new Document();
    }

    public static Document ok() {
        return empty().append("status", true);
    }

    public static Document err() {
        return empty().append("status", false);
    }
}
