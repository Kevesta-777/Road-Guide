package utils

import (
	"encoding/json"
	"net/http"
)

func WriteErr(w http.ResponseWriter, status int, message string) {
	WriteJSON(w, status, map[string]any{"error": message})
}

func WriteJSON(w http.ResponseWriter, status int, payload any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(payload)
}

func DecodeMap(raw string) map[string]any {
	out := map[string]any{}
	if raw == "" {
		return out
	}
	_ = json.Unmarshal([]byte(raw), &out)
	return out
}
