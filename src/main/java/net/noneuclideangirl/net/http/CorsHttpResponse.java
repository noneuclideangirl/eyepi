package net.noneuclideangirl.net.http;

import org.bson.Document;

class CorsHttpResponse extends HttpResponse {
    @Override
    public String marshall() {
        return "HTTP/1.1 200 OK\r\n"
             + "Access-Control-Allow-Origin: *\r\n"
             + "Access-Control-Allow-Methods: POST\r\n"
             + "Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept\r\n"
             + "Content-Length: 0\r\n"
             + "\r\n"
             + "\n";

    }

    @Override
    public Document toDoc() {
        return new Document();
    }
}
