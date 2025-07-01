package Lagerverwaltungssystem;

public class Artikel {
    private int id;
    private String name;
    private int menge;
    private double preis;

    public Artikel() {
    }

    public Artikel(int id, String name, int menge, double preis) {
        this.id = id;
        this.name = name;
        this.menge = menge;
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

    public int getMenge() {
        return menge;
    }

    public void setMenge(int menge) {
        this.menge = menge;
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
                ", menge=" + menge +
                ", preis=" + preis +
                '}';
    }
}

