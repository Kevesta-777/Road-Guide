package services

import (
	"database/sql"
	"errors"
	"net/http"
	"strings"
	"time"

	"ROAD-GUIDE-BACKEND/utils"
)

func (s *Services) HandleGetPlaceDetail(w http.ResponseWriter, r *http.Request) {
	externalRef := strings.TrimSpace(r.URL.Query().Get("externalRef"))
	if externalRef == "" {
		utils.WriteErr(w, http.StatusBadRequest, "externalRef is required")
		return
	}

	var poiID string
	err := s.QueryRow("SELECT id FROM business_pois WHERE external_ref = ?", externalRef).Scan(&poiID)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			utils.WriteJSON(w, http.StatusOK, map[string]any{"hasBusinessData": false})
			return
		}
		utils.WriteErr(w, http.StatusInternalServerError, "failed to resolve place")
		return
	}

	poi, err := s.mustGetPOI(poiID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to read business place")
		return
	}
	photos, err := s.listApprovedPhotos(poiID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list photos")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{
		"hasBusinessData": true,
		"poi":             poi,
		"photos":          photos,
	})
}

func (s *Services) HandleListPlacePanoramas(w http.ResponseWriter, r *http.Request) {
	externalRef := strings.TrimSpace(r.URL.Query().Get("externalRef"))
	if externalRef == "" {
		utils.WriteErr(w, http.StatusBadRequest, "externalRef is required")
		return
	}

	var poiID string
	err := s.QueryRow("SELECT id FROM business_pois WHERE external_ref = ?", externalRef).Scan(&poiID)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			utils.WriteJSON(w, http.StatusOK, map[string]any{"poiId": "", "panoramas": []any{}})
			return
		}
		utils.WriteErr(w, http.StatusInternalServerError, "failed to resolve place")
		return
	}

	panoramas, err := s.listApprovedPanoramas(poiID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list panoramas")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{
		"poiId":     poiID,
		"panoramas": panoramas,
	})
}

func (s *Services) listApprovedPhotos(poiID string) ([]map[string]any, error) {
	rows, err := s.Query(
		`SELECT id, url, caption, sort_order, created_at
		 FROM poi_media
		 WHERE poi_id = ? AND kind = 'photo' AND status = 'approved'
		 ORDER BY sort_order, created_at`,
		poiID,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	result := []map[string]any{}
	for rows.Next() {
		var id, url, caption string
		var sortOrder int
		var createdAt time.Time
		if err := rows.Scan(&id, &url, &caption, &sortOrder, &createdAt); err != nil {
			return nil, err
		}
		result = append(result, map[string]any{
			"id":        id,
			"url":       url,
			"caption":   caption,
			"sortOrder": sortOrder,
			"createdAt": createdAt,
		})
	}
	return result, rows.Err()
}

func (s *Services) listApprovedPanoramas(poiID string) ([]map[string]any, error) {
	rows, err := s.Query(
		`SELECT id, url, caption, sort_order, created_at
		 FROM poi_media
		 WHERE poi_id = ? AND kind = 'panorama' AND status = 'approved'
		 ORDER BY sort_order, created_at`,
		poiID,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	result := []map[string]any{}
	for rows.Next() {
		var id, url, caption string
		var sortOrder int
		var createdAt time.Time
		if err := rows.Scan(&id, &url, &caption, &sortOrder, &createdAt); err != nil {
			return nil, err
		}
		result = append(result, map[string]any{
			"id":        id,
			"url":       url,
			"caption":   caption,
			"sortOrder": sortOrder,
			"createdAt": createdAt,
		})
	}
	return result, rows.Err()
}
