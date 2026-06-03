package services

import (
	"database/sql"
	"encoding/json"
	"errors"
	"net/http"
	"strings"
	"time"

	"ROAD-GUIDE-BACKEND/middleware"
	"ROAD-GUIDE-BACKEND/models"
	"ROAD-GUIDE-BACKEND/utils"

	"github.com/google/uuid"
)

func (s *Services) HandleListRegistrationRequests(w http.ResponseWriter, r *http.Request) {
	status := strings.TrimSpace(r.URL.Query().Get("status"))
	query := `
		SELECT r.id, r.user_id, r.poi_id, r.status, r.message, r.admin_note, r.created_at, r.updated_at,
		       u.identifier, u.name, p.name
		FROM business_registration_requests r
		JOIN users u ON u.id = r.user_id
		JOIN business_pois p ON p.id = r.poi_id
	`
	args := make([]any, 0)
	if status != "" {
		query += " WHERE r.status = ?"
		args = append(args, status)
	}
	query += " ORDER BY r.created_at ASC"

	rows, err := s.Query(query, args...)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list requests")
		return
	}
	defer rows.Close()
	response := []map[string]any{}
	for rows.Next() {
		var id, userID, poiID, reqStatus, msg, adminNote string
		var createdAt, updatedAt time.Time
		var userIdentifier, userName, poiName string
		if err := rows.Scan(&id, &userID, &poiID, &reqStatus, &msg, &adminNote, &createdAt, &updatedAt, &userIdentifier, &userName, &poiName); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan requests")
			return
		}
		response = append(response, map[string]any{
			"id":        id,
			"userId":    userID,
			"poiId":     poiID,
			"status":    reqStatus,
			"message":   msg,
			"adminNote": adminNote,
			"createdAt": createdAt,
			"updatedAt": updatedAt,
			"user": map[string]any{
				"identifier": userIdentifier,
				"name":       userName,
			},
			"poi": map[string]any{
				"name": poiName,
			},
		})
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"requests": response})
}

func (s *Services) HandleApproveRegistrationRequest(w http.ResponseWriter, r *http.Request) {
	adminUser := middleware.UserFromContext(r)
	requestID := utils.URLParam(r, "requestID")
	var body struct {
		AssignPoiIDs []string `json:"assignPoiIds"`
		AdminNote    string   `json:"adminNote"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	var reqUserID, reqPOIID, status string
	err := s.QueryRow(
		`SELECT user_id, poi_id, status FROM business_registration_requests WHERE id = ?`,
		requestID,
	).Scan(&reqUserID, &reqPOIID, &status)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			utils.WriteErr(w, http.StatusNotFound, "request not found")
			return
		}
		utils.WriteErr(w, http.StatusInternalServerError, "failed to fetch request")
		return
	}
	if status != "pending" {
		utils.WriteErr(w, http.StatusConflict, "request already processed")
		return
	}
	assignments := utils.UniqueStrings(append(body.AssignPoiIDs, reqPOIID))
	now := time.Now().UTC()

	tx, err := s.DB.Begin()
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to start transaction")
		return
	}
	defer tx.Rollback()

	if _, err := utils.TxExec(tx, "UPDATE users SET role = ?, updated_at = ? WHERE id = ?", models.RoleBusiness, now, reqUserID); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to update role")
		return
	}
	for _, poiID := range assignments {
		if _, err := utils.TxExec(
			tx,
			`INSERT INTO business_user_pois(user_id, poi_id, created_at)
			 VALUES(?, ?, ?)
			 ON CONFLICT(user_id, poi_id) DO NOTHING`,
			reqUserID, poiID, now,
		); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to assign poi")
			return
		}
	}
	if _, err := utils.TxExec(
		tx,
		`UPDATE business_registration_requests
		 SET status = 'approved', admin_note = ?, processed_by_user_id = ?, updated_at = ?
		 WHERE id = ?`,
		strings.TrimSpace(body.AdminNote), adminUser.ID, now, requestID,
	); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to update request status")
		return
	}
	if err := tx.Commit(); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to commit")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": "approved"})
}

func (s *Services) HandleRejectRegistrationRequest(w http.ResponseWriter, r *http.Request) {
	adminUser := middleware.UserFromContext(r)
	requestID := utils.URLParam(r, "requestID")
	var body struct {
		AdminNote string `json:"adminNote"`
	}
	_ = json.NewDecoder(r.Body).Decode(&body)
	result, err := s.Exec(
		`UPDATE business_registration_requests
		 SET status = 'rejected', admin_note = ?, processed_by_user_id = ?, updated_at = ?
		 WHERE id = ? AND status = 'pending'`,
		strings.TrimSpace(body.AdminNote), adminUser.ID, time.Now().UTC(), requestID,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to reject request")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		utils.WriteErr(w, http.StatusNotFound, "pending request not found")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": "rejected"})
}

func (s *Services) HandleListUsers(w http.ResponseWriter, _ *http.Request) {
	rows, err := s.Query(
		`SELECT id, identifier, name, role, created_at, updated_at
		 FROM users ORDER BY created_at DESC`,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list users")
		return
	}
	defer rows.Close()
	users := []map[string]any{}
	for rows.Next() {
		var u models.User
		if err := rows.Scan(&u.ID, &u.Identifier, &u.Name, &u.Role, &u.CreatedAt, &u.UpdatedAt); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan users")
			return
		}
		assignments, err := s.listAssignedPOIIDs(u.ID)
		if err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan assignments")
			return
		}
		users = append(users, map[string]any{
			"id":             u.ID,
			"identifier":     u.Identifier,
			"name":           u.Name,
			"role":           u.Role,
			"createdAt":      u.CreatedAt,
			"updatedAt":      u.UpdatedAt,
			"assignedPoiIds": assignments,
		})
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"users": users})
}

func (s *Services) HandleSetUserRole(w http.ResponseWriter, r *http.Request) {
	userID := utils.URLParam(r, "userID")
	var body struct {
		Role string `json:"role"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	if body.Role != models.RoleVisitor && body.Role != models.RoleBusiness && body.Role != models.RoleAdmin {
		utils.WriteErr(w, http.StatusBadRequest, "invalid role")
		return
	}
	result, err := s.Exec("UPDATE users SET role = ?, updated_at = ? WHERE id = ?", body.Role, time.Now().UTC(), userID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to update role")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		utils.WriteErr(w, http.StatusNotFound, "user not found")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": "updated"})
}

