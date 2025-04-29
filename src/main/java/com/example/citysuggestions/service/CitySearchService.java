package com.example.citysuggestions.service;

import com.example.citysuggestions.model.CityData;
import com.example.citysuggestions.model.CitySuggestion;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.stream.Collectors;
import java.util.Collections; // Import Collections for empty list


// --- Potensi impor library untuk struktur data efisien (contoh, jika pakai Trie) ---
// import com.your_chosen_library.Trie; // Jika menggunakan library Trie


@Service // Menandakan kelas ini adalah Service (komponen Spring)
public class CitySearchService {

    // Injeksi resource file data dari src/main/resources
    // PASTIKAN NAMA FILE DI @Value SESUAI DENGAN NAMA FILE TSV ANDA DI src/main/resources
    @Value("classpath:cities_canada-usa.tsv") // <-- NAMA FILE TSV SUDAH DIPERBARUI DI SINI
    private Resource cityDataFile;

    // Struktur data untuk menyimpan data kota yang sudah dimuat dari file (sementara)
    // List ini hanya penampung saat memuat data.
    // Untuk PENCARIAN CEPAT, Anda PERLU membangun struktur data indeks yang efisien!
    private List<CityData> allCitiesRaw = new ArrayList<>();


     // *** PENTING: Variabel untuk STRUKTUR DATA PENCARIAN EFISIEN Anda ***
     // INI ADALAH BAGIAN KRITIS YANG HARUS ANDA IMPLEMENTASIKAN SECARA EFISIEN
     // GANTI List ini dengan struktur data yang memungkinkan pencarian super cepat (misalnya Trie, Inverted Index, dll.)
     // Data dari allCitiesRaw akan diproses dan disimpan ke dalam struktur ini di buildSearchIndex().
     private List<CityData> citySearchIndex = Collections.emptyList(); // Placeholder: GANTI DENGAN STRUKTUR DATA EFISIEN ANDA


