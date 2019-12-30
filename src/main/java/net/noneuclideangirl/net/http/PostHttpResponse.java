package net.noneuclideangirl.net.http;

import org.bson.Document;

class PostHttpResponse extends HttpResponse {
    private final Document commandResponse;

    PostHttpResponse(Document commandResponse) {
        this.commandResponse = commandResponse;
    }

    @Override
    public String marshall() {
        String json = commandResponse.toJson();

        return "HTTP/1.1 200 OK\r\n"
              + "Access-Control-Allow-Origin: *\r\n"
              + "Content-Type: application/json\r\n"
              + "Content-Length: " + json.length() + "\r\n"
              + "\r\n"
              + json
              + "\n";
    }

    @Override
    public Document toDoc() {
        return commandResponse;
    }
}
