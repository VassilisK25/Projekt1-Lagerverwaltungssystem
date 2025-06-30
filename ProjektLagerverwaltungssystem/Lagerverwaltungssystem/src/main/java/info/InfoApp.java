package info;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InfoApp extends Application{
    private String host = "localhost";
    private final int port = 50000;
    // Textbereich
    private TextArea ta;
    // Textfeld
    private TextField tf;
    // Threads = Auszuführende Elemente oder Jobs
    private Thread t;

    @Override
    public void start(Stage stage) throws Exception {
        // Programmelemente aus JavaFX
        // box ist Elternelement, der Rest sind Kindelemente
        var box = new VBox();
        ta = new TextArea();
        tf = new TextField();
        box.getChildren().addAll(ta, tf);

        // Design des Textfeldes
        ta.setWrapText(false);
        ta.setStyle("-fx-font: 20pt \"Arial\";-fx-text-fill: red;");
        ta.setEditable(false);

        // Design des Textblocks
        tf.setStyle("-fx-font: italic 16pt \"Arial\";-fx-text-fill: green;");
        tf.setEditable(false);

        stage.setScene(new Scene(box, 700, 200));
        stage.setTitle("InfoApp");
        stage.show();
    }
}
