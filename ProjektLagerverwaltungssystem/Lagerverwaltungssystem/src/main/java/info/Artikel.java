package info;

import java.io.Serializable;

public class Artikel implements Serializable {
    private int id;
    private String name;
    private double preis;

    public Artikel() {
    }

    public Artikel(int id, String name, double preis) {
        this.id = id;
        this.name = name;
        this.preis = preis;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPreis() {
        return preis;
    }

    public void setPreis(double preis) {
        this.preis = preis;
    }

    @Override
    public String toString() {
        return "Artikel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", preis=" + preis +
                '}';
    }
}