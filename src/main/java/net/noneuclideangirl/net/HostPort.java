package net.noneuclideangirl.net;

import java.net.Socket;

public class HostPort {
    public final String name;
    public final String ip;
    public final int port;
    public final String fullAddress;

    public HostPort(Socket socket) {
        name = socket.getInetAddress().getHostName();
        ip = socket.getInetAddress().getHostAddress();
        port = socket.getPort();
        fullAddress = name + ":" + port;
    }
}
