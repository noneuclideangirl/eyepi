package net.noneuclideangirl.data;

import org.bson.Document;

public interface IMongoDocument {
    String getId();
    Document toDoc();

    default boolean equals(IMongoDocument other) {
        return toDoc().equals(other.toDoc());
    }
}
