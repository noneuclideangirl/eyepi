package net.noneuclideangirl.data;

import net.noneuclideangirl.functional.Option;
import net.noneuclideangirl.net.INetworkDocument;
import org.bson.Document;

public class ServiceDescriptor implements INetworkDocument {
    public final String name;
    public final String description;
    public final String exec;

    private ServiceDescriptor(String name, String description, String exec) {
        this.name = name;
        this.description = description;
        this.exec = exec;
    }

    public static Option<ServiceDescriptor> fromDoc(Document doc) {
        var name = doc.getString("name");
        var desc = doc.getString("desc");
        var exec = doc.getString("exec");
        if (name != null && desc != null && exec != null) {
            return Option.some(new ServiceDescriptor(name, desc, exec));
        } else {
            return Option.none();
        }
    }

    @Override
    public Document toDoc() {
        return new Document("name", name)
                .append("desc", description)
                .append("exec", exec);
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
