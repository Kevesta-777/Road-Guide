package services

import (
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"log"
	"mime/multipart"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"ROAD-GUIDE-BACKEND/middleware"
	"ROAD-GUIDE-BACKEND/models"
	"ROAD-GUIDE-BACKEND/utils"

	"github.com/google/uuid"
)

func (s *Services) HandleClaimStatus(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	poiID := utils.URLParam(r, "poiID")
	owned, err := s.userOwnsPOI(u.ID, poiID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to verify ownership")
		return
	}

	if u.Role == models.RoleBusiness && owned {
		utils.WriteJSON(w, http.StatusOK, map[string]any{
			"status":          "assigned",
			"canEditBusiness": true,
			"claimButtonAction": map[string]any{
				"type":       "navigate_business_edit",
				"targetPath": fmt.Sprintf("/business-pois/%s/edit", poiID),
			},
			"redirectToEditPath": fmt.Sprintf("/business-pois/%s/edit", poiID),
		})
		return
	}

	var hasPending int
	if err := s.QueryRow(
		`SELECT COUNT(*) FROM business_registration_requests
		 WHERE user_id = ? AND poi_id = ? AND status = 'pending'`,
		u.ID, poiID,
	).Scan(&hasPending); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to query registration request")
		return
	}

	status := "not_assigned"
	if hasPending > 0 {
		status = "pending_registration"
	}

	utils.WriteJSON(w, http.StatusOK, map[string]any{
		"status":               status,
		"canEditBusiness":      false,
		"message":              "Please complete your business registration.",
		"registrationGuidance": s.ClaimGuidance,
	})
}

func (s *Services) HandleCreateClaimRequest(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	poiID := utils.URLParam(r, "poiID")

	if _, err := s.mustGetPOI(poiID); err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			utils.WriteErr(w, http.StatusNotFound, "poi not found")
			return
		}
		utils.WriteErr(w, http.StatusInternalServerError, "failed to load poi")
		return
	}

	var body struct {
		Message string `json:"message"`
	}
	_ = json.NewDecoder(r.Body).Decode(&body)

	var hasPending int
	if err := s.QueryRow(
		`SELECT COUNT(*) FROM business_registration_requests
		 WHERE user_id = ? AND poi_id = ? AND status = 'pending'`,
		u.ID, poiID,
	).Scan(&hasPending); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to query request")
		return
	}
	if hasPending > 0 {
		utils.WriteErr(w, http.StatusConflict, "pending request already exists")
		return
	}

	now := time.Now().UTC()
	id := uuid.NewString()
	_, err := s.Exec(
		`INSERT INTO business_registration_requests(
			 id, user_id, poi_id, status, message, created_at, updated_at
		 ) VALUES(?, ?, ?, 'pending', ?, ?, ?)`,
		id, u.ID, poiID, strings.TrimSpace(body.Message), now, now,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to create request")
		return
	}
	utils.WriteJSON(w, http.StatusCreated, map[string]any{
		"id":      id,
		"status":  "pending",
		"message": "Your request has been submitted for admin review.",
	})
}

