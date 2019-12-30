package net.noneuclideangirl.data;

import net.noneuclideangirl.functional.Option;
import net.noneuclideangirl.net.INetworkDocument;
import org.bson.Document;
import org.bson.types.ObjectId;

public class ServiceDescriptor implements INetworkDocument {
    public final Option<String> id;
    public final String name;
    public final String description;
    public final String exec;

    public ServiceDescriptor(String name, String description, String exec) {
        this(Option.none(), name, description, exec);
    }

    private ServiceDescriptor(Option<String> id, String name, String description, String exec) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.exec = exec;
    }

    public static Option<ServiceDescriptor> fromDoc(Document doc) {
        if (doc == null) {
            return Option.none();
        } else {
            var id = Option.of(doc.getObjectId("_id")).map(ObjectId::toString)
                    .or(Option.of(doc.getString("id")));

            var name = doc.getString("name");
            var desc = doc.getString("desc");
            var exec = doc.getString("exec");

            if (name != null && desc != null && exec != null) {
                return Option.some(new ServiceDescriptor(id, name, desc, exec));
            } else {
                return Option.none();
            }
        }
    }

    @Override
    public Document toDoc() {
        var doc = new Document("name", name)
                .append("desc", description)
                .append("exec", exec);
        id.ifSome(idStr -> doc.append("id", idStr));
        return doc;
    }

    @Override
    public boolean equals(Object rhs) {
        return rhs instanceof ServiceDescriptor && name.equals(((ServiceDescriptor) rhs).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return toDoc().toJson();
    }
}
