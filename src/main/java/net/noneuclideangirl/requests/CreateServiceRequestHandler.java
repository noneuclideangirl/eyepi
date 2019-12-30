package net.noneuclideangirl.requests;

import net.noneuclideangirl.data.ServiceDescriptor;
import net.noneuclideangirl.functional.Functional;
import net.noneuclideangirl.net.AbstractRequestHandler;
import net.noneuclideangirl.net.Response;
import net.noneuclideangirl.DatabaseManager;
import org.bson.Document;

public class CreateServiceRequestHandler extends AbstractRequestHandler {
    public static final String COMMAND_NAME = "CREATE_SERVICE";

    public CreateServiceRequestHandler() {
        super(COMMAND_NAME);
    }

    @Override
    protected Document process(Document doc) {
        return ServiceDescriptor.fromDoc(doc).matchThen(desc -> {
            DatabaseManager.get().createService(desc);
            return Response.ok();
        }, Functional.supply(Response.err()));
    }
}
