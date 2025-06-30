package GUIBeispiel;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class QuoteClient {
    private static final int BUFSIZE = 508;
    private final int port;
    private final InetAddress addr;

    // Konstruktor
    public QuoteClient(String host, int port) throws UnknownHostException {
        this.port = port;
        addr = InetAddress.getByName(host);
    }

    //erzeugt ein Datagram, dass dem Server übermittelt wird
    // außerdem wird ein Datagram empfangen
    public Quote getQuote() throws Exception {
        var socket = new DatagramSocket();
        socket.setSoTimeout(10000);

        var packetOut = new DatagramPacket(new byte[1], 1, addr, port);
        socket.send(packetOut);

        var packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
        socket.receive(packetIn);
        socket.close();

        var json = new String(packetIn.getData(), 0, packetIn.getLength());
        return deserialize(json);
    }

    // empfangene Datagram wird deserialisiert und in ein Quote-Objekt übertragen
    private static Quote deserialize(String data) {
        Jsonb jsonb = JsonbBuilder.create();
        return jsonb.fromJson(data, Quote.class);
    }

    public static void main(String[] args) throws Exception {
        var host = "Benutzer";
        int port = 50000;

        var client = new QuoteClient(host, port);
        var quote = client.getQuote();
        System.out.println(quote.getText());
        System.out.println(quote.getAuthor());
    }
}

