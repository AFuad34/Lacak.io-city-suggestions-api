package com.example.citysuggestions.model;

// Kelas ini merepresentasikan format setiap saran kota di output JSON
public class CitySuggestion {
    private String name;
    private String latitude; // Menggunakan String sesuai contoh output tantangan
    private String longitude; // Menggunakan String sesuai contoh output tantangan
    private double score;

    // Constructor
    public CitySuggestion(String name, double latitude, double longitude, double score) {
        this.name = name;
        this.latitude = String.valueOf(latitude); // Konversi double ke String
        this.longitude = String.valueOf(longitude); // Konversi double ke String
        this.score = score;
    }

    // --- Getters --- (Generate di IDE untuk semua field agar bisa di-serialize ke JSON)
    public String getName() { return name; }
    public String getLatitude() { return latitude; }
    public String getLongitude() { return longitude; }
    public double getScore() { return score; }

    // --- Setters --- (Tidak perlu jika objek bersifat immutable setelah dibuat)
    // public void setName(String name) { this.name = name; }
    // ...
}