package JobClientSystem;

import jakarta.json.bind.JsonbBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class JobClient {
    private static String host;
    private final static int PORT = 50000;
    private final static int SERVER_PORT = 40000;
    private ServerSocket serverSocket;

    public static void main(String[] args) throws Exception {
        var localHost = "localhost";
        host = "localhost";

        var client = new JobClient();
        client.startReceiver();

        var job1 = new Job();
        job1.setId("Job 1");
        job1.setRequest("Hier steht die Auftragsbeschreibung.");
        job1.setAddr(localHost);
        job1.setPort(SERVER_PORT);

        var job2 = new Job();
        job2.setId("Job 2");
        job2.setRequest("Hier steht die Auftragsbeschreibung.");
        job2.setAddr(localHost);
        job2.setPort(SERVER_PORT);

        var job3 = new Job();
        job3.setId("Job 3");
        job3.setRequest("Hier steht die Auftragsbeschreibung.");
        job3.setAddr(localHost);
        job3.setPort(SERVER_PORT);

        client.process(job1);
        Thread.sleep(3000);
        client.process(job2);
        Thread.sleep(500);
        client.process(job3);

        System.in.read();
        client.serverSocket.close();
    }

    private void process(Job job) {
        try (var socket = new Socket(host, PORT);
             var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(serialize(job));
            System.out.println("Server: " + in.readLine());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void startReceiver() {
        var t = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);

                while (true) {
                    try (var socket = serverSocket.accept();
                         var in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        var data = in.readLine();
                        if (data != null) {
                            var job = deserialize(data);
                            System.out.println(job);
                        }
                    } catch (SocketException e) {
                        break;
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });

        t.start();
    }

    private static String serialize(Job job) {
        var jsonb = JsonbBuilder.create();
        return jsonb.toJson(job);
    }

    private static Job deserialize(String data) {
        var jsonb = JsonbBuilder.create();
        return jsonb.fromJson(data, Job.class);
    }
}
