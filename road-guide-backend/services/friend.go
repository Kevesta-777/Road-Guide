package services

import (
	"database/sql"
	"encoding/json"
	"errors"
	"net/http"
	"strings"
	"time"

	"ROAD-GUIDE-BACKEND/middleware"
	"ROAD-GUIDE-BACKEND/utils"

	"github.com/google/uuid"
)

func (s *Services) HandleLookupUserProfile(w http.ResponseWriter, r *http.Request) {
	profileID := strings.TrimSpace(utils.URLParam(r, "profileID"))
	if !isValidProfileID(profileID) {
		utils.WriteErr(w, http.StatusBadRequest, "invalid profile id")
		return
	}
	profile, err := s.lookupUserProfile(profileID)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			utils.WriteErr(w, http.StatusNotFound, "profile not found")
			return
		}
		utils.WriteErr(w, http.StatusInternalServerError, "failed to lookup profile")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"profile": profile})
}

func (s *Services) HandleListFriends(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	rows, err := s.Query(
		`SELECT u.id, u.identifier, u.name, uf.created_at
		 FROM user_friends uf
		 JOIN users u ON u.id = uf.friend_user_id
		 WHERE uf.user_id = ?
		 ORDER BY uf.created_at DESC`,
		u.ID,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list friends")
		return
	}
	defer rows.Close()
	friends := []map[string]any{}
	for rows.Next() {
		var profileID, identifier, name string
		var createdAt time.Time
		if err := rows.Scan(&profileID, &identifier, &name, &createdAt); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan friends")
			return
		}
		friends = append(friends, map[string]any{
			"profileId":  profileID,
			"identifier": identifier,
			"name":       name,
			"addedAt":    createdAt,
		})
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"friends": friends})
}

func (s *Services) HandleAddFriend(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	var body struct {
		ProfileID string `json:"profileId"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	profileID := strings.TrimSpace(body.ProfileID)
	if !isValidProfileID(profileID) {
		utils.WriteErr(w, http.StatusBadRequest, "invalid profile id")
		return
	}
	if strings.EqualFold(profileID, u.ID) {
		utils.WriteErr(w, http.StatusBadRequest, "cannot add yourself")
		return
	}
	if _, err := s.lookupUserProfile(profileID); err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			utils.WriteErr(w, http.StatusNotFound, "profile not found")
			return
		}
		utils.WriteErr(w, http.StatusInternalServerError, "failed to lookup profile")
		return
	}
	var existing int
	if err := s.QueryRow(
		`SELECT COUNT(*) FROM user_friends WHERE user_id = ? AND friend_user_id = ?`,
		u.ID, profileID,
	).Scan(&existing); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to check friendship")
		return
	}
	if existing > 0 {
		utils.WriteErr(w, http.StatusConflict, "already friends")
		return
	}
	now := time.Now().UTC()
	_, err := s.Exec(
		`INSERT INTO user_friends(user_id, friend_user_id, created_at)
		 VALUES(?, ?, ?)`,
		u.ID, profileID, now,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to add friend")
		return
	}
	profile, err := s.lookupUserProfile(profileID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to load friend profile")
		return
	}
	utils.WriteJSON(w, http.StatusCreated, map[string]any{"friend": profile})
}

func (s *Services) HandleRemoveFriend(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	profileID := strings.TrimSpace(utils.URLParam(r, "profileID"))
	if !isValidProfileID(profileID) {
		utils.WriteErr(w, http.StatusBadRequest, "invalid profile id")
		return
	}
	result, err := s.Exec(
		`DELETE FROM user_friends WHERE user_id = ? AND friend_user_id = ?`,
		u.ID, profileID,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to remove friend")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		utils.WriteErr(w, http.StatusNotFound, "friend not found")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": "removed"})
}

func (s *Services) lookupUserProfile(profileID string) (map[string]any, error) {
	var identifier, name string
	err := s.QueryRow(
		`SELECT identifier, name FROM users WHERE id = ?`,
		profileID,
	).Scan(&identifier, &name)
	if err != nil {
		return nil, err
	}
	return map[string]any{
		"profileId":  profileID,
		"identifier": identifier,
		"name":       name,
	}, nil
}

func isValidProfileID(value string) bool {
	_, err := uuid.Parse(strings.TrimSpace(value))
	return err == nil
}
