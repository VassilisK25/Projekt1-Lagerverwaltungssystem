package Lagerverwaltungssystem;

import jakarta.json.bind.JsonbBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ArtikelClient {
    public static void main(String[] args) {
        new ArtikelClient().startClient("localhost", 50000);
    }
    private void startClient(String host, int port) {
        try (var socket = new Socket(host, port);
             var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var out = new PrintWriter(socket.getOutputStream(), true);
             var scanner = new Scanner(System.in)) {


        } catch (IOException e) {
            System.err.println("Verbindungsfehler: " + e.getMessage());
        }
    }

/*
    public static void clientAusführen(Scanner scanner, BufferedReader in, PrintWriter out) throws IOException {
        while (true) {
            System.out.print("Read (r), Update (u), New (n), Exit (e), Subtract (s), Add (a), DELETE (d) > ");
            var op = scanner.next();
            if (op.equals("e")) break;

            boolean success = benutzerAktionen(op, scanner, out);
            if (!success) continue;

            var message = deserialize(in.readLine());
            System.out.println(message.info != null ? message.info : message.artikel);
        }
    }
*/

    public static String executeOnce(String op,
                                     String[] parameters,
                                     BufferedReader in,
                                     PrintWriter out) throws IOException {
        // 1) Operation durchführen
        boolean success = benutzerAktionen(op, parameters, out);
        if (!success) {


            // ++++++++++ Hier ist aktuell der Fehler zu finden  +++++++++++++
            return "Ungültige Operation oder falsche Parameter.";
        }

        // 2) Antwort einlesen
        var raw = in.readLine();
        var message = deserialize(raw);
        return message.info != null ? message.info : message.artikel.toString();
    }



    public static boolean benutzerAktionen(String op, String[] args, PrintWriter out) {
        try {
            switch (op) {
                case "r", "read" -> read(Integer.parseInt(args[0]), out);
                case "u", "update" -> update(Integer.parseInt(args[0]), Double.parseDouble(args[1]), out);
                case "n", "neu", "new" -> neu(Integer.parseInt(args[0]), args[1], Double.parseDouble(args[2]), out);
                case "s", "subtract", "sub"-> entnehmen(Integer.parseInt(args[0]), Integer.parseInt(args[1]), out);
                case "a", "add" -> hinzufügen(Integer.parseInt(args[0]), Integer.parseInt(args[1]), out);
                case "d", "delete", "del" -> loeschen(Integer.parseInt(args[0]), out);
                default -> { return false; }
            }
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Ungültige Eingabe. Bitte erneut versuchen.");
            return false;
        }
    }

    public static void read(int id, PrintWriter out) {
        var message = new Message();
        message.op = Message.Op.READ;
        var artikel = new Artikel();
        artikel.setId(id);
        message.artikel = artikel;
        out.println(serialize(message));
    }

    private static void update(int id, double preis, PrintWriter out) {
        var message = new Message();
        message.op = Message.Op.UPDATE;
        // Auswahl der ID des Artikels und Änderung des Preises
        var artikel = new Artikel();
        artikel.setId(id);
        artikel.setPreis(preis);
        message.artikel = artikel;
        out.println(serialize(message));
    }

    private static void neu(int id, String name, double preis, PrintWriter out){
        var message = new Message();
        message.op = Message.Op.NEW;

        var artikel = new Artikel();
        artikel.setId(id);
        artikel.setName(name);
        artikel.setPreis(preis);
        message.artikel = artikel;
        out.println((serialize(message)));
    }
    private static void entnehmen(int id, int menge, PrintWriter out){
        var message = new Message();
        message.op = Message.Op.SUB;
        var artikel = new Artikel();
        artikel.setId(id);
        artikel.setMenge(menge);
        message.artikel = artikel;
        out.println((serialize(message)));
    }
    private static void hinzufügen(int id, int menge, PrintWriter out){
        var message = new Message();
        message.op = Message.Op.ADD;
        var artikel = new Artikel();
        artikel.setId(id);
        artikel.setMenge(menge);
        message.artikel = artikel;
        out.println((serialize(message)));
    }
    private static void loeschen(int id, PrintWriter out){
        var message = new Message();
        message.op = Message.Op.DEL;
        var artikel = new Artikel();
        artikel.setId(id);
        message.artikel = artikel;
        out.println((serialize(message)));
    }

    private static String serialize(Message message) {
        var jsonb = JsonbBuilder.create();
        return jsonb.toJson(message);
    }

    private static Message deserialize(String data) {
        var jsonb = JsonbBuilder.create();
        return jsonb.fromJson(data, Message.class);
    }
}
