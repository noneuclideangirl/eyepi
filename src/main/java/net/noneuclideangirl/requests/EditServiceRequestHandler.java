package net.noneuclideangirl.requests;

import net.noneuclideangirl.DatabaseManager;
import net.noneuclideangirl.functional.Option;
import net.noneuclideangirl.net.AbstractRequestHandler;
import net.noneuclideangirl.net.Response;
import org.bson.Document;

public class EditServiceRequestHandler extends AbstractRequestHandler {
    public static final String COMMAND_NAME = "EDIT_SERVICE";

    public EditServiceRequestHandler() {
        super(COMMAND_NAME);
    }

    @Override
    protected Document process(Document doc) {
        // Must provide ID
        return Option.of(doc.getString("id"))
                         .map(id -> DatabaseManager.get().updateService(id, doc))
                         .orElse(false)
                ? Response.ok()
                : Response.err();
    }
}
