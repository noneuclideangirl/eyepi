package net.noneuclideangirl.requests;

import net.noneuclideangirl.ServiceMonitor;
import net.noneuclideangirl.net.AbstractRequestHandler;
import net.noneuclideangirl.net.Response;
import net.noneuclideangirl.DatabaseManager;
import org.bson.Document;

public class StartServiceRequestHandler extends AbstractRequestHandler {
    public static final String COMMAND_NAME = "START_SERVICE";

    public StartServiceRequestHandler() {
        super(COMMAND_NAME);
    }

    @Override
    protected Document process(Document doc) {
        return DatabaseManager.get().findServiceByIdOrName(doc)
                      .map(ServiceMonitor::startService)
                      .matchThen(result -> result ? Response.ok() : Response.err(),
                                 Response::err);
    }
}
