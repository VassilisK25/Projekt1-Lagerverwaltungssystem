package JobClientSystem;

import jakarta.json.bind.JsonbBuilder;
import tcpframework.AbstractHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class JobHandler extends AbstractHandler {
    @Override
    public void runTask(Socket socket) {
        try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var out = new PrintWriter(socket.getOutputStream(), true)) {

            var data = in.readLine();
            if (data != null) {
                var job = deserialize(data);
                out.println(job.getId() + " erhalten.");
                new Worker(job).start();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static Job deserialize(String data) {
        var jsonb = JsonbBuilder.create();
        return jsonb.fromJson(data, Job.class);
    }
}
