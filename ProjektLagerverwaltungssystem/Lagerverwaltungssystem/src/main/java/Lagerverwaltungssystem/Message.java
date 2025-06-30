package Lagerverwaltungssystem;

public class Message {
    public enum Op {
        READ, UPDATE, NEW, SUB, ADD, DEL
    }

    // auszuführende Operation
    public Op op;
    // Information über erfolgreiche/ nicht erfolgreiche Änderung
    public String info;
    // Referenz von Artikel
    public Artikel artikel;
}