    // Metode ini akan dijalankan otomatis oleh Spring setelah objek CitySearchService dibuat.
    // Digunakan untuk memuat data dari file TSV saat aplikasi dimulai.
    @PostConstruct
    public void loadCityData() {
        System.out.println("Memuat data kota dari file: " + cityDataFile.getFilename() + "...");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(cityDataFile.getInputStream()))) {
            String line;
            int countLoaded = 0; // Menghitung data yang berhasil dimuat (setelah filter)
            int countSkipped = 0; // Menghitung baris yang dilewati (header, error, tidak relevan)
            boolean isHeader = true; // Asumsi baris pertama adalah header

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    // Lewati baris header jika ada
                    isHeader = false;
                    continue;
                }

                // Data di file geonames/TSV dipisahkan oleh karakter TAB (\t)
                String[] data = line.split("\t");

                // File geonames standar memiliki 19 kolom. Lakukan validasi jumlah kolom.
                if (data.length >= 19) { // Pastikan jumlah kolom minimal 19
                     try {
                         // *** PENTING: LAKUKAN FILTERING DATA SAAT MEMUAT ***
                         // File Geonames sangat besar! Filter hanya data yang relevan dan dianggap 'kota besar'.
                         // Ini krusial untuk manajemen memori dan performa.
                         String featureClass = data[6]; // Kolom 'feat_class' ada di indeks 6
                         long population = Long.parseLong(data[14]); // Kolom 'population' ada di indeks 14

                         // Contoh Filter: hanya kota besar (Feature Class 'P') dengan populasi > 5000
                         // Anda perlu menentukan kriteria 'kota besar' yang sesuai dengan tantangan.
                         if ("P".equals(featureClass) && population > 5000) { // <-- SESUAIKAN KRITERIA FILTER ANDA
                             CityData city = new CityData(data); // Buat objek CityData dari data baris
                             allCitiesRaw.add(city); // Tambahkan ke list data mentah sementara
                             countLoaded++;
                         } else {
                             countSkipped++;
                         }

                     } catch (NumberFormatException e) {
                         // Tangani error jika ada data numerik yang tidak valid (misal: populasi kosong/teks)
                          // System.err.println("Lewati baris (Data Numerik Tidak Valid): " + line);
                          countSkipped++; // Hitung sebagai baris dilewati
                     } catch (ArrayIndexOutOfBoundsException e) {
                          // Tangani jika `split` gagal (jarang terjadi jika length >= 19 tapi baik untuk jaga-jaga)
                          // System.err.println("Lewati baris (Error Parsing Kolom): " + line);
                          countSkipped++;
                     } catch (Exception e) {
                         // Tangani error tak terduga saat parsing baris
                         System.err.println("Error tak terduga saat parsing baris: " + line + " - " + e.getMessage());
                         countSkipped++;
                     }
                } else {
                    // Lewati baris yang tidak memiliki jumlah kolom yang sesuai
                     // System.err.println("Lewati baris (Jumlah Kolom Tidak Sesuai " + data.length + "): " + line);
                     countSkipped++;
                }
                 // !!! OPSI: Untuk testing awal, batasi jumlah baris yang dibaca agar cepat startup !!!
                 // HAPUS atau komen baris di bawah saat Anda siap memuat data penuh (setelah optimasi indeks)
                 // if (countLoaded >= 50000) { // Baca hanya 50,000 data relevan pertama (sesuaikan angka)
                 //    System.out.println("Membatasi muat data hanya " + countLoaded + " untuk testing.");
                 //    break; // Keluar dari loop baca file
                 // }
            }
            System.out.println("Selesai memuat " + countLoaded + " data kota yang relevan.");
            System.out.println("Melewati " + countSkipped + " baris data (header, tidak relevan, atau error).");

        } catch (IOException e) {
            // Tangani error jika file tidak ditemukan atau gagal dibaca
            System.err.println("Gagal memuat data kota dari file: " + cityDataFile.getFilename() + " - " + e.getMessage());
            // Jika data gagal dimuat saat startup, aplikasi mungkin tidak bisa berfungsi, jadi lempar RuntimeException
             throw new RuntimeException("Gagal memuat data awal kota dari file", e);
        }

        // *** PENTING: Panggil metode untuk membangun struktur data pencarian efisien ***
        // Bagian ini sangat krusial untuk performa pencarian.
         System.out.println("Mulai membangun indeks pencarian efisien dari " + allCitiesRaw.size() + " data...");
         buildSearchIndex(allCitiesRaw); // Panggil metode untuk membangun indeks
         System.out.println("Selesai membangun indeks pencarian.");

         // OPSI: Setelah indeks efisien dibangun, `allCitiesRaw` (List) yang besar bisa dibersihkan
         // jika data sudah tercopy/terindeks di struktur lain dan tidak lagi diperlukan.
         // HAPUS baris di bawah HANYA jika yakin indeks Anda menyimpan kopi data atau tidak lagi merujuk ke objek di allCitiesRaw
         // allCitiesRaw = null;
    }

    // *** PENTING: IMPLEMENTASIKAN METODE buildSearchIndex() INI DENGAN CERMAT ***
    // Metode ini menerima data mentah (setelah difilter) dan membangun struktur data pencarian yang efisien.
    private void buildSearchIndex(List<CityData> rawData) {
         // !!! IMPLEMENTASIKAN LOGIKA PEMBANGUNAN INDEKS DI SINI !!!
         // Ini adalah TUGAS UTAMA yang menentukan seberapa cepat pencarian Anda nantinya.
         // GANTI placeholder `this.citySearchIndex = new ArrayList<>(rawData);` dengan implementasi indeks sebenarnya.
         // Contoh:
         // - Inisialisasi `this.citySearchIndex` dengan objek dari struktur data pilihan Anda (Trie, Map yang diorganisir, dll.)
         // - Loop melalui setiap objek `CityData` di `rawData`.
         // - Untuk setiap `CityData`, ekstrak nama (`getName()`), nama ASCII (`getAsciiname()`), dan nama alternatif (`getAlternateNames()`).
         // - Masukkan string nama-nama ini ke dalam struktur data indeks Anda, asosiasikan dengan objek `CityData` atau referensinya,
         //   sehingga Anda bisa dengan cepat menemukan objek `CityData` berdasarkan query pencarian.

         // --- Placeholder: Menggunakan List biasa sebagai 'indeks' (SANGAT TIDAK EFISIEN untuk data besar!) ---
         // GANTI BARIS INI dengan logika pembangunan indeks yang sebenarnya:
         System.out.println("PERINGATAN: Struktur indeks pencarian masih menggunakan List biasa (tidak efisien).");
         this.citySearchIndex = new ArrayList<>(rawData); // <-- Ini hanya mengkopi data mentah ke list lain
         // Saat `searchCitiesEfficiently` dipanggil, ini akan melakukan pencarian linear di atas list ini, yang LAMBAT untuk jutaan data.
         // --- Akhir Placeholder ---

    }


    // Metode utama yang dipanggil oleh Controller untuk mendapatkan saran
    public List<CitySuggestion> getSuggestions(String query, Double latitude, Double longitude) {
        // 1. Lakukan pencarian kandidat kota yang cocok dengan 'query' menggunakan indeks efisien Anda
        //    Panggil metode `searchCitiesEfficiently` yang harus menggunakan `citySearchIndex` (struktur data efisien Anda)
        //    untuk menemukan kandidat kota dengan CEPAT berdasarkan `query` string.

        List<CityData> matchedCitiesCandidates = searchCitiesEfficiently(query); // <-- Panggil metode pencarian efisien Anda

        // Jika tidak ada kandidat yang cocok, kembalikan daftar kosong
        if (matchedCitiesCandidates == null || matchedCitiesCandidates.isEmpty()) {
            return Collections.emptyList(); // Mengembalikan immutable empty list
        }

        // 2. Hitung skor untuk setiap kandidat kota yang ditemukan
        List<CitySuggestion> suggestions = new ArrayList<>();
        for (CityData city : matchedCitiesCandidates) {
            // calculateScore akan menggabungkan faktor nama, populasi, dan jarak
            double score = calculateScore(city, query, latitude, longitude);
             // Jika skor di bawah ambang batas tertentu, bisa dilewati (opsional)
             // if (score > 0.1) { // Contoh: hanya tampilkan saran dengan skor di atas 0.1
                suggestions.add(new CitySuggestion(buildDisplayName(city), city.getLatitude(), city.getLongitude(), score));
             // }
        }

        // Jika setelah scoring tidak ada saran yang valid (misal semua skor 0), kembalikan daftar kosong
        if (suggestions.isEmpty()) {
             return Collections.emptyList();
        }

        // 3. Urutkan daftar saran berdasarkan skor (dari tertinggi ke terendah)
        suggestions.sort(Comparator.comparingDouble(CitySuggestion::getScore).reversed());

         // 4. OPSI: Batasi jumlah hasil yang dikembalikan jika terlalu banyak
         // Misalnya, hanya kembalikan 10 saran teratas
         // return suggestions.stream().limit(10).collect(Collectors.toList());


        return suggestions; // Kembalikan daftar saran yang sudah terurut
    }

    // *** PENTING: IMPLEMENTASIKAN METODE searchCitiesEfficiently() INI ***
    // Metode ini HARUS CEPAT dalam menemukan kandidat CityData berdasarkan query menggunakan struktur indeks Anda.
    private List<CityData> searchCitiesEfficiently(String query) {
        // !!! IMPLEMENTASIKAN LOGIKA PENCARIAN EFISIEN DI SINI MENGGUNAKAN `citySearchIndex` !!!
        // Struktur `citySearchIndex` (yang Anda bangun di buildSearchIndex) harus bisa dengan cepat
        // mengembalikan daftar CityData yang relevan dengan string `query` (misal: mencari awalan, kata kunci, dll.)
        // ini adalah inti algoritma pencarian Anda.

        // --- Placeholder Pencarian Sederhana (SANGAT TIDAK EFISIEN untuk data besar!) ---
        // Kode ini akan melakukan pencarian linear pada list besar, GANTI KODE DI BAWAH INI!
        System.out.println("PERINGATAN: Melakukan pencarian inefisien pada List di searchCitiesEfficiently. Perlu diganti!");
        String lowerQuery = query.toLowerCase();
         return citySearchIndex.stream() // <-- Iterasi list besar di sini sangat LAMBAT
                 .filter(city -> {
                     // Cek kecocokan di nama utama (name, asciiname)
                     if (city.getAsciiname().toLowerCase().contains(lowerQuery) || city.getName().toLowerCase().contains(lowerQuery)) {
                         return true;
                     }
                     // Cek kecocokan di nama alternatif (jika ada)
                     if (city.getAlternateNames() != null) {
                         for (String altName : city.getAlternateNames()) {
                             if (altName.toLowerCase().contains(lowerQuery)) {
                                 return true;
                             }
                         }
                     }
                     return false;
                 })
                 .collect(Collectors.toList());
        // --- Akhir Placeholder ---
    }


    // *** PENTING: IMPLEMENTASIKAN METODE calculateScore() INI DENGAN CERMAT ***
    // Metode ini menghitung skor gabungan antara 0 dan 1 untuk satu kota kandidat
    private double calculateScore(CityData city, String query, Double requestLatitude, Double requestLongitude) {
        // !!! IMPLEMENTASIKAN LOGIKA KOMBINASI SKOR INI DENGAN CERMAT !!!
        // Sesuaikan bobot masing-masing faktor (nama, populasi, jarak) sesuai yang Anda anggap paling relevan
        // dan bagaimana Anda ingin mereka memengaruhi skor akhir.

         double nameMatchScore = calculateNameMatchScore(city, query); // Hitung skor kecocokan nama
         double populationScore = calculatePopulationScore(city.getPopulation()); // Hitung skor populasi

        double distanceScore = 0.0;
        // Hanya hitung skor jarak jika lokasi pengguna (latitude & longitude) diberikan
        if (requestLatitude != null && requestLongitude != null) {
             distanceScore = calculateDistanceScore(city.getLatitude(), city.getLongitude(), requestLatitude, requestLongitude); // Hitung skor jarak
        }

        // Contoh kombinasi skor (bobot bisa disesuaikan - total bobot = 1.0)
        // Misalnya: Kecocokan nama 60%, Populasi 20%, Jarak 20%
        double finalScore = (nameMatchScore * 0.6) + (populationScore * 0.2);
        if (requestLatitude != null && requestLongitude != null) {
             finalScore += (distanceScore * 0.2); // Tambahkan bobot jarak jika lokasi ada
        }

        // Pastikan skor akhir selalu berada di antara 0.0 dan 1.0
        return Math.max(0.0, Math.min(1.0, finalScore));
    }

    // *** PENTING: IMPLEMENTASIKAN METODE calculateNameMatchScore() INI ***
    // Metode ini menghitung seberapa baik 'query' cocok dengan nama kota (name, asciiname, alternatenames) -> Skor 0-1
    private double calculateNameMatchScore(CityData city, String query) {
         // !!! IMPLEMENTASIKAN LOGIKA INI DENGAT CERMAT !!!
        // Ini adalah bagian dari bagaimana Anda menilai relevansi berdasarkan teks.
        // Gunakan algoritma perbandingan string yang sesuai (misal: skor berdasarkan posisi match, kemiripan string).
        // Skala skor: 1.0 untuk kecocokan terbaik (misal: query persis sama atau query adalah awalan kota besar), 0.0 jika tidak ada kecocokan relevan.

         String lowerQuery = query.toLowerCase();
         String lowerName = city.getName().toLowerCase();
         String lowerAsciiname = city.getAsciiname().toLowerCase();

         // --- Contoh Placeholder Logika Scoring Nama (Perlu Disempurnakan) ---
         // Ini hanyalah contoh dasar, perlu diperbaiki agar lebih canggih
         if (lowerAsciiname.equals(lowerQuery) || lowerName.equals(lowerQuery)) {
             return 1.0; // Exact match
         }
         if (lowerAsciiname.startsWith(lowerQuery) || lowerName.startsWith(lowerQuery)) {
             // Jika query adalah awalan, beri skor tinggi, mungkin proporsional dengan panjang query
             return 0.9 + (double) lowerQuery.length() / lowerAsciiname.length() * 0.1; // Contoh: lebih panjang query, skor lebih tinggi jika prefix
         }
         if (lowerAsciiname.contains(lowerQuery) || lowerName.contains(lowerQuery)) {
             // Jika query muncul di tengah nama utama
             return 0.5 + (double) lowerQuery.length() / lowerAsciiname.length() * 0.2; // Contoh: lebih panjang query, skor lebih tinggi jika contains
         }
         // Cek nama alternatif (jika ada)
         if (city.getAlternateNames() != null) {
             for (String altName : city.getAlternateNames()) {
                 String lowerAltName = altName.toLowerCase();
                 if (lowerAltName.equals(lowerQuery)) {
                     return 0.9; // Exact match di nama alternatif
                 }
                 if (lowerAltName.startsWith(lowerQuery)) {
                     return 0.8 + (double) lowerQuery.length() / lowerAltName.length() * 0.1; // Prefix match di nama alternatif
                 }
                 if (lowerAltName.contains(lowerQuery)) {
                      return 0.4 + (double) lowerQuery.length() / lowerAltName.length() * 0.2; // Contains match di nama alternatif
                 }
             }
         }

        // Jika menggunakan library fuzzy matching (Levenshtein, Jaro-Winkler), hitung skor kemiripan di sini
        // dan kembalikan skor berdasarkan kemiripan tersebut (misal: skala 0-1 dari 0-100%)


         return 0.0; // Jika tidak ada kecocokan signifikan
         // --- Akhir Placeholder Logika Scoring Nama ---
    }

    // *** PENTING: IMPLEMENTASIKAN METODE calculatePopulationScore() INI ***
    // Metode ini mengubah nilai populasi kota menjadi skor antara 0 dan 1.
    private double calculatePopulationScore(long population) {
         // !!! IMPLEMENTASIKAN LOGIKA INI DENGAT CERMAT !!!
         // Bagaimana skala populasi diubah menjadi skor? Linear? Logaritmik? Perlu menentukan skala yang relevan.
         // Contoh: Kota dengan populasi X ke atas skor 1, populasi Y skor Z, populasi di bawah threshold filter skor 0.
         double maxPopConsidered = 10_000_000.0; // Populasi maksimum yang kita pertimbangkan untuk skor 1 (sesuaikan)
         double minPopForScore = 5000.0; // Populasi minimum yang relevan untuk mendapatkan skor (sesuaikan, harus >= filter di loadData)

         if (population < minPopForScore) return 0.0; // Di bawah threshold, skor 0

         // Contoh skala logaritmik (memberi bobot lebih pada perbedaan populasi kecil pada skala rendah, lebih realistis)
         double logPop = Math.log10(population);
         double logMinPop = Math.log10(minPopForScore);
         double logMaxPop = Math.log10(maxPopConsidered);

         // Normalisasi skor logaritmik antara 0 dan 1
         double scaledScore = (logPop - logMinPop) / (logMaxPop - logMinPop);

         return Math.max(0.0, Math.min(1.0, scaledScore)); // Pastikan skor antara 0-1
    }

    // *** PENTING: IMPLEMENTASIKAN METODE calculateDistanceScore() INI ***
    // Metode ini menghitung skor berdasarkan jarak geografis antara kota dan lokasi request.
    private double calculateDistanceScore(double cityLat, double cityLon, double requestLat, double requestLon) {
        // !!! IMPLEMENTASIKAN LOGIKA INI DENGAN CERMAT !!!
        // 1. Hitung jarak antara (cityLat, cityLon) dan (requestLat, requestLon) dalam KM.
        //    Gunakan rumus Haversine. Implementasi dasar sudah ada di bawah.
        // 2. Ubah jarak tersebut menjadi skor antara 0 dan 1.
        //    Semakin dekat, skor semakin tinggi. Jarak 0 km -> skor 1. Jarak > ambang batas -> skor 0.
        //    Tentukan ambang batas jarak di mana kota masih dianggap relevan karena kedekatannya.

        double distanceInKm = calculateHaversineDistance(cityLat, cityLon, requestLat, requestLon);

        double maxRelevantDistanceKm = 500.0; // Contoh: Kota di luar jarak 500km dapat skor jarak 0 (sesuaikan)
        double minDistanceScore = 0.1; // Beri skor dasar minimum jika dalam jarak jauh tapi masih relevan

        if (distanceInKm <= 0.1) return 1.0; // Sangat sangat dekat (dalam 100 meter), beri skor sempurna

        if (distanceInKm >= maxRelevantDistanceKm) return 0.0; // Terlalu jauh, skor 0

        // Contoh: Skor menurun secara tidak langsung (misal: menggunakan fungsi eksponensial atau pembagian)
        // Atau penurunan linear dari 1.0 (jarak ~0) ke minDistanceScore (jarak maxRelevantDistanceKm)
        double scoreDecreaseRange = maxRelevantDistanceKm;
        double calculatedScore = 1.0 - (distanceInKm / scoreDecreaseRange) * (1.0 - minDistanceScore);


        return Math.max(0.0, Math.min(1.0, calculatedScore)); // Pastikan skor antara 0-1
    }

    // Implementasi Haversine Formula (untuk menghitung jarak dalam KM antara dua titik koordinat)
    // Anda bisa menggunakan library Apache Commons Math atau implementasi yang lebih teruji jika mau.
    // Ini adalah implementasi matematis standar.
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // distance in km
    }


    // Metode untuk membangun nama lengkap kota yang akan ditampilkan di output JSON
    private String buildDisplayName(CityData city) {
        // Gabungkan nama kota, kode admin (jika relevan), dan kode negara
        // Contoh: "London, ON, Canada"

         StringBuilder displayName = new StringBuilder(city.getName());

         // Tambahkan kode admin1 jika ada dan bukan "0" atau kosong
         if (city.getAdmin1Code() != null && !city.getAdmin1Code().trim().isEmpty() && !"0".equals(city.getAdmin1Code())) {
             displayName.append(", ").append(city.getAdmin1Code());
         }
         // Tambahkan kode negara
         if (city.getCountryCode() != null && !city.getCountryCode().trim().isEmpty()) {
             displayName.append(", ").append(city.getCountryCode());
         }
         // Anda bisa tambahkan admin2, admin3, dst jika dirasa perlu untuk membedakan nama yang sama

         return displayName.toString();
    }
}