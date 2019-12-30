package net.noneuclideangirl.commands;

import net.noneuclideangirl.ServiceMonitor;
import net.noneuclideangirl.data.ServiceDescriptor;
import net.noneuclideangirl.net.AbstractRequestHandler;
import net.noneuclideangirl.net.Response;
import net.noneuclideangirl.util.DatabaseManager;
import org.bson.Document;

public class StopServiceRequestHandler extends AbstractRequestHandler {
    public static final String COMMAND_NAME = "STOP_SERVICE";

    public StopServiceRequestHandler() {
        super(COMMAND_NAME);
    }

    @Override
    protected Document process(Document doc) {
        return DatabaseManager.get().findServiceByIdOrName(doc)
                      .andThen(ServiceDescriptor::fromDoc)
                      .map(ServiceMonitor::stopService)
                      .matchThen(result -> result ? Response.ok() : Response.err(),
                                 Response::err);
    }
}
