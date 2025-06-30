package info;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

// Iterativer Server - es kann stets nur ein Client gleichzeitig bedient werden
public class InfoServer1 {
    private final int port;
    // Die Informationsobjekte
    private Map<Integer, Artikel> map;

    public InfoServer1(int port) {
        this.port = port;
        // Im Konstruktor werden gleichzeitig die Informationsobjekte geladen
        load();
    }

    public void startServer() {
        try (var serverSocket = new ServerSocket(port)) {
            System.out.println("InfoServer1 gestartet ...");
            // Interne Servermethode die zu Beginn gestartet wird
            // Server-Socket wird als Parameter übergeben
            process(serverSocket);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void process(ServerSocket serverSocket) throws IOException {
        while (true) {
            SocketAddress socketAddress = null;

            try (var socket = serverSocket.accept();
                 // Eingabe und Ausgabestrom für den Serversocket
                 var out = new ObjectOutputStream(socket.getOutputStream());
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
        new InfoServer1(50000).startServer();
    }
}