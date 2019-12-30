package net.noneuclideangirl.requests;

import net.noneuclideangirl.ServiceMonitor;
import net.noneuclideangirl.data.ServiceDescriptor;
import net.noneuclideangirl.net.AbstractRequestHandler;
import net.noneuclideangirl.net.Response;
import net.noneuclideangirl.DatabaseManager;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ListServicesRequestHandler extends AbstractRequestHandler {
    public static final String COMMAND_NAME = "LIST_SERVICES";

    public ListServicesRequestHandler() {
        super(COMMAND_NAME);
    }

    @Override
    protected Document process(Document doc) {
        var serviceIterable = DatabaseManager.get().findAllServices();
        List<Document> services = new ArrayList<>();
        for (var service : serviceIterable) {
            var descriptor = ServiceDescriptor.fromDoc(service).unwrap();
            services.add(descriptor.toDoc()
                                   .append("active", ServiceMonitor.isActive(descriptor)));
        }

        return Response.ok()
                .append("services", services);
    }
}
