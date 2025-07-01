package Lagerverwaltungssystem;

import jakarta.json.bind.JsonbBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LagerverwaltungAPP extends Application {
    private String host = "localhost";
    private final int port = 50000;
    private Thread t;

    @Override
    public void start(Stage primaryStage) {

        // Aufruf der Start-Methoden im Client und im Server
        new Thread(() -> new ArtikelServer(port).startServer()).start();
        new Thread(() -> new ArtikelClient(port, host).startClient()).start();
        
        ChoiceBox<Message.Op> opBox = new ChoiceBox<>(FXCollections.observableArrayList(Message.Op.values()));
        opBox.getSelectionModel().select(Message.Op.READ);

        TextField idField    = new TextField(); idField.setPromptText("ID");
        TextField nameField  = new TextField(); nameField.setPromptText("Name");
        TextField priceField = new TextField(); priceField.setPromptText("Preis");
        TextField qtyField   = new TextField(); qtyField.setPromptText("Menge");

        Button executeBtn = new Button("Ausführen");
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);

        // TextArea für Serverinformationen
        TextArea serverKommunikation = new TextArea();
        serverKommunikation.setEditable(false);
        serverKommunikation.setPromptText("Serverinformationen ...");

        // 2) Sichtbarkeit der Felder steuern
        opBox.getSelectionModel().selectedItemProperty().addListener((obs, oldOp, newOp) -> {
            nameField.setDisable(true);
            priceField.setDisable(true);
            qtyField.setDisable(true);

            switch (newOp) {
                case READ, DEL ->          {/* nur ID */}
                case UPDATE, NEW -> {
                    priceField.setDisable(false);
                    if (newOp == Message.Op.NEW) nameField.setDisable(false);
                }
                case ADD, SUB -> qtyField.setDisable(false);
            }
        });

        // Dropdown-Auswahl der durchzuführenden Operationen
        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10);
        form.add(new Label("Operation:"), 0, 0); form.add(opBox,    1, 0);
        form.add(new Label("ID:"),        0, 1); form.add(idField,  1, 1);
        form.add(new Label("Name:"),      0, 2); form.add(nameField,1, 2);
        form.add(new Label("Preis:"),     0, 3); form.add(priceField,1, 3);
        form.add(new Label("Menge:"),     0, 4); form.add(qtyField, 1, 4);
        form.add(executeBtn,              1, 5);

        // erstes TextArea
        VBox root = new VBox(10, form, resultArea, serverKommunikation);
        root.setPadding(new Insets(15));


        // Button-Handler
        executeBtn.setOnAction(e -> {
            try {
                // Extraktion von Operation + Parameter aus den TextFields
                String op       = opBox.getValue().name().toLowerCase();
                String id   = idField.getText().trim();
                String name     = nameField.getText().trim();
                String price = priceField.getText().trim();
                String qty   = qtyField.getText().trim();

                // Baue dein Parameter-Array
                List<String> params = new ArrayList<>();

                // ID wird an dieser Stelle jedem Listenwert übergeben
                params.add(id);
                switch (op) {
                    case "update" -> {
                        if(price.isBlank()){
                            resultArea.setText("Bitte Preis angeben.");
                            return;
                        }
                        params.add(price);        // update braucht ID + Preis
                    }
                    case "new" -> {
                        if(name.isBlank() || price.isBlank()){
                            resultArea.setText("Bitte Name und/ oder Preis angeben.");
                            return;
                        }
                        params.add(name);         // … und den Namen
                        params.add(price);        // new braucht ID + Preis …

                    }
                    case "add", "subtract", "s", "sub" -> {
                        if(id.isBlank() || qty.isBlank()){
                            resultArea.setText("Bitte gültige Menge angeben.");
                            return;
                        }
                        params.add(qty);          // add/subtract braucht ID + Menge
                    }
                    // read und delete brauchen nur ID
                }


                new Thread(() -> {
                    try (Socket sock = new Socket("localhost", 50000);
                         BufferedReader in  = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                         PrintWriter    out = new PrintWriter(sock.getOutputStream(), true)) {

                        String result = ArtikelClient.executeOnce(op,
                                params.toArray(new String[0]), in, out);

                        Platform.runLater(() -> resultArea.setText(result));

                    } catch (IOException ex) {
                        Platform.runLater(() ->
                                resultArea.setText("Kommunikationsfehler: " + ex.getMessage()));
                    }
                }).start();

            } catch (Exception ex) {
                resultArea.setText("Bitte alle erforderlichen Felder korrekt ausfüllen.");
            }
        });


        primaryStage.setScene(new Scene(root, 400, 350));
        primaryStage.setTitle("Lagerverwaltung GUI");
        primaryStage.show();
    }


    private Message deserialize(String data) {
        var jsonb = JsonbBuilder.create();
        return jsonb.fromJson(data, Message.class);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
