package info;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

// Paralleler Server
public class InfoServer2 {
    private final int port;
    private Map<Integer, Artikel> map;

    public InfoServer2(int port) {
        this.port = port;
        load();
    }

    public void startServer() {
        try (var serverSocket = new ServerSocket(port)) {
            System.out.println("InfoServer2 gestartet ...");
            // unterschied zum Server1 --> neuer Thread mit
            // Server-Handler ermöglicht mehrere Client-Anfragen
            while (true) {
                var socket = serverSocket.accept();
                new Thread(new Handler(socket)).start();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    // Jede CLient-Anfrage wird quasi in einer eigenen
    // Server-Handler-Klasse bearbeitet
    private class Handler implements Runnable {
        private final Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        // Methode run() innerhalb des Server-Handlers
        // fungiert wie process() in InfoServer1
        @Override
        public void run() {
            SocketAddress socketAddress = null;

            // Eingabe und Ausgabestrom für den Serversocket
            try (var out = new ObjectOutputStream(socket.getOutputStream());
                 var in = new ObjectInputStream(socket.getInputStream())) {

                // Adresse des ClientSocket speichern
                socketAddress = socket.getRemoteSocketAddress();
                System.out.println("Verbindung zu " + socketAddress + " hergestellt");

                // gepufferte Daten sofort ausgeben oder speichern
                out.flush();

                // Überprüfung und Suche nach der eingehenden ID
                while (true) {
                    var id = in.readInt();
                    var artikel = map.get(id);
                    // wenn gefunden wird der Artikel in den Outputstream gespeichert und gesendet
                    out.writeObject(artikel);
                    out.flush();
                }
            } catch (EOFException ignored) {
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                System.out.println("Verbindung zu " + socketAddress + " beendet");
            }
        }
    }

    private void load() {
        map = new HashMap<>();
        map.put(4711, new Artikel(4711, "Hammer", 2.99));
        map.put(4712, new Artikel(4712, "Zange", 3.99));
        map.put(4713, new Artikel(4713, "Schraubendreher", 1.50));
    }

    public static void main(String[] args) {
        new InfoServer2(50000).startServer();
    }
}