func (s *Services) HandleResolveBusinessPOI(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	var body struct {
		ExternalRef string   `json:"externalRef"`
		Name        string   `json:"name"`
		Address     string   `json:"address"`
		Category    string   `json:"category"`
		Latitude    *float64 `json:"latitude"`
		Longitude   *float64 `json:"longitude"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	externalRef := strings.TrimSpace(body.ExternalRef)
	if externalRef == "" {
		utils.WriteErr(w, http.StatusBadRequest, "externalRef is required")
		return
	}
	name := strings.TrimSpace(body.Name)
	if name == "" {
		name = "Unnamed Place"
	}
	address := strings.TrimSpace(body.Address)

	respond := func(poiID string, created bool, status int) {
		poi, getErr := s.mustGetPOI(poiID)
		if getErr != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to load poi")
			return
		}
		canEdit := false
		if u.Role == models.RoleBusiness {
			owned, err := s.userOwnsPOI(u.ID, poiID)
			if err != nil {
				utils.WriteErr(w, http.StatusInternalServerError, "failed to verify ownership")
				return
			}
			canEdit = owned
		}
		utils.WriteJSON(w, status, map[string]any{
			"poi":             poi,
			"created":         created,
			"canEditBusiness": canEdit,
		})
	}

	var existingID string
	err := s.QueryRow("SELECT id FROM business_pois WHERE external_ref = ?", externalRef).Scan(&existingID)
	if err == nil {
		if mergeErr := s.mergePOICategoryIfEmpty(existingID, body.Category); mergeErr != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to update poi category")
			return
		}
		respond(existingID, false, http.StatusOK)
		return
	}
	if !errors.Is(err, sql.ErrNoRows) {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to query poi")
		return
	}

	if u.Role == models.RoleBusiness {
		if ownedID, ok, findErr := s.findOwnedPOINearUser(u.ID, body.Latitude, body.Longitude, name); findErr != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to match assigned poi")
			return
		} else if ok {
			_, _ = s.Exec(
				`UPDATE business_pois SET external_ref = ?, updated_at = ? WHERE id = ?`,
				externalRef, time.Now().UTC(), ownedID,
			)
			respond(ownedID, false, http.StatusOK)
			return
		}
	}

	now := time.Now().UTC()
	id := uuid.NewString()
	category := utils.NormalizedCategory(body.Category)
	metadataJSON, _ := json.Marshal(utils.MetadataWithCategory(nil, category))
	_, err = s.Exec(
		`INSERT INTO business_pois(id, name, address, description, category, metadata_json, external_ref, latitude, longitude, created_at, updated_at)
		 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
		 ON CONFLICT (external_ref) WHERE external_ref IS NOT NULL DO NOTHING`,
		id, name, address, "", category, string(metadataJSON), externalRef, body.Latitude, body.Longitude, now, now,
	)
	if err != nil {
		log.Printf("resolve poi insert failed: %v", err)
		utils.WriteErr(w, http.StatusInternalServerError, "failed to create poi")
		return
	}

	var resolvedID string
	if err := s.QueryRow("SELECT id FROM business_pois WHERE external_ref = ?", externalRef).Scan(&resolvedID); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to resolve poi")
		return
	}
	respond(resolvedID, resolvedID == id, http.StatusCreated)
}

func (s *Services) HandleListMyBusinessPOIs(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	poiIDs, err := s.listAssignedPOIIDs(u.ID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list assigned pois")
		return
	}
	pois := make([]map[string]any, 0, len(poiIDs))
	for _, poiID := range poiIDs {
		poi, getErr := s.mustGetPOI(poiID)
		if getErr != nil {
			if errors.Is(getErr, sql.ErrNoRows) {
				continue
			}
			utils.WriteErr(w, http.StatusInternalServerError, "failed to load poi")
			return
		}
		pois = append(pois, poi)
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"pois": pois})
}

func (s *Services) HandleGetBusinessPOI(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	poiID := utils.URLParam(r, "poiID")
	if ok, err := s.canAccessPOI(u, poiID); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed auth check")
		return
	} else if !ok {
		utils.WriteErr(w, http.StatusForbidden, "you do not have access to this business")
		return
	}

	poi, err := s.mustGetPOI(poiID)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			utils.WriteErr(w, http.StatusNotFound, "poi not found")
			return
		}
		utils.WriteErr(w, http.StatusInternalServerError, "failed to load poi")
		return
	}
	media, err := s.listPOIMedia(poiID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to load media")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{
		"poi":   poi,
		"media": media,
	})
}

func (s *Services) HandleUpdateBusinessPOI(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	poiID := utils.URLParam(r, "poiID")
	if ok, err := s.canAccessPOI(u, poiID); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed auth check")
		return
	} else if !ok {
		utils.WriteErr(w, http.StatusForbidden, "you do not have access to this business")
		return
	}
	var body struct {
		Name        string         `json:"name"`
		Address     string         `json:"address"`
		Description string         `json:"description"`
		Category    string         `json:"category"`
		Metadata    map[string]any `json:"metadata"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	if body.Metadata == nil {
		body.Metadata = map[string]any{}
	}
	category := utils.NormalizedCategory(body.Category)
	if category == "" {
		category = utils.CategoryFromMetadata(body.Metadata)
	}
	if category != "" {
		body.Metadata["category"] = category
	}
	metadataJSON, _ := json.Marshal(body.Metadata)
	if body.Name == "" || body.Address == "" {
		utils.WriteErr(w, http.StatusBadRequest, "name and address are required")
		return
	}
	_, err := s.Exec(
		`UPDATE business_pois
		 SET name = ?, address = ?, description = ?, category = ?, metadata_json = ?, updated_at = ?
		 WHERE id = ?`,
		body.Name, body.Address, body.Description, category, string(metadataJSON), time.Now().UTC(), poiID,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to update poi")
		return
	}
	poi, err := s.mustGetPOI(poiID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to read updated poi")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"poi": poi})
}

func (s *Services) HandleUploadPOIMedia(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	poiID := utils.URLParam(r, "poiID")
	if ok, err := s.canAccessPOI(u, poiID); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed auth check")
		return
	} else if !ok {
		utils.WriteErr(w, http.StatusForbidden, "you do not have access to this business")
		return
	}

	if err := r.ParseMultipartForm(20 << 20); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid multipart body")
		return
	}
	kind := strings.TrimSpace(r.FormValue("kind"))
	if kind != "photo" && kind != "panorama" {
		utils.WriteErr(w, http.StatusBadRequest, "kind must be photo or panorama")
		return
	}
	caption := strings.TrimSpace(r.FormValue("caption"))
	sortOrder, _ := strconv.Atoi(r.FormValue("sortOrder"))

	file, header, err := r.FormFile("file")
	if err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "file is required")
		return
	}
	defer file.Close()

	publicURL, fileSize, err := s.saveUpload(file, header)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to store media")
		return
	}
	mediaStatus := "approved"
	if kind == "panorama" {
		mediaStatus = "pending"
	}
	id := uuid.NewString()
	now := time.Now().UTC()
	_, err = s.Exec(
		`INSERT INTO poi_media(
			 id, poi_id, kind, url, caption, sort_order, created_by_user_id, created_at,
			 status, admin_note, views, file_size_bytes
		 ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, '', 0, ?)`,
		id, poiID, kind, publicURL, caption, sortOrder, u.ID, now, mediaStatus, fileSize,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to save media metadata")
		return
	}
	utils.WriteJSON(w, http.StatusCreated, map[string]any{
		"media": map[string]any{
			"id":            id,
			"poiId":         poiID,
			"kind":          kind,
			"url":           publicURL,
			"caption":       caption,
			"sortOrder":     sortOrder,
			"status":        mediaStatus,
			"fileSizeBytes": fileSize,
			"createdAt":     now,
		},
	})
}

