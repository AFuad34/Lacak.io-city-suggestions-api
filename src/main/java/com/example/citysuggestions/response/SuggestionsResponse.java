package com.example.citysuggestions.response;

import com.example.citysuggestions.model.CitySuggestion;

import java.util.List;

// Kelas ini membungkus daftar saran untuk format JSON respons keseluruhan
public class SuggestionsResponse {
    private List<CitySuggestion> suggestions;

    // Constructor
    public SuggestionsResponse(List<CitySuggestion> suggestions) {
        this.suggestions = suggestions;
    }

    // --- Getter --- (Generate di IDE agar bisa di-serialize ke JSON)
    public List<CitySuggestion> getSuggestions() {
        return suggestions;
    }

    // --- Setter --- (Tidak perlu jika objek bersifat immutable setelah dibuat)
    // public void setSuggestions(List<CitySuggestion> suggestions) { this.suggestions = suggestions; }
}