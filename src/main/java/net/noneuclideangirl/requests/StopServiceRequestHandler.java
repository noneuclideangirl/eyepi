package net.noneuclideangirl.requests;

import net.noneuclideangirl.ServiceMonitor;
import net.noneuclideangirl.net.AbstractRequestHandler;
import net.noneuclideangirl.net.Response;
import net.noneuclideangirl.DatabaseManager;
import org.bson.Document;

public class StopServiceRequestHandler extends AbstractRequestHandler {
    public static final String COMMAND_NAME = "STOP_SERVICE";

    public StopServiceRequestHandler() {
        super(COMMAND_NAME);
    }

    @Override
    protected Document process(Document doc) {
        return DatabaseManager.get().findServiceByIdOrName(doc)
                      .map(ServiceMonitor::stopService)
                      .matchThen(result -> result ? Response.ok() : Response.err(),
                                 Response::err);
    }
}