func (s *Services) HandleDeletePOIMedia(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	poiID := utils.URLParam(r, "poiID")
	mediaID := utils.URLParam(r, "mediaID")

	if ok, err := s.canAccessPOI(u, poiID); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed auth check")
		return
	} else if !ok {
		utils.WriteErr(w, http.StatusForbidden, "you do not have access to this business")
		return
	}

	var url string
	err := s.QueryRow("SELECT url FROM poi_media WHERE id = ? AND poi_id = ?", mediaID, poiID).Scan(&url)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			utils.WriteErr(w, http.StatusNotFound, "media not found")
			return
		}
		utils.WriteErr(w, http.StatusInternalServerError, "failed to load media")
		return
	}
	_, err = s.Exec("DELETE FROM poi_media WHERE id = ? AND poi_id = ?", mediaID, poiID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to delete media")
		return
	}
	localPath := strings.TrimPrefix(url, s.Config.PublicUploadPrefix+"/")
	if localPath != "" {
		_ = os.Remove(filepath.Join(s.Config.UploadDir, localPath))
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": "deleted"})
}

func (s *Services) saveUpload(file multipart.File, header *multipart.FileHeader) (string, int64, error) {
	ext := strings.ToLower(filepath.Ext(header.Filename))
	if ext == "" {
		ext = ".bin"
	}
	name := uuid.NewString() + ext
	targetPath := filepath.Join(s.Config.UploadDir, name)
	out, err := os.Create(targetPath)
	if err != nil {
		return "", 0, err
	}
	written, err := io.Copy(out, file)
	out.Close()
	if err != nil {
		return "", 0, err
	}
	return strings.TrimSuffix(s.Config.PublicUploadPrefix, "/") + "/" + name, written, nil
}

func (s *Services) mustGetPOI(poiID string) (map[string]any, error) {
	var id, name, address, description, category, metadataJSON, externalRef sql.NullString
	var latitude, longitude sql.NullFloat64
	var createdAt, updatedAt time.Time
	err := s.QueryRow(
		`SELECT id, name, address, description, category, metadata_json, external_ref, latitude, longitude, created_at, updated_at
		 FROM business_pois WHERE id = ?`,
		poiID,
	).Scan(&id, &name, &address, &description, &category, &metadataJSON, &externalRef, &latitude, &longitude, &createdAt, &updatedAt)
	if err != nil {
		return nil, err
	}
	result := map[string]any{
		"id":          id.String,
		"name":        name.String,
		"address":     address.String,
		"description": description.String,
		"category":    utils.NormalizedCategory(category.String),
		"metadata":    utils.DecodeMap(metadataJSON.String),
		"createdAt":   createdAt,
		"updatedAt":   updatedAt,
	}
	if externalRef.Valid {
		result["externalRef"] = externalRef.String
	}
	if latitude.Valid {
		result["latitude"] = latitude.Float64
	}
	if longitude.Valid {
		result["longitude"] = longitude.Float64
	}
	return utils.WithPOICategory(result), nil
}

