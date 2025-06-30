package JobClientSystem;

import jakarta.json.bind.JsonbBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

// Eigener Thread für die eigentliche Bearbeitung des Auftrags (Jobs)
public class Worker extends Thread {
    private final Job job;

    public Worker(Job job) {
        this.job = job;
    }

    @Override
    public void run() {
        System.out.println(job.getId() + " wird bearbeitet.");
        var start = Instant.now();

        try {
            var random = new Random();
            Thread.sleep(8000 + random.nextInt(8000));
        } catch (InterruptedException ignored) {
        }

        var end = Instant.now();
        var millis = Duration.between(start, end).toMillis();
        job.setResponse("Hier steht das Ergebnis.");
        job.setDuration(millis);

        System.out.println(job.getId() + " ist fertig.");

        try (var socket = new Socket(job.getAddr(), job.getPort());
             var out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(serialize(job));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static String serialize(Job job) {
        var jsonb = JsonbBuilder.create();
        return jsonb.toJson(job);
    }
}

