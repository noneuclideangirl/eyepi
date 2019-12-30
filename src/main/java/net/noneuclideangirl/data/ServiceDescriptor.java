package net.noneuclideangirl.data;

import net.noneuclideangirl.functional.Option;
import org.bson.Document;
import org.bson.types.ObjectId;

public class ServiceDescriptor implements IMongoDocument {
    public final String id;
    public String name;
    public String description;
    public String exec;

    private ServiceDescriptor(String id, String name, String description, String exec) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.exec = exec;
    }

    public static Option<ServiceDescriptor> fromDoc(Document doc) {
        var id   = Option.of(doc.getObjectId("_id")).map(ObjectId::toString)
                         .or(Option.of(doc.getString("id")))
                         .unwrapOr(null);
        var name = doc.getString("name");
        var desc = doc.getString("desc");
        var exec = doc.getString("exec");
        if (id != null && name != null && desc != null && exec != null) {
            return Option.some(new ServiceDescriptor(id, name, desc, exec));
        } else {
            return Option.none();
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Document toDoc() {
        return new Document("id", id)
                .append("name", name)
                .append("desc", description)
                .append("exec", exec);
    }

    @Override
    public boolean equals(Object rhs) {
        return rhs instanceof ServiceDescriptor && this.equals((ServiceDescriptor) rhs);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return toDoc().toJson();
    }
}
