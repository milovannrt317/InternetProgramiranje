package models;

import java.util.ArrayList;

public class Artikal {
    
    private int id_artikla;
    private String path;
    private String kategorija;
    private String naziv;
    private double cena;
    private int zalihe;

    public Artikal(int id, String path, String kategorija, String naziv, double cena, int zalihe) {
        this.id_artikla = id;
        this.path = path;
        this.kategorija = kategorija;
        this.naziv = naziv;
        this.cena = cena;
        this.zalihe = zalihe;
    }
    public static int noviID(ArrayList<Artikal> artikli)
    {
        int idN=artikli.size();
        while(sadrzi(artikli,idN))
        {
            idN++;
        }
        return idN;
    }
    private static boolean sadrzi(ArrayList<Artikal> artikli,int id)
    {
       return artikli.stream().anyMatch(artikal -> artikal.id_artikla==id);
    }
    public int getId_artikla() {
        return id_artikla;
    }

    public void setId_artikla(int id_artikla) {
        this.id_artikla = id_artikla;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getKategorija() {
        return kategorija;
    }

    public void setKategorija(String kategorija) {
        this.kategorija = kategorija;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public double getCena() {
        return cena;
    }

    public void setCena(double cena) {
        this.cena = cena;
    }

    public int getZalihe() {
        return zalihe;
    }

    public void setZalihe(int zalihe) {
        this.zalihe = zalihe;
    }
}
