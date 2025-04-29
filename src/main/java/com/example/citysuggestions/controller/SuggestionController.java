package com.example.citysuggestions.controller;

import com.example.citysuggestions.model.CitySuggestion;
import com.example.citysuggestions.response.SuggestionsResponse;
import com.example.citysuggestions.service.CitySearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // Menandakan ini adalah Controller REST
@RequestMapping("/suggestions") // Menentukan base path untuk endpoint di controller ini
public class SuggestionController {

    private final CitySearchService citySearchService;

    // Dependency Injection: Spring akan menginjeksi instance CitySearchService
    @Autowired
    public SuggestionController(CitySearchService citySearchService) {
        this.citySearchService = citySearchService;
    }

    // Menangani permintaan GET ke /suggestions
    @GetMapping
    public ResponseEntity<SuggestionsResponse> getSuggestions(
            @RequestParam(name = "q", required = true) String query, // Parameter 'q' wajib
            @RequestParam(name = "latitude", required = false) Double latitude, // Parameter 'latitude' opsional
            @RequestParam(name = "longitude", required = false) Double longitude // Parameter 'longitude' opsional
    ) {
        // Validasi input dasar
        if (query == null || query.trim().isEmpty()) {
            // Kembalikan respons error jika query kosong atau hanya spasi
            // Mengembalikan BAD_REQUEST (400)
            return ResponseEntity.badRequest().body(new SuggestionsResponse(List.of())); // Mengembalikan list kosong di body
        }

        // Panggil service untuk mendapatkan saran
        List<CitySuggestion> suggestions = citySearchService.getSuggestions(query.trim(), latitude, longitude);

        // Kembalikan respons dalam format JSON
        // HTTP Status 200 OK secara default jika tidak ada error
        return ResponseEntity.ok(new SuggestionsResponse(suggestions));
    }
}