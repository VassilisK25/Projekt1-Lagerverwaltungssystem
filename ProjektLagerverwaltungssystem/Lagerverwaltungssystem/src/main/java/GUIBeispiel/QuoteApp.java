package GUIBeispiel;

// JavaFX Bibliotheken
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class QuoteApp extends Application {
    private String host = "localhost";
    private final int port = 50000;
    // Textbereich
    private TextArea ta;
    // Textfeld
    private TextField tf;
    // Threads = Auszuführende Elemente oder Jobs
    private Thread t;

    @Override
    public void start(Stage stage) {
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
        stage.setTitle("QuoteApp");
        stage.show();

        // Variable instanziiert, die überprüft, ob Inhalte übergeben werden
        var params = getParameters().getRaw();
        if (!params.isEmpty())
            host = params.get(0);
        // setzen eines neuen Thread-Objekts
        t = new Thread(() -> {
            try {
                // QuoteClient-Objekt erzeugt, dem host und port übergeben werden
                // der Client erzeugt ein Datagram, dass dem Server übermittelt wird
                var client = new QuoteClient(host, port);

                while (true) {
                    var quote = client.getQuote();
                    Platform.runLater(() -> {
                        ta.setText(quote.getText());
                        tf.setText(quote.getAuthor());
                    });
                    Thread.sleep(8000);
                }
            } catch (Exception e) {
                Platform.runLater(() -> ta.setText(e.toString()));
            }
        });

        t.start();
    }

    @Override
    public void stop() {
        t.interrupt();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
