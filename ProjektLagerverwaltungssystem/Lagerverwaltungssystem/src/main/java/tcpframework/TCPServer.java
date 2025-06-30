package tcpframework;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer extends Thread {
    private final AbstractHandler handler;
    private final ServerSocket serverSocket;
    private final ExecutorService pool;

    // Konstruktor mit Portnummer, Handlerklasse (als Subklasse des AbstractHandlers)
    public TCPServer(int port, Class<?> handlerClass) throws Exception {
        // erzeugt ServerSocket und Thread-Pool
        handler = (AbstractHandler) handlerClass.getDeclaredConstructor().newInstance();
        serverSocket = new ServerSocket(port);
        pool = Executors.newCachedThreadPool();
    }

    // oder mit Handler-Instanz
    public TCPServer(int port, AbstractHandler handlerObject) throws IOException {
        // erzeugt ServerSocket und Thread-Pool
        handler = handlerObject;
        serverSocket = new ServerSocket(port);
        pool = Executors.newCachedThreadPool();
    }

    public void run() {
        try {
            while (true) {
                // Erstellen und Akzeptieren der Socketverbindung
                var socket = serverSocket.accept();
                // übergabe der Parameter an Handler-Instanz
                handler.handle(socket, pool);
            }
        } catch (SocketException ignored) {
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    // Beim Aufruf von stopServer() wird Socket geschlossen und ThreadPool terminiert
    public void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
        pool.shutdown();
    }
}

