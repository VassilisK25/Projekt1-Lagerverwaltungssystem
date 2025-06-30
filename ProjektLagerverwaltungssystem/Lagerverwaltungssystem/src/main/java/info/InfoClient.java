package info;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class InfoClient {
    public static void main(String[] args) {
        var host = "Benutzer";
        var port = 50000;

        // Eingabe und Ausgabestrom für den ClientSocket
        try (var socket = new Socket(host, port);
             var in = new ObjectInputStream(socket.getInputStream());
             var out = new ObjectOutputStream(socket.getOutputStream())) {

            // Analyse des Input-Streams
            var scanner = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                var id = 0;
                try {
                    // Speichern des nächsten Inputs als Integer
                    id = Integer.parseInt(scanner.next());
                } catch (NumberFormatException e) {
                    continue;
                }

                if (id == 0)
                    break;
                // Aufnahme der ID in Output-Stream für Übertragung
                out.writeInt(id);
                out.flush();
                // Lesen des zugehörigen Artikels aus Input-Stream
                var artikel = (Artikel) in.readObject();
                if (artikel != null)
                    System.out.println(artikel);
                else
                    System.out.println("Artikel " + id + " nicht vorhanden");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}