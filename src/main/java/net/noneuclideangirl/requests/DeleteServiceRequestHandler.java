package net.noneuclideangirl.requests;

import net.noneuclideangirl.DatabaseManager;
import net.noneuclideangirl.net.AbstractRequestHandler;
import net.noneuclideangirl.net.Response;
import org.bson.Document;

public class DeleteServiceRequestHandler extends AbstractRequestHandler {
    public static final String COMMAND_NAME = "DELETE_SERVICE";

    public DeleteServiceRequestHandler() {
        super(COMMAND_NAME);
    }

    @Override
    protected Document process(Document doc) {
        return DatabaseManager.get().deleteServiceByIdOrName(doc)
            ? Response.ok()
            : Response.err();
    }
}
