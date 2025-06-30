package JobClientSystem;

import tcpframework.TCPServer;

public class JobServer {
    public static void main(String[] args) throws Exception {
        var port = 50000;

        var server = new TCPServer(port, JobHandler.class);
        server.start();

        System.out.println("Stoppen mit ENTER");
        System.in.read();
        server.stopServer();
    }
}