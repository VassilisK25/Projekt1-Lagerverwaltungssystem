package tcpframework;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public abstract class AbstractHandler {
    public void handle(final Socket socket, ExecutorService pool) {
        pool.execute(() -> {
            var socketAddress = socket.getRemoteSocketAddress();
            System.out.println("Verbindung zu " + socketAddress + " hergestellt");

            runTask(socket);

            try {
                socket.close();
            } catch (IOException ignored) {
            }

            System.out.println("Verbindung zu " + socketAddress + " beendet");
        });
    }

    public abstract void runTask(Socket socket);
}
