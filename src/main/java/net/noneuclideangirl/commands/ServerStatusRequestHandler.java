package net.noneuclideangirl.commands;

import net.noneuclideangirl.ServiceMonitor;
import net.noneuclideangirl.net.Response;
import net.noneuclideangirl.net.AbstractRequestHandler;
import org.bson.Document;

public class ServerStatusRequestHandler extends AbstractRequestHandler {
    public static final String COMMAND_NAME = "SERVER_STATUS";

    public ServerStatusRequestHandler() {
        super(COMMAND_NAME);
    }

    @Override
    protected Document process(Document doc) {
        return Response.ok()
                .append("service_count", ServiceMonitor.getActiveServiceCount());
    }
}
