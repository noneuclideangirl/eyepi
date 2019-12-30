package net.noneuclideangirl.net;

import org.bson.Document;

public abstract class AbstractRequestHandler {
    private final String commandName;

    public AbstractRequestHandler(String commandName) {
        this.commandName = commandName;
    }

    String getCommandName() {
        return commandName;
    }

    protected abstract Document process(Document doc);
}
