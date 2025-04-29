package com.example.citysuggestions.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional; // Untuk elevation dan dem yang bisa null

public class CityData {
    // Nama field Java yang merepresentasikan kolom di file TSV
    // id	name	ascii	alt_name	lat	long	feat_class	feat_code	country	cc2	admin1	admin2	admin3	admin4	population	elevation	dem	tz	modified_at

    private int geonameid; // id (indeks 0)
    private String name; // name (indeks 1)
    private String asciiname; // ascii (indeks 2)
    private List<String> alternateNames; // alt_name (indeks 3, dipisah koma)
    private double latitude; // lat (indeks 4)
    private double longitude; // long (indeks 5)
    private String featureClass; // feat_class (indeks 6)
    private String featureCode; // feat_code (indeks 7)
    private String countryCode; // country (indeks 8)
    private String cc2; // cc2 (indeks 9)
    private String admin1Code; // admin1 (indeks 10)
    private String admin2Code; // admin2 (indeks 11)
    private String admin3Code; // admin3 (indeks 12)
    private String admin4Code; // admin4 (indeks 13)
    private long population; // population (indeks 14)
    private Integer elevation; // elevation (indeks 15, bisa kosong/null)
    private Integer dem; // dem (indeks 16, bisa kosong/null)
    private String timezone; // tz (indeks 17)
    private String modificationDate; // modified_at (indeks 18)

    // Constructor yang menerima array String dari satu baris TSV
    // Ini adalah cara untuk mem-parse data mentah dari file
    public CityData(String[] data) {
        // Pastikan indeks `data` sesuai dengan urutan kolom di file TSV Anda!
        // Urutan kolom di file geonames dump standar adalah seperti yang dijelaskan sebelumnya.

        this.geonameid = Integer.parseInt(data[0]);
        this.name = data[1];
        this.asciiname = data[2];
        // Parsing alternate names: split string di data[3] dengan koma
        if (data[3] != null && !data[3].trim().isEmpty()) {
            this.alternateNames = Arrays.asList(data[3].split(","));
        } else {
            this.alternateNames = List.of(); // List kosong jika tidak ada alt_names
        }
        this.latitude = Double.parseDouble(data[4]);
        this.longitude = Double.parseDouble(data[5]);
        this.featureClass = data[6];
        this.featureCode = data[7];
        this.countryCode = data[8];
        this.cc2 = data[9];
        this.admin1Code = data[10];
        this.admin2Code = data[11];
        this.admin3Code = data[12];
        this.admin4Code = data[13];
        this.population = Long.parseLong(data[14]);

        // Handle kolom elevation (indeks 15) yang mungkin kosong atau non-angka
        if (data[15] != null && !data[15].trim().isEmpty()) {
             try { this.elevation = Integer.parseInt(data[15]); } catch (NumberFormatException e) { this.elevation = null; }
        } else { this.elevation = null; }

        // Handle kolom dem (indeks 16) yang mungkin kosong atau non-angka
         if (data[16] != null && !data[16].trim().isEmpty()) {
             try { this.dem = Integer.parseInt(data[16]); } catch (NumberFormatException e) { this.dem = null; }
         } else { this.dem = null; }

        this.timezone = data[17];
        this.modificationDate = data[18];
    }

    // --- Getters --- (Generate di IDE Anda untuk semua field agar bisa diakses)
    // Ini diperlukan agar data di objek CityData bisa diambil nilainya
    public int getGeonameid() { return geonameid; }
    public String getName() { return name; }
    public String getAsciiname() { return asciiname; }
    public List<String> getAlternateNames() { return alternateNames; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getFeatureClass() { return featureClass; }
    public String getFeatureCode() { return featureCode; }
    public String getCountryCode() { return countryCode; }
    public String getCc2() { return cc2; }
    public String getAdmin1Code() { return admin1Code; }
    public String getAdmin2Code() { return admin2Code; }
    public String getAdmin3Code() { return admin3Code; }
    public String getAdmin4Code() { return admin4Code; }
    public long getPopulation() { return population; }
    public Optional<Integer> getElevation() { return Optional.ofNullable(elevation); } // Menggunakan Optional
    public Optional<Integer> getDem() { return Optional.ofNullable(dem); } // Menggunakan Optional
    public String getTimezone() { return timezone; }
    public String getModificationDate() { return modificationDate; }

    // --- Setters --- (Tidak terlalu perlu jika data hanya di-set via constructor)
    // ...
}