func (s *Services) HandleSetUserAssignments(w http.ResponseWriter, r *http.Request) {
	userID := utils.URLParam(r, "userID")
	var body struct {
		PoiIDs []string `json:"poiIds"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	tx, err := s.DB.Begin()
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to begin transaction")
		return
	}
	defer tx.Rollback()
	if _, err := utils.TxExec(tx, "DELETE FROM business_user_pois WHERE user_id = ?", userID); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to clear assignments")
		return
	}
	now := time.Now().UTC()
	for _, poiID := range utils.UniqueStrings(body.PoiIDs) {
		if poiID == "" {
			continue
		}
		if _, err := utils.TxExec(
			tx,
			`INSERT INTO business_user_pois(user_id, poi_id, created_at)
			 VALUES(?, ?, ?)`,
			userID, poiID, now,
		); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to create assignment")
			return
		}
	}
	if err := tx.Commit(); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to commit assignments")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": "updated"})
}

func (s *Services) HandleListBusinessPOIs(w http.ResponseWriter, _ *http.Request) {
	rows, err := s.Query(
		`SELECT p.id, p.name, p.address, p.description, p.category, p.metadata_json,
		        p.external_ref, p.latitude, p.longitude, p.created_at, p.updated_at,
		        (SELECT u.id FROM business_user_pois bup
		         JOIN users u ON u.id = bup.user_id
		         WHERE bup.poi_id = p.id
		         ORDER BY u.name LIMIT 1) AS owner_id,
		        (SELECT u.name FROM business_user_pois bup
		         JOIN users u ON u.id = bup.user_id
		         WHERE bup.poi_id = p.id
		         ORDER BY u.name LIMIT 1) AS owner_name,
		        (SELECT COUNT(*)::int FROM business_registration_requests r
		         WHERE r.poi_id = p.id AND r.status = 'pending') AS pending_claims
		 FROM business_pois p
		 ORDER BY p.name ASC`,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list pois")
		return
	}
	defer rows.Close()
	pois := []map[string]any{}
	for rows.Next() {
		var id, name, address, description, category, metadataJSON string
		var externalRef, ownerID, ownerName sql.NullString
		var latitude, longitude sql.NullFloat64
		var pendingClaims int
		var createdAt, updatedAt time.Time
		if err := rows.Scan(
			&id, &name, &address, &description, &category, &metadataJSON,
			&externalRef, &latitude, &longitude, &createdAt, &updatedAt,
			&ownerID, &ownerName, &pendingClaims,
		); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan pois")
			return
		}
		entry := map[string]any{
			"id":            id,
			"name":          name,
			"address":       address,
			"description":   description,
			"category":      utils.NormalizedCategory(category),
			"metadata":      utils.DecodeMap(metadataJSON),
			"createdAt":     createdAt,
			"updatedAt":     updatedAt,
			"pendingClaims": pendingClaims,
		}
		if externalRef.Valid {
			entry["externalRef"] = externalRef.String
		}
		if latitude.Valid {
			entry["latitude"] = latitude.Float64
		}
		if longitude.Valid {
			entry["longitude"] = longitude.Float64
		}
		if ownerID.Valid {
			entry["ownerId"] = ownerID.String
		}
		if ownerName.Valid {
			entry["ownerName"] = ownerName.String
		}
		pois = append(pois, utils.WithPOICategory(entry))
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"pois": pois})
}

func (s *Services) HandleCreateBusinessPOI(w http.ResponseWriter, r *http.Request) {
	var body struct {
		Name        string   `json:"name"`
		Address     string   `json:"address"`
		Description string   `json:"description"`
		Category    string   `json:"category"`
		Latitude    *float64 `json:"latitude"`
		Longitude   *float64 `json:"longitude"`
		ExternalRef string   `json:"externalRef"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	name := strings.TrimSpace(body.Name)
	address := strings.TrimSpace(body.Address)
	if name == "" || address == "" {
		utils.WriteErr(w, http.StatusBadRequest, "name and address are required")
		return
	}
	externalRef := strings.TrimSpace(body.ExternalRef)
	if externalRef == "" {
		externalRef = "admin:" + uuid.NewString()
	}
	metadata := map[string]any{}
	category := utils.NormalizedCategory(body.Category)
	if category != "" {
		metadata["category"] = category
	}
	metadataJSON, _ := json.Marshal(metadata)
	now := time.Now().UTC()
	id := uuid.NewString()
	_, err := s.Exec(
		`INSERT INTO business_pois(id, name, address, description, category, metadata_json, external_ref, latitude, longitude, created_at, updated_at)
		 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
		id, name, address, strings.TrimSpace(body.Description), category, string(metadataJSON),
		externalRef, body.Latitude, body.Longitude, now, now,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to create poi")
		return
	}
	poi, err := s.mustGetPOI(id)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to load created poi")
		return
	}
	utils.WriteJSON(w, http.StatusCreated, map[string]any{"poi": poi})
}

func (s *Services) HandleListPanoramas(w http.ResponseWriter, r *http.Request) {
	statusFilter := strings.TrimSpace(strings.ToLower(r.URL.Query().Get("status")))
	query := `
		SELECT m.id, m.poi_id, m.url, m.caption, m.status, m.admin_note, m.views,
		       m.file_size_bytes, m.created_at, m.created_by_user_id,
		       p.name, p.address, u.name
		FROM poi_media m
		JOIN business_pois p ON p.id = m.poi_id
		JOIN users u ON u.id = m.created_by_user_id
		WHERE m.kind = 'panorama'`
	args := []any{}
	if statusFilter != "" && statusFilter != "all" {
		query += " AND m.status = ?"
		args = append(args, statusFilter)
	}
	query += " ORDER BY m.created_at DESC"

	rows, err := s.Query(query, args...)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list panoramas")
		return
	}
	defer rows.Close()

	items := []map[string]any{}
	for rows.Next() {
		var id, poiID, url, caption, status, adminNote, poiName, poiAddress, userName, userID string
		var views int
		var fileSizeBytes int64
		var createdAt time.Time
		if err := rows.Scan(
			&id, &poiID, &url, &caption, &status, &adminNote, &views,
			&fileSizeBytes, &createdAt, &userID,
			&poiName, &poiAddress, &userName,
		); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan panorama")
			return
		}
		title := strings.TrimSpace(caption)
		if title == "" {
			title = poiName + " Panorama"
		}
		items = append(items, map[string]any{
			"id":            id,
			"title":         title,
			"poiId":         poiID,
			"poiName":       poiName,
			"location":      poiAddress,
			"uploadedBy":    userName,
			"userId":        userID,
			"status":        utils.PanoramaStatusLabel(status),
			"uploadedAt":    createdAt.UTC().Format(time.RFC3339),
			"views":         views,
			"thumbnail":     url,
			"imageUrl":      url,
			"fileSizeBytes": fileSizeBytes,
			"rejectionReason": func() string {
				if status == "rejected" {
					return adminNote
				}
				return ""
			}(),
		})
	}
	if err := rows.Err(); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to iterate panoramas")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"items": items})
}

func (s *Services) HandleApprovePanorama(w http.ResponseWriter, r *http.Request) {
	mediaID := utils.URLParam(r, "mediaID")
	result, err := s.Exec(
		`UPDATE poi_media SET status = 'approved', admin_note = ''
		 WHERE id = ? AND kind = 'panorama' AND status = 'pending'`,
		mediaID,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to approve panorama")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		utils.WriteErr(w, http.StatusNotFound, "pending panorama not found")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": "approved"})
}

func (s *Services) HandleRejectPanorama(w http.ResponseWriter, r *http.Request) {
	mediaID := utils.URLParam(r, "mediaID")
	var body struct {
		AdminNote string `json:"adminNote"`
	}
	_ = json.NewDecoder(r.Body).Decode(&body)
	note := strings.TrimSpace(body.AdminNote)
	if note == "" {
		note = "Rejected by admin."
	}
	result, err := s.Exec(
		`UPDATE poi_media SET status = 'rejected', admin_note = ?
		 WHERE id = ? AND kind = 'panorama' AND status = 'pending'`,
		note, mediaID,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to reject panorama")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		utils.WriteErr(w, http.StatusNotFound, "pending panorama not found")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": "rejected"})
}
