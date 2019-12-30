package net.noneuclideangirl.net;

import org.bson.Document;

public interface INetworkDocument {
    default String marshall() {
        return toDoc().toJson() + "\n";
    }
    Document toDoc();

    static INetworkDocument from(Document doc) {
        return new INetworkDocument() {
            @Override
            public String marshall() {
                return doc.toJson() + "\n";
            }

            @Override
            public Document toDoc() {
                return doc;
            }
        };
    }

}
