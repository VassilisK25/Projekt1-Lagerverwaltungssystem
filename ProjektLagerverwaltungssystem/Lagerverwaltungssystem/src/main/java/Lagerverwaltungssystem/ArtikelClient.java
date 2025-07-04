package Lagerverwaltungssystem;

import jakarta.json.bind.JsonbBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ArtikelClient {
    private final int port;
    private final String host;

    public ArtikelClient(int port, String host) {
        this.port = port;
        this.host = host;
    }
    
    public void startClient(String host, int port) {
        try (var socket = new Socket(host, port);
             var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var out = new PrintWriter(socket.getOutputStream(), true);
             var scanner = new Scanner(System.in)) {


        } catch (IOException e) {
            System.err.println("Verbindungsfehler: " + e.getMessage());
        }
    }

    // Methode, um Informationen der Text-Area zu übergeben
    // Parameter sind: die Message-Informationen, ein BufferedReader zum lesen des InputStreams
    //                 
    public static String executeOnce(String op,
                                     String[] parameters,
                                     BufferedReader in,
                                     PrintWriter out) throws IOException {
        // Ausgewählte Operation durchführen
        boolean success = benutzerAktionen(op, parameters, out);
        if (!success) {
            return "Ungültige Operation oder falsche Parameter.";
        }

        // Antwort einlesen und deserialisieren
        var raw = in.readLine();
        var message = deserialize(raw);
        // Informationen in StringFormat übertragen
        return message.info != null ? message.info : message.artikel.toString();
    }

    // Methode, die die möglichen Operationen verwaltet
    public static boolean benutzerAktionen(String op, String[] args, PrintWriter out) {
        try {
            switch (op) {
                case "r", "read" -> read(Integer.parseInt(args[0]), out);
                case "u", "update" -> update(Integer.parseInt(args[0]), Double.parseDouble(args[1]), out);
                case "n", "neu", "new" -> neu(Integer.parseInt(args[0]), args[1], Double.parseDouble(args[2]), out);
                case "s", "subtract", "sub"-> entnehmen(Integer.parseInt(args[0]), Integer.parseInt(args[1]), out);
                case "a", "add" -> hinzufuegen(Integer.parseInt(args[0]), Integer.parseInt(args[1]), out);
                case "d", "delete", "del" -> loeschen(Integer.parseInt(args[0]), out);
                default -> { return false; }
            }
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Ungültige Eingabe. Bitte erneut versuchen.");
            return false;
        }
    }

    // erstellen der Operations-Nachrichten zur Übermittlung an den Server
    // read, update, neu, entnehmen, hinzufügen, löschen
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
        // erstellen eines Objektes der Klasse Message die einige Informationen
        // für die Datenübertragung beinhaltet
        var message = new Message();
        // Auswahl einer Enumeration in Message
        message.op = Message.Op.NEW;
        // erstellen eines Objektes der Klasse Artikel, mit einigen
        // Informationen für die Datenübertragung
        var artikel = new Artikel();
        artikel.setId(id);
        artikel.setName(name);
        artikel.setPreis(preis);
        // Artikel wird der Klasse Message zugeordnet, bevor Message zu
        // JSON-Übertragungsformat serialisiert wird
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
    private static void hinzufuegen(int id, int menge, PrintWriter out){
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
    
    // Serialisierung der zu übermittelnden Informationen in JSON-Format
    private static String serialize(Message message) {
        var jsonb = JsonbBuilder.create();
        return jsonb.toJson(message);
    }
    
    // Deserialisierung der Informationen aus erhaltenem JSON-Format
    private static Message deserialize(String data) {
        var jsonb = JsonbBuilder.create();
        return jsonb.fromJson(data, Message.class);
    }
}
