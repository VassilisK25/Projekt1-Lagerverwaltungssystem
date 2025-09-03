package Lagerverwaltungssystem;

import jakarta.json.bind.JsonbBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArtikelServer {
    private final int port;
    private Map<Integer, Artikel> map;
    private ExecutorService pool;
    // Dateien werden aus Ordner resources bezogen
    String eingabeDatei = Paths.get("ProjektLagerverwaltungssystem/resources/Lagerhaltung.xlsx").toAbsolutePath().toString();

    String logDatei = Paths.get("ProjektLagerverwaltungssystem/resources/Protokolldatei_Lager.xlsx").toAbsolutePath().toString();

    public ArtikelServer(int port) {
        this.port = port;
        load();
    }

    // Server-Start-Routine  -- Socket für Datenempfang und /-versendung wird gesetzt
    public void startServer() {
        try (var serverSocket = new ServerSocket(port)) {
            System.out.println("ArtikelServer gestartet ...");
            pool = Executors.newCachedThreadPool();
            while (true) {
                var socket = serverSocket.accept();
                // Pool wird erzeugt und Handlerobjekt übergeben
                // Ermöglicht es dem Server mehrere asynchrone Aufgaben zu erledigen
                pool.execute(new Handler(socket));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            if (pool != null) pool.shutdown();
        }
    }

    private class Handler implements Runnable {
        private final Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        // Start des eigentlichen Programms
        @Override
        public void run() {
            // Herstellen der Verbindung
            SocketAddress socketAddress = socket.getRemoteSocketAddress();
            System.out.println("Verbindung zu " + socketAddress + " hergestellt");

            // Auslesen der erhaltenen Message
            try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 var out = new PrintWriter(socket.getOutputStream(), true)) {

                String line;
                while ((line = in.readLine()) != null) {
                    var message = deserialize(line);
                    switch (message.op) {
                        case READ -> read(message.artikel.getId(), out);
                        case UPDATE -> update(message.artikel.getId(), message.artikel.getPreis(), out);
                        case NEW -> neu(message.artikel.getId(), message.artikel.getName(), message.artikel.getMenge(), message.artikel.getPreis(), out);
                        case SUB -> entnehmen(message.artikel.getId(), message.artikel.getMenge(), out);
                        case ADD ->  hinzufügen(message.artikel.getId(), message.artikel.getMenge(), out);
                        case DEL -> loeschen(message.artikel.getId(), out);
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                System.out.println("Verbindung zu " + socketAddress + " beendet");
            }
        }

        // durchführen der jeweiligen Operation hierbei wird direkt eine Message erzeugt
        // und serialisiert, die direkt an den Client zurückgesendet wird
        private void read(int id, PrintWriter out) {
            var artikel = map.get(id);
            var message = new Message();
            if (artikel == null) {
                message.info = "Artikel " + id + " nicht vorhanden";
            } else {
                message.artikel = artikel;
            }
            out.println(serialize(message));
        }

        private void update(int id, double preis, PrintWriter out) throws IOException {
            var artikel = map.get(id);
            var message = new Message();
            if (artikel == null) {
                message.info = "Artikel " + id + " nicht vorhanden";
            } else {
                artikel.setPreis(preis);
                message.info = "Artikel " + id + " wurde geändert";
                speichereExcel(eingabeDatei);
                protokollDatei(artikel, "Update", logDatei);

            }
            out.println(serialize(message));
        }

        private void neu(int id, String name, int menge, double preis, PrintWriter out) throws IOException {
            var message = new Message();
            System.out.println("addArtikel() wurde aufgerufen mit: " + id + ", " + name + ", " + preis);

            if(map.containsKey(id)){
                message.info = "Artikel " + id + " existiert bereits.";
            } else{
                Artikel neuerArtikel = new Artikel(id, name, menge, preis);
                map.put(id, neuerArtikel);
                message.info = "Neuer Artikel " + id + " wurde hinzugefügt.";
                speichereExcel(eingabeDatei);
                protokollDatei(map.get(id), "Neuer Artikel", logDatei);
            }
            out.println(serialize(message));
        }
        private void entnehmen (int id, int menge, PrintWriter out) throws IOException {
            var artikel = map.get(id);
            var message = new Message();
            if (artikel == null) {
                message.info = "Artikel " + id + " nicht vorhanden";
            } else {
                if (artikel.getMenge() < menge){
                    message.info = "Es wird die maximale Menge in Höhe von " + artikel.getMenge() + " ausgegeben.";
                    artikel.setMenge(0);
                    speichereExcel(eingabeDatei);
                } else if(artikel.getMenge() == 0){
                    message.info = "Bestand von Artikel " + id + " ist leer.";
                }else{
                    artikel.setMenge(artikel.getMenge() - menge);
                    message.info = "Artikel " + id + " wurde geändert";
                    message.info = "Restmenge von " + id + " beträgt " + artikel.getMenge();
                    speichereExcel(eingabeDatei);
                    protokollDatei(artikel, "Entnahme", logDatei);

                }
            }
            out.println(serialize(message));
        }
        private void hinzufügen (int id, int menge, PrintWriter out) throws IOException {
            var artikel = map.get(id);
            var message = new Message();
            if (artikel == null) {
                message.info = "Artikel " + id + " nicht vorhanden";
            } else{
                message.info = "Artikel " + id + " Menge alt: " + artikel.getMenge();
                artikel.setMenge(artikel.getMenge() + menge);
                message.info = "Artikel " + id + " wurde geändert.";
                message.info = "Artikel " + id + " Menge neu: " + artikel.getMenge();
                speichereExcel(eingabeDatei);
                protokollDatei(artikel, "Hinzufügen", logDatei);
            }
            out.println(serialize(message));
        }
        private void loeschen(int id, PrintWriter out) throws IOException {
            var artikel = map.get(id);
            var message = new Message();
            if (artikel == null) {
                message.info = "Artikel " + id + " nicht vorhanden";
            } else{
                map.remove(id);
                message.info = "Artikel " + id + " wurde erfolgreich gelöscht.";
                speichereExcel(eingabeDatei);
                protokollDatei(artikel, "Löschung", logDatei);
            }
            out.println(serialize(message));
        }

    }
    // load-Methode wird beim Start des Servers aufgerufen und die
    // Informationen der referenzierten Excel-Datei ausgelesen
    private void load() {
        map = new HashMap<>();

        // im InputStream werden die Daten ausgelesen
        // dabei wird ein Objekt vom Typ Workbook erzeugt, dass speziell zum Arbeiten
        // mit Excel-Formaten verwendet wird
        try (FileInputStream fis = new FileInputStream(eingabeDatei);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Durchlauf der Excel-Sheet, wobei die Kopfzeile übersprungen wird
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                Cell idCell = row.getCell(0);
                Cell nameCell = row.getCell(1);
                Cell preisCell = row.getCell(2);
                Cell mengeCell = row.getCell(3);

                // ist eines der notwendigen Felder leer, wird die Zeile überprungen
                if (idCell == null || nameCell == null || preisCell == null|| mengeCell == null) continue;

                int id = (int) idCell.getNumericCellValue();
                String name = nameCell.getStringCellValue();
                int menge = (int) mengeCell.getNumericCellValue();
                double preis = preisCell.getNumericCellValue();

                // Artikel-Informationen werden innerhalb einer Map-Struktur gespeichert
                map.put(id, new Artikel(id, name, menge, preis));
            }

            System.out.println("Artikel aus Excel geladen: " + map.size());


        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Excel-Datei: " + e.getMessage());
        }
    }

    // Methode um Änderungen in der Excel persistent zu speichern
    private void speichereExcel(String dateiname) {
        System.out.println("Excel wird gespeichert.");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Artikel");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Bezeichnung");
            header.createCell(2).setCellValue("Preis");
            header.createCell(3).setCellValue("Menge");

            int zeile = 1;
            for (Artikel artikel : map.values()) {
                Row row = sheet.createRow(zeile++);
                row.createCell(0).setCellValue(artikel.getId());
                row.createCell(1).setCellValue(artikel.getName());
                row.createCell(2).setCellValue(artikel.getPreis());
                row.createCell((3)).setCellValue(artikel.getMenge());
            }

            try (FileOutputStream fos = new FileOutputStream(dateiname)) {
                workbook.write(fos);
                System.out.println("Excel gespeichert: " + dateiname);
            }

        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Excel-Datei: " + e.getMessage());
        }
    }

    // Methode um sämtliche Aktivitäten zu protokollieren
    private void protokollDatei(Artikel artikel, String aktion, String logDatei) throws IOException {
        System.out.println("Änderungen werden protokolliert.");
        try {
            Workbook workbook;
            Sheet sheet;
            File file = new File(logDatei);

            if(file.exists()){
                try (FileInputStream fis = new FileInputStream(file)){
                    workbook = new XSSFWorkbook(fis);
                }
            } else {
                workbook = new XSSFWorkbook();
            }

            sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                sheet = workbook.createSheet("Protokoll");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Zeitstempel");
                header.createCell(1).setCellValue("Artikel-ID");
                header.createCell(2).setCellValue("Aktion");
            }
            int neueZeile = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(neueZeile);

            String zeitstempel = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            row.createCell(0).setCellValue(zeitstempel);
            row.createCell(1).setCellValue(artikel.getId());
            row.createCell(2).setCellValue(aktion);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            workbook.close();

        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Protokoll-Datei: " + e.getMessage());
        }
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