func (s *Services) listPOIMedia(poiID string) ([]map[string]any, error) {
	rows, err := s.Query(
		`SELECT id, kind, url, caption, sort_order, created_by_user_id, created_at, status, views, file_size_bytes
		 FROM poi_media WHERE poi_id = ? ORDER BY kind, sort_order, created_at`,
		poiID,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	result := []map[string]any{}
	for rows.Next() {
		var id, kind, url, caption, userID, status string
		var sortOrder, views int
		var fileSizeBytes int64
		var createdAt time.Time
		if err := rows.Scan(&id, &kind, &url, &caption, &sortOrder, &userID, &createdAt, &status, &views, &fileSizeBytes); err != nil {
			return nil, err
		}
		result = append(result, map[string]any{
			"id":              id,
			"kind":            kind,
			"url":             url,
			"caption":         caption,
			"sortOrder":       sortOrder,
			"createdByUserId": userID,
			"createdAt":       createdAt,
			"status":          status,
			"views":           views,
			"fileSizeBytes":   fileSizeBytes,
		})
	}
	return result, rows.Err()
}

func (s *Services) userOwnsPOI(userID, poiID string) (bool, error) {
	var count int
	err := s.QueryRow(
		`SELECT COUNT(*) FROM business_user_pois WHERE user_id = ? AND poi_id = ?`,
		userID, poiID,
	).Scan(&count)
	return count > 0, err
}

func (s *Services) findOwnedPOINearUser(userID string, lat, lng *float64, name string) (string, bool, error) {
	if lat == nil || lng == nil {
		return "", false, nil
	}
	rows, err := s.Query(
		`SELECT bp.id, bp.name, bp.latitude, bp.longitude
		 FROM business_user_pois bup
		 JOIN business_pois bp ON bp.id = bup.poi_id
		 WHERE bup.user_id = ?`,
		userID,
	)
	if err != nil {
		return "", false, err
	}
	defer rows.Close()

	const tolerance = 0.0008
	normalizedName := utils.NormalizePlaceName(name)
	for rows.Next() {
		var poiID, poiName string
		var poiLat, poiLng sql.NullFloat64
		if err := rows.Scan(&poiID, &poiName, &poiLat, &poiLng); err != nil {
			return "", false, err
		}
		if !poiLat.Valid || !poiLng.Valid {
			continue
		}
		if utils.MathAbs(poiLat.Float64-*lat) > tolerance || utils.MathAbs(poiLng.Float64-*lng) > tolerance {
			continue
		}
		if normalizedName != "" && utils.NormalizePlaceName(poiName) != "" &&
			utils.NormalizePlaceName(poiName) != normalizedName {
			continue
		}
		return poiID, true, nil
	}
	return "", false, rows.Err()
}

func (s *Services) canAccessPOI(u models.User, poiID string) (bool, error) {
	if u.Role == models.RoleAdmin {
		return true, nil
	}
	if u.Role != models.RoleBusiness {
		return false, nil
	}
	return s.userOwnsPOI(u.ID, poiID)
}

func (s *Services) listAssignedPOIIDs(userID string) ([]string, error) {
	rows, err := s.Query(
		`SELECT poi_id FROM business_user_pois WHERE user_id = ? ORDER BY poi_id`,
		userID,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	items := []string{}
	for rows.Next() {
		var poiID string
		if err := rows.Scan(&poiID); err != nil {
			return nil, err
		}
		items = append(items, poiID)
	}
	return items, rows.Err()
}

func (s *Services) mergePOICategoryIfEmpty(poiID, category string) error {
	category = utils.NormalizedCategory(category)
	if category == "" {
		return nil
	}
	var existingCategory, metadataJSON string
	if err := s.QueryRow(
		`SELECT category, metadata_json FROM business_pois WHERE id = ?`,
		poiID,
	).Scan(&existingCategory, &metadataJSON); err != nil {
		return err
	}
	if utils.NormalizedCategory(existingCategory) != "" {
		return nil
	}
	meta := utils.DecodeMap(metadataJSON)
	meta["category"] = category
	encoded, _ := json.Marshal(meta)
	_, err := s.Exec(
		`UPDATE business_pois SET category = ?, metadata_json = ?, updated_at = ? WHERE id = ?`,
		category, string(encoded), time.Now().UTC(), poiID,
	)
	return err
}
