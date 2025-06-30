package GUIBeispiel;

import jakarta.json.bind.JsonbBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuoteServer {
    private static final int BUFSIZE = 508;
    // Bezieht die Zitate aus der Textdatei quotes.txt
    private static final String FILE = "C:\\Users\\vassi\\Documents\\Projekt_CS_Programmierung\\MKJava-main\\MKJava-main\\Begleitmaterial-MKJava6\\03_UDP\\Aufgaben\\quotes.txt";

    public static void main(String[] args) throws Exception {
        int port = 50000;

        try (var socket = new DatagramSocket(port)) {
            var quotes = loadQuotes();
            var numberOfQuotes = quotes.size();

            System.out.println("Anzahl Zitate: " + numberOfQuotes);

            var packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
            var packetOut = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
            var random = new Random();

            while (true) {
                socket.receive(packetIn);
                System.out.print(".");

                var idx = random.nextInt(numberOfQuotes);
                var data = serialize(quotes.get(idx)).getBytes();

                packetOut.setData(data);
                packetOut.setLength(data.length);
                packetOut.setSocketAddress(packetIn.getSocketAddress());

                socket.send(packetOut);
            }
        }
    }

    private static List<Quote> loadQuotes() throws IOException {
        List<Quote> quotes = new ArrayList<>();
        try (var in = new BufferedReader(new FileReader(FILE))) {
            var line = "";
            while ((line = in.readLine()) != null) {
                var parts = line.split("#");
                if (parts.length == 2) {
                    var quote = new Quote(parts[0], parts[1]);
                    quotes.add(quote);
                }
            }
        }
        return quotes;
    }

    private static String serialize(Quote quote) {
        var jsonb = JsonbBuilder.create();
        return jsonb.toJson(quote);
    }
}

