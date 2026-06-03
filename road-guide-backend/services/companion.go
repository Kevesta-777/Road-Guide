package services

import (
	"database/sql"
	"encoding/json"
	"errors"
	"math"
	"net/http"
	"strings"
	"time"

	"ROAD-GUIDE-BACKEND/middleware"
	"ROAD-GUIDE-BACKEND/models"
	"ROAD-GUIDE-BACKEND/utils"

	"github.com/google/uuid"
)

func companionMigrateDDL() []string {
	return []string{
		`CREATE TABLE IF NOT EXISTS companion_driver_posts (
			id TEXT PRIMARY KEY,
			user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
			origin_label TEXT NOT NULL,
			destination_label TEXT NOT NULL,
			origin_lat DOUBLE PRECISION,
			origin_lon DOUBLE PRECISION,
			dest_lat DOUBLE PRECISION,
			dest_lon DOUBLE PRECISION,
			depart_at TIMESTAMPTZ NOT NULL,
			seats_total INTEGER NOT NULL CHECK (seats_total > 0),
			seats_booked INTEGER NOT NULL DEFAULT 0 CHECK (seats_booked >= 0),
			price_per_seat_cents INTEGER NOT NULL DEFAULT 0,
			vehicle TEXT NOT NULL DEFAULT '',
			preferences TEXT NOT NULL DEFAULT '',
			route_summary TEXT NOT NULL DEFAULT '',
			distance_label TEXT NOT NULL DEFAULT '',
			duration_label TEXT NOT NULL DEFAULT '',
			status TEXT NOT NULL DEFAULT 'active',
			created_at TIMESTAMPTZ NOT NULL,
			updated_at TIMESTAMPTZ NOT NULL
		);`,
		`CREATE INDEX IF NOT EXISTS idx_companion_driver_posts_status ON companion_driver_posts(status);`,
		`CREATE INDEX IF NOT EXISTS idx_companion_driver_posts_user ON companion_driver_posts(user_id);`,
		`CREATE INDEX IF NOT EXISTS idx_companion_driver_posts_depart ON companion_driver_posts(depart_at);`,
		`CREATE TABLE IF NOT EXISTS companion_passenger_requests (
			id TEXT PRIMARY KEY,
			user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
			origin_label TEXT NOT NULL,
			destination_label TEXT NOT NULL,
			origin_lat DOUBLE PRECISION,
			origin_lon DOUBLE PRECISION,
			dest_lat DOUBLE PRECISION,
			dest_lon DOUBLE PRECISION,
			window_start TIMESTAMPTZ NOT NULL,
			window_end TIMESTAMPTZ NOT NULL,
			passenger_count INTEGER NOT NULL DEFAULT 1 CHECK (passenger_count > 0),
			max_price_cents INTEGER NOT NULL DEFAULT 0,
			preferences TEXT NOT NULL DEFAULT '',
			status TEXT NOT NULL DEFAULT 'looking',
			matched_driver_post_id TEXT REFERENCES companion_driver_posts(id) ON DELETE SET NULL,
			created_at TIMESTAMPTZ NOT NULL,
			updated_at TIMESTAMPTZ NOT NULL
		);`,
		`CREATE INDEX IF NOT EXISTS idx_companion_passenger_status ON companion_passenger_requests(status);`,
		`CREATE INDEX IF NOT EXISTS idx_companion_passenger_user ON companion_passenger_requests(user_id);`,
		`CREATE TABLE IF NOT EXISTS companion_bookings (
			id TEXT PRIMARY KEY,
			driver_post_id TEXT NOT NULL REFERENCES companion_driver_posts(id) ON DELETE CASCADE,
			passenger_user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
			passenger_request_id TEXT REFERENCES companion_passenger_requests(id) ON DELETE SET NULL,
			seats INTEGER NOT NULL DEFAULT 1 CHECK (seats > 0),
			status TEXT NOT NULL DEFAULT 'confirmed',
			created_at TIMESTAMPTZ NOT NULL
		);`,
		`CREATE UNIQUE INDEX IF NOT EXISTS idx_companion_booking_user_post ON companion_bookings(driver_post_id, passenger_user_id);`,
		`CREATE TABLE IF NOT EXISTS companion_matches (
			id TEXT PRIMARY KEY,
			driver_post_id TEXT NOT NULL REFERENCES companion_driver_posts(id) ON DELETE CASCADE,
			passenger_request_id TEXT NOT NULL REFERENCES companion_passenger_requests(id) ON DELETE CASCADE,
			match_score INTEGER NOT NULL DEFAULT 0,
			reasons_json JSONB NOT NULL DEFAULT '[]'::jsonb,
			status TEXT NOT NULL DEFAULT 'pending',
			created_at TIMESTAMPTZ NOT NULL,
			updated_at TIMESTAMPTZ NOT NULL
		);`,
		`CREATE UNIQUE INDEX IF NOT EXISTS idx_companion_match_pair ON companion_matches(driver_post_id, passenger_request_id);`,
		`ALTER TABLE companion_passenger_requests ADD COLUMN IF NOT EXISTS source TEXT NOT NULL DEFAULT 'request';`,
	}
}

func sqlErrIsUniqueViolation(err error) bool {
	if err == nil {
		return false
	}
	msg := strings.ToLower(err.Error())
	return strings.Contains(msg, "unique") || strings.Contains(msg, "duplicate key")
}

func SeedCompanion(db *sql.DB) error {
	var count int
	if err := db.QueryRow(`SELECT COUNT(*) FROM companion_driver_posts`).Scan(&count); err != nil {
		return err
	}
	if count > 0 {
		return nil
	}

	var adminID string
	err := db.QueryRow(utils.RebindPostgres(`SELECT id FROM users WHERE role = ? LIMIT 1`), models.RoleAdmin).Scan(&adminID)
	if err != nil {
		return nil
	}

	now := time.Now().UTC()
	tomorrow := now.Add(48 * time.Hour)
	postID := uuid.NewString()
	reqID := uuid.NewString()
	matchID := uuid.NewString()

	_, err = db.Exec(
		utils.RebindPostgres(`INSERT INTO companion_driver_posts(
			id, user_id, origin_label, destination_label,
			origin_lat, origin_lon, dest_lat, dest_lon,
			depart_at, seats_total, seats_booked, price_per_seat_cents,
			vehicle, preferences, route_summary, distance_label, duration_label,
			status, created_at, updated_at
		) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`),
		postID, adminID,
		"123 Main St, Downtown", "456 Oak Ave, Airport",
		34.0522, -118.2437, 33.9416, -118.4085,
		tomorrow, 3, 0, 1500,
		"Toyota Camry 2022", "Non-smoking, No pets",
		"Main St → Highway 101 → Airport Blvd", "28 miles", "35 min",
		models.CompanionDriverActive, now, now,
	)
	if err != nil {
		return err
	}

	windowStart := tomorrow.Add(-30 * time.Minute)
	windowEnd := tomorrow.Add(90 * time.Minute)
	_, err = db.Exec(
		utils.RebindPostgres(`INSERT INTO companion_passenger_requests(
			id, user_id, origin_label, destination_label,
			origin_lat, origin_lon, dest_lat, dest_lon,
			window_start, window_end, passenger_count, max_price_cents,
			preferences, status, created_at, updated_at
		) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`),
		reqID, adminID,
		"150 Main St, Downtown", "450 Oak Ave, Airport Area",
		34.0510, -118.2450, 33.9400, -118.4100,
		windowStart, windowEnd, 1, 2000,
		"Prefer verified drivers", models.CompanionPassengerLooking, now, now,
	)
	if err != nil {
		return err
	}

	reasons, _ := json.Marshal([]string{"Same route", "Time match", "Price compatible"})
	_, err = db.Exec(
		utils.RebindPostgres(`INSERT INTO companion_matches(
			id, driver_post_id, passenger_request_id, match_score, reasons_json, status, created_at, updated_at
		) VALUES(?,?,?,?,?,?,?,?)`),
		matchID, postID, reqID, 95, string(reasons), models.CompanionMatchPending, now, now,
	)
	return err
}

func companionStatusLabelDriver(status string) string {
	switch strings.ToLower(strings.TrimSpace(status)) {
	case models.CompanionDriverActive:
		return "Active"
	case models.CompanionDriverCompleted:
		return "Completed"
	case models.CompanionDriverCancelled:
		return "Cancelled"
	default:
		return status
	}
}

func companionStatusLabelPassenger(status string) string {
	switch strings.ToLower(strings.TrimSpace(status)) {
	case models.CompanionPassengerLooking:
		return "Looking"
	case models.CompanionPassengerMatched:
		return "Matched"
	case models.CompanionPassengerCompleted:
		return "Completed"
	case models.CompanionPassengerCancelled:
		return "Cancelled"
	default:
		return status
	}
}

func companionStatusLabelMatch(status string) string {
	switch strings.ToLower(strings.TrimSpace(status)) {
	case models.CompanionMatchPending:
		return "Pending"
	case models.CompanionMatchAccepted:
		return "Accepted"
	case models.CompanionMatchDismissed:
		return "Dismissed"
	default:
		return status
	}
}

func userCompanionVerified(role string) bool {
	r := strings.ToLower(strings.TrimSpace(role))
	return r == models.RoleBusiness || r == models.RoleAdmin
}

func formatCompanionDate(t time.Time) string {
	return t.UTC().Format("2006-01-02")
}

func formatCompanionTime(t time.Time) string {
	return t.UTC().Format("03:04 PM")
}

func formatCompanionTimeRange(start, end time.Time) string {
	return formatCompanionTime(start) + " - " + formatCompanionTime(end)
}

func (s *Services) recomputeCompanionMatchesForPost(driverPostID string) error {
	var (
		postStatus                             string
		originLat, originLon, destLat, destLon sql.NullFloat64
		departAt                               time.Time
		seatsTotal, seatsBooked, priceCents    int
	)
	err := s.QueryRow(
		`SELECT status, origin_lat, origin_lon, dest_lat, dest_lon, depart_at,
		        seats_total, seats_booked, price_per_seat_cents
		 FROM companion_driver_posts WHERE id = ?`,
		driverPostID,
	).Scan(&postStatus, &originLat, &originLon, &destLat, &destLon, &departAt, &seatsTotal, &seatsBooked, &priceCents)
	if err != nil {
		return err
	}
	if postStatus != models.CompanionDriverActive || seatsBooked >= seatsTotal {
		return nil
	}

	rows, err := s.Query(
		`SELECT id, origin_lat, origin_lon, dest_lat, dest_lon, window_start, window_end,
		        passenger_count, max_price_cents, status
		 FROM companion_passenger_requests
		 WHERE status = ?`,
		models.CompanionPassengerLooking,
	)
	if err != nil {
		return err
	}
	defer rows.Close()

	now := time.Now().UTC()
	for rows.Next() {
		var reqID, reqStatus string
		var rOriginLat, rOriginLon, rDestLat, rDestLon sql.NullFloat64
		var windowStart, windowEnd time.Time
		var passengerCount, maxPrice int
		if err := rows.Scan(&reqID, &rOriginLat, &rOriginLon, &rDestLat, &rDestLon, &windowStart, &windowEnd, &passengerCount, &maxPrice, &reqStatus); err != nil {
			return err
		}
		score, reasons := scoreCompanionMatch(
			originLat, originLon, destLat, destLon, departAt, priceCents, seatsTotal-seatsBooked,
			rOriginLat, rOriginLon, rDestLat, rDestLon, windowStart, windowEnd, maxPrice, passengerCount,
		)
		if score < 50 {
			continue
		}
		reasonsJSON, _ := json.Marshal(reasons)
		_, err := s.Exec(
			`INSERT INTO companion_matches(id, driver_post_id, passenger_request_id, match_score, reasons_json, status, created_at, updated_at)
			 VALUES(?, ?, ?, ?, ?::jsonb, ?, ?, ?)
			 ON CONFLICT(driver_post_id, passenger_request_id) DO UPDATE SET
			   match_score = EXCLUDED.match_score,
			   reasons_json = EXCLUDED.reasons_json,
			   updated_at = EXCLUDED.updated_at`,
			uuid.NewString(), driverPostID, reqID, score, string(reasonsJSON), models.CompanionMatchPending, now, now,
		)
		if err != nil {
			return err
		}
	}
	return rows.Err()
}

func (s *Services) recomputeCompanionMatchesForRequest(passengerRequestID string) error {
	var (
		reqStatus                              string
		originLat, originLon, destLat, destLon sql.NullFloat64
		windowStart, windowEnd                 time.Time
		passengerCount, maxPrice               int
	)
	err := s.QueryRow(
		`SELECT status, origin_lat, origin_lon, dest_lat, dest_lon, window_start, window_end,
		        passenger_count, max_price_cents
		 FROM companion_passenger_requests WHERE id = ?`,
		passengerRequestID,
	).Scan(&reqStatus, &originLat, &originLon, &destLat, &destLon, &windowStart, &windowEnd, &passengerCount, &maxPrice)
	if err != nil {
		return err
	}
	if reqStatus != models.CompanionPassengerLooking {
		return nil
	}

	rows, err := s.Query(
		`SELECT id, origin_lat, origin_lon, dest_lat, dest_lon, depart_at,
		        seats_total, seats_booked, price_per_seat_cents
		 FROM companion_driver_posts WHERE status = ?`,
		models.CompanionDriverActive,
	)
	if err != nil {
		return err
	}
	defer rows.Close()

	now := time.Now().UTC()
	for rows.Next() {
		var postID string
		var pOriginLat, pOriginLon, pDestLat, pDestLon sql.NullFloat64
		var departAt time.Time
		var seatsTotal, seatsBooked, priceCents int
		if err := rows.Scan(&postID, &pOriginLat, &pOriginLon, &pDestLat, &pDestLon, &departAt, &seatsTotal, &seatsBooked, &priceCents); err != nil {
			return err
		}
		available := seatsTotal - seatsBooked
		if available <= 0 {
			continue
		}
		score, reasons := scoreCompanionMatch(
			pOriginLat, pOriginLon, pDestLat, pDestLon, departAt, priceCents, available,
			originLat, originLon, destLat, destLon, windowStart, windowEnd, maxPrice, passengerCount,
		)
		if score < 50 {
			continue
		}
		reasonsJSON, _ := json.Marshal(reasons)
		_, err := s.Exec(
			`INSERT INTO companion_matches(id, driver_post_id, passenger_request_id, match_score, reasons_json, status, created_at, updated_at)
			 VALUES(?, ?, ?, ?, ?::jsonb, ?, ?, ?)
			 ON CONFLICT(driver_post_id, passenger_request_id) DO UPDATE SET
			   match_score = EXCLUDED.match_score,
			   reasons_json = EXCLUDED.reasons_json,
			   updated_at = EXCLUDED.updated_at`,
			uuid.NewString(), postID, passengerRequestID, score, string(reasonsJSON), models.CompanionMatchPending, now, now,
		)
		if err != nil {
			return err
		}
	}
	return rows.Err()
}

func scoreCompanionMatch(
	pOriginLat, pOriginLon, pDestLat, pDestLon sql.NullFloat64,
	departAt time.Time, priceCents, seatsAvailable int,
	rOriginLat, rOriginLon, rDestLat, rDestLon sql.NullFloat64,
	windowStart, windowEnd time.Time, maxPriceCents, passengerCount int,
) (int, []string) {
	score := 0
	reasons := []string{}

	if departAt.Before(windowEnd) && departAt.After(windowStart.Add(-2*time.Hour)) {
		score += 30
		reasons = append(reasons, "Time match")
	}

	if priceCents <= maxPriceCents || maxPriceCents == 0 {
		score += 15
		reasons = append(reasons, "Price compatible")
	}

	if seatsAvailable >= passengerCount {
		score += 10
	}

	var originDist, destDist float64
	if pOriginLat.Valid && pOriginLon.Valid && rOriginLat.Valid && rOriginLon.Valid {
		originDist = utils.HaversineKm(pOriginLat.Float64, pOriginLon.Float64, rOriginLat.Float64, rOriginLon.Float64)
		if originDist < 15 {
			score += 20
			reasons = append(reasons, "Similar origin")
		}
	}
	if pDestLat.Valid && pDestLon.Valid && rDestLat.Valid && rDestLon.Valid {
		destDist = utils.HaversineKm(pDestLat.Float64, pDestLon.Float64, rDestLat.Float64, rDestLon.Float64)
		if destDist < 15 {
			score += 25
			reasons = append(reasons, "Similar destination")
		}
	}
	if originDist > 0 && originDist < 15 && destDist > 0 && destDist < 15 {
		reasons = append(reasons, "Same route")
	}

	if score > 100 {
		score = 100
	}
	if len(reasons) == 0 {
		reasons = append(reasons, "Possible match")
	}
	return score, utils.UniqueStrings(reasons)
}

func (s *Services) scanAdminDriverPost(rows *sql.Rows) (map[string]any, error) {
	var id, userID, origin, dest, vehicle, preferences, route, distance, duration, status, userName, userRole string
	var departAt, createdAt time.Time
	var seatsTotal, seatsBooked, priceCents int
	if err := rows.Scan(
		&id, &userID, &origin, &dest, &departAt, &seatsTotal, &seatsBooked, &priceCents,
		&vehicle, &preferences, &route, &distance, &duration, &status, &createdAt, &userName, &userRole,
	); err != nil {
		return nil, err
	}
	return map[string]any{
		"id":           id,
		"driver":       userName,
		"driverId":     userID,
		"verified":     userCompanionVerified(userRole),
		"from":         origin,
		"to":           dest,
		"date":         formatCompanionDate(departAt),
		"time":         formatCompanionTime(departAt),
		"seats":        seatsTotal,
		"seatsBooked":  seatsBooked,
		"pricePerSeat": float64(priceCents) / 100.0,
		"status":       companionStatusLabelDriver(status),
		"vehicle":      vehicle,
		"preferences":  preferences,
		"postedAt":     createdAt.UTC().Format("2006-01-02 03:04 PM"),
		"route":        route,
		"distance":     distance,
		"duration":     duration,
	}, nil
}

func (s *Services) HandleAdminListCompanionDriverPosts(w http.ResponseWriter, r *http.Request) {
	statusFilter := strings.TrimSpace(strings.ToLower(r.URL.Query().Get("status")))
	query := `
		SELECT p.id, p.user_id, p.origin_label, p.destination_label, p.depart_at,
		       p.seats_total, p.seats_booked, p.price_per_seat_cents,
		       p.vehicle, p.preferences, p.route_summary, p.distance_label, p.duration_label,
		       p.status, p.created_at, u.name, u.role
		FROM companion_driver_posts p
		JOIN users u ON u.id = p.user_id`
	args := []any{}
	if statusFilter != "" && statusFilter != "all" {
		query += " WHERE p.status = ?"
		args = append(args, statusFilter)
	}
	query += " ORDER BY p.created_at DESC"

	rows, err := s.Query(query, args...)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list driver posts")
		return
	}
	defer rows.Close()

	items := []map[string]any{}
	for rows.Next() {
		item, err := s.scanAdminDriverPost(rows)
		if err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan driver post")
			return
		}
		items = append(items, item)
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"items": items})
}

func (s *Services) HandleAdminListCompanionPassengerRequests(w http.ResponseWriter, r *http.Request) {
	rows, err := s.Query(`
		SELECT r.id, r.user_id, r.origin_label, r.destination_label,
		       r.window_start, r.window_end, r.passenger_count, r.max_price_cents,
		       r.preferences, r.status, r.created_at, r.matched_driver_post_id, r.source,
		       u.name, u.role,
		       COALESCE(du.name, '')
		FROM companion_passenger_requests r
		JOIN users u ON u.id = r.user_id
		LEFT JOIN companion_driver_posts dp ON dp.id = r.matched_driver_post_id
		LEFT JOIN users du ON du.id = dp.user_id
		ORDER BY r.created_at DESC`)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list passenger requests")
		return
	}
	defer rows.Close()

	items := []map[string]any{}
	for rows.Next() {
		var id, userID, origin, dest, preferences, status, userName, userRole, matchedDriverName, source string
		var matchedPostID sql.NullString
		var windowStart, windowEnd, createdAt time.Time
		var passengerCount, maxPrice int
		if err := rows.Scan(
			&id, &userID, &origin, &dest, &windowStart, &windowEnd, &passengerCount, &maxPrice,
			&preferences, &status, &createdAt, &matchedPostID, &source, &userName, &userRole, &matchedDriverName,
		); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan passenger request")
			return
		}

		matchCount := 0
		_ = s.QueryRow(
			`SELECT COUNT(*) FROM companion_matches WHERE passenger_request_id = ? AND status != ?`,
			id, models.CompanionMatchDismissed,
		).Scan(&matchCount)

		item := map[string]any{
			"id":          id,
			"passenger":   userName,
			"passengerId": userID,
			"verified":    userCompanionVerified(userRole),
			"from":        origin,
			"to":          dest,
			"date":        formatCompanionDate(windowStart),
			"timeRange":   formatCompanionTimeRange(windowStart, windowEnd),
			"passengers":  passengerCount,
			"maxPrice":    float64(maxPrice) / 100.0,
			"status":      companionStatusLabelPassenger(status),
			"preferences": preferences,
			"postedAt":    createdAt.UTC().Format("2006-01-02 03:04 PM"),
			"matches":     matchCount,
			"source":      source,
		}
		if matchedDriverName != "" {
			item["matchedDriver"] = matchedDriverName
		}
		items = append(items, item)
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"items": items})
}

func (s *Services) HandleAdminListCompanionMatches(w http.ResponseWriter, r *http.Request) {
	rows, err := s.Query(`
		SELECT m.id, m.driver_post_id, m.passenger_request_id, m.match_score, m.reasons_json, m.status
		FROM companion_matches m
		WHERE m.status != ?
		ORDER BY m.match_score DESC, m.created_at DESC`,
		models.CompanionMatchDismissed,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list matches")
		return
	}
	defer rows.Close()

	items := []map[string]any{}
	for rows.Next() {
		var id, driverPostID, passengerReqID, status string
		var reasonsJSON []byte
		var matchScore int
		if err := rows.Scan(&id, &driverPostID, &passengerReqID, &matchScore, &reasonsJSON, &status); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan match")
			return
		}
		reasons := []string{}
		_ = json.Unmarshal(reasonsJSON, &reasons)
		items = append(items, map[string]any{
			"id":               id,
			"driverPost":       driverPostID,
			"passengerRequest": passengerReqID,
			"matchScore":       matchScore,
			"reasons":          reasons,
			"status":           companionStatusLabelMatch(status),
		})
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"items": items})
}

func (s *Services) HandleAdminCompanionMatchNotify(w http.ResponseWriter, r *http.Request) {
	matchID := utils.URLParam(r, "matchID")
	now := time.Now().UTC()
	result, err := s.Exec(
		`UPDATE companion_matches SET status = ?, updated_at = ? WHERE id = ? AND status = ?`,
		models.CompanionMatchAccepted, now, matchID, models.CompanionMatchPending,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to update match")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		utils.WriteErr(w, http.StatusNotFound, "match not found or already processed")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": "notified"})
}

func (s *Services) HandleAdminCompanionMatchDismiss(w http.ResponseWriter, r *http.Request) {
	matchID := utils.URLParam(r, "matchID")
	now := time.Now().UTC()
	result, err := s.Exec(
		`UPDATE companion_matches SET status = ?, updated_at = ? WHERE id = ?`,
		models.CompanionMatchDismissed, now, matchID,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to dismiss match")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		utils.WriteErr(w, http.StatusNotFound, "match not found")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": "dismissed"})
}

func (s *Services) HandleAdminCompanionDriverPostStatus(w http.ResponseWriter, r *http.Request) {
	postID := utils.URLParam(r, "postID")
	var body struct {
		Status string `json:"status"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	status := strings.ToLower(strings.TrimSpace(body.Status))
	switch status {
	case models.CompanionDriverActive, models.CompanionDriverCompleted, models.CompanionDriverCancelled:
	default:
		utils.WriteErr(w, http.StatusBadRequest, "invalid status")
		return
	}
	now := time.Now().UTC()
	result, err := s.Exec(
		`UPDATE companion_driver_posts SET status = ?, updated_at = ? WHERE id = ?`,
		status, now, postID,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to update post")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		utils.WriteErr(w, http.StatusNotFound, "post not found")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": companionStatusLabelDriver(status)})
}

func (s *Services) HandleAdminCompanionRecomputeMatches(w http.ResponseWriter, r *http.Request) {
	var body struct {
		DriverPostID       string `json:"driverPostId"`
		PassengerRequestID string `json:"passengerRequestId"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	if body.DriverPostID == "" && body.PassengerRequestID == "" {
		utils.WriteErr(w, http.StatusBadRequest, "driverPostId or passengerRequestId is required")
		return
	}
	if body.DriverPostID != "" {
		if err := s.recomputeCompanionMatchesForPost(strings.TrimSpace(body.DriverPostID)); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to recompute matches")
			return
		}
	}
	if body.PassengerRequestID != "" {
		if err := s.recomputeCompanionMatchesForRequest(strings.TrimSpace(body.PassengerRequestID)); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to recompute matches")
			return
		}
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": "recomputed"})
}

// --- User-facing handlers ---

func (s *Services) HandleCreateCompanionDriverPost(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	snap, err := s.userSubscriptionSnapshot(u.ID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to verify subscription")
		return
	}
	if !snap.CanOfferRide {
		utils.WriteErr(w, http.StatusForbidden, "active Premium subscription required to post rides")
		return
	}
	var body struct {
		From          string   `json:"from"`
		To            string   `json:"to"`
		Waypoints     []string `json:"waypoints"`
		OriginLat     *float64 `json:"originLat"`
		OriginLon     *float64 `json:"originLon"`
		DestLat       *float64 `json:"destLat"`
		DestLon       *float64 `json:"destLon"`
		DepartAt      string   `json:"departAt"`
		Seats         int      `json:"seats"`
		PricePerSeat  float64  `json:"pricePerSeat"`
		Vehicle       string   `json:"vehicle"`
		Preferences   string   `json:"preferences"`
		RouteSummary  string   `json:"route"`
		DistanceLabel string   `json:"distance"`
		DurationLabel string   `json:"duration"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	from := strings.TrimSpace(body.From)
	to := strings.TrimSpace(body.To)
	if from == "" || to == "" {
		utils.WriteErr(w, http.StatusBadRequest, "from and to are required")
		return
	}
	departAt, err := time.Parse(time.RFC3339, strings.TrimSpace(body.DepartAt))
	if err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "departAt must be RFC3339")
		return
	}
	seats := body.Seats
	if seats < 1 {
		seats = 1
	}
	if seats > 60 {
		utils.WriteErr(w, http.StatusBadRequest, "seats must be between 1 and 60")
		return
	}
	cleanWaypoints := make([]string, 0, len(body.Waypoints))
	for _, raw := range body.Waypoints {
		cleaned := strings.TrimSpace(raw)
		if cleaned == "" {
			continue
		}
		cleanWaypoints = append(cleanWaypoints, cleaned)
	}
	routeSummary := strings.TrimSpace(body.RouteSummary)
	if routeSummary == "" {
		segments := []string{from}
		segments = append(segments, cleanWaypoints...)
		segments = append(segments, to)
		routeSummary = strings.Join(segments, " → ")
	}
	priceCents := int(math.Round(body.PricePerSeat * 100))

	id := uuid.NewString()
	now := time.Now().UTC()
	_, err = s.Exec(
		`INSERT INTO companion_driver_posts(
			id, user_id, origin_label, destination_label,
			origin_lat, origin_lon, dest_lat, dest_lon,
			depart_at, seats_total, seats_booked, price_per_seat_cents,
			vehicle, preferences, route_summary, distance_label, duration_label,
			status, created_at, updated_at
		) VALUES(?,?,?,?,?,?,?,?,?,?,0,?,?,?,?,?,?,'active',?,?)`,
		id, u.ID, from, to,
		body.OriginLat, body.OriginLon, body.DestLat, body.DestLon,
		departAt.UTC(), seats, priceCents,
		strings.TrimSpace(body.Vehicle), strings.TrimSpace(body.Preferences),
		routeSummary, strings.TrimSpace(body.DistanceLabel), strings.TrimSpace(body.DurationLabel),
		now, now,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to create driver post")
		return
	}
	_ = s.recomputeCompanionMatchesForPost(id)
	utils.WriteJSON(w, http.StatusCreated, map[string]any{"id": id})
}

func (s *Services) HandleListCompanionDriverPosts(w http.ResponseWriter, r *http.Request) {
	status := strings.TrimSpace(strings.ToLower(r.URL.Query().Get("status")))
	if status == "" {
		status = models.CompanionDriverActive
	}
	query := `
		SELECT p.id, p.user_id, p.origin_label, p.destination_label, p.depart_at,
		       p.seats_total, p.seats_booked, p.price_per_seat_cents,
		       p.vehicle, p.preferences, p.route_summary, p.distance_label, p.duration_label,
		       p.status, p.created_at, u.name, u.role
		FROM companion_driver_posts p
		JOIN users u ON u.id = p.user_id
		WHERE p.status = ?`
	args := []any{status}
	if mine := strings.TrimSpace(r.URL.Query().Get("mine")); mine == "1" || strings.EqualFold(mine, "true") {
		u := middleware.UserFromContext(r)
		query += " AND p.user_id = ?"
		args = append(args, u.ID)
	}
	query += " ORDER BY p.depart_at ASC"

	rows, err := s.Query(query, args...)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list driver posts")
		return
	}
	defer rows.Close()

	posts := []map[string]any{}
	for rows.Next() {
		item, err := s.scanAdminDriverPost(rows)
		if err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan driver post")
			return
		}
		posts = append(posts, item)
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"posts": posts})
}

func (s *Services) HandleUpdateCompanionDriverPost(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	postID := utils.URLParam(r, "postID")
	var body struct {
		Status string `json:"status"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	status := strings.ToLower(strings.TrimSpace(body.Status))
	if status != models.CompanionDriverCancelled && status != models.CompanionDriverCompleted {
		utils.WriteErr(w, http.StatusBadRequest, "invalid status")
		return
	}
	now := time.Now().UTC()
	result, err := s.Exec(
		`UPDATE companion_driver_posts SET status = ?, updated_at = ?
		 WHERE id = ? AND user_id = ?`,
		status, now, postID, u.ID,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to update post")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		utils.WriteErr(w, http.StatusNotFound, "post not found")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": companionStatusLabelDriver(status)})
}

func (s *Services) HandleCreateCompanionPassengerRequest(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	var body struct {
		From        string   `json:"from"`
		To          string   `json:"to"`
		OriginLat   *float64 `json:"originLat"`
		OriginLon   *float64 `json:"originLon"`
		DestLat     *float64 `json:"destLat"`
		DestLon     *float64 `json:"destLon"`
		WindowStart string   `json:"windowStart"`
		WindowEnd   string   `json:"windowEnd"`
		Passengers  int      `json:"passengers"`
		MaxPrice    float64  `json:"maxPrice"`
		Preferences string   `json:"preferences"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	from := strings.TrimSpace(body.From)
	to := strings.TrimSpace(body.To)
	if from == "" || to == "" {
		utils.WriteErr(w, http.StatusBadRequest, "from and to are required")
		return
	}
	windowStart, err := time.Parse(time.RFC3339, strings.TrimSpace(body.WindowStart))
	if err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "windowStart must be RFC3339")
		return
	}
	windowEnd, err := time.Parse(time.RFC3339, strings.TrimSpace(body.WindowEnd))
	if err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "windowEnd must be RFC3339")
		return
	}
	passengers := body.Passengers
	if passengers < 1 {
		passengers = 1
	}
	maxPriceCents := int(math.Round(body.MaxPrice * 100))

	id := uuid.NewString()
	now := time.Now().UTC()
	_, err = s.Exec(
		`INSERT INTO companion_passenger_requests(
			id, user_id, origin_label, destination_label,
			origin_lat, origin_lon, dest_lat, dest_lon,
			window_start, window_end, passenger_count, max_price_cents,
			preferences, status, created_at, updated_at
		) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
		id, u.ID, from, to,
		body.OriginLat, body.OriginLon, body.DestLat, body.DestLon,
		windowStart.UTC(), windowEnd.UTC(), passengers, maxPriceCents,
		strings.TrimSpace(body.Preferences), models.CompanionPassengerLooking, now, now,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to create passenger request")
		return
	}
	_ = s.recomputeCompanionMatchesForRequest(id)
	utils.WriteJSON(w, http.StatusCreated, map[string]any{"id": id})
}

func (s *Services) HandleListCompanionPassengerRequests(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	mineOnly := strings.TrimSpace(r.URL.Query().Get("mine"))
	query := `
		SELECT r.id, r.user_id, r.origin_label, r.destination_label,
		       r.window_start, r.window_end, r.passenger_count, r.max_price_cents,
		       r.preferences, r.status, r.created_at, r.matched_driver_post_id,
		       u.name, u.role, COALESCE(du.name, '')
		FROM companion_passenger_requests r
		JOIN users u ON u.id = r.user_id
		LEFT JOIN companion_driver_posts dp ON dp.id = r.matched_driver_post_id
		LEFT JOIN users du ON du.id = dp.user_id`
	args := []any{}
	if mineOnly == "1" || strings.EqualFold(mineOnly, "true") {
		query += " WHERE r.user_id = ?"
		args = append(args, u.ID)
	} else {
		query += " WHERE r.status = ?"
		args = append(args, models.CompanionPassengerLooking)
	}
	query += " ORDER BY r.created_at DESC"

	rows, err := s.Query(query, args...)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list passenger requests")
		return
	}
	defer rows.Close()

	requests := []map[string]any{}
	for rows.Next() {
		var id, userID, origin, dest, preferences, status, userName, userRole, matchedDriverName string
		var matchedPostID sql.NullString
		var windowStart, windowEnd, createdAt time.Time
		var passengerCount, maxPrice int
		if err := rows.Scan(
			&id, &userID, &origin, &dest, &windowStart, &windowEnd, &passengerCount, &maxPrice,
			&preferences, &status, &createdAt, &matchedPostID, &userName, &userRole, &matchedDriverName,
		); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan request")
			return
		}
		item := map[string]any{
			"id":          id,
			"passenger":   userName,
			"from":        origin,
			"to":          dest,
			"date":        formatCompanionDate(windowStart),
			"timeRange":   formatCompanionTimeRange(windowStart, windowEnd),
			"passengers":  passengerCount,
			"maxPrice":    float64(maxPrice) / 100.0,
			"status":      companionStatusLabelPassenger(status),
			"preferences": preferences,
		}
		if matchedDriverName != "" {
			item["matchedDriver"] = matchedDriverName
		}
		requests = append(requests, item)
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"requests": requests})
}

func (s *Services) HandleUpdateCompanionPassengerRequest(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	reqID := utils.URLParam(r, "requestID")
	var body struct {
		Status string `json:"status"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	status := strings.ToLower(strings.TrimSpace(body.Status))
	if status != models.CompanionPassengerCancelled && status != models.CompanionPassengerCompleted {
		utils.WriteErr(w, http.StatusBadRequest, "invalid status")
		return
	}
	now := time.Now().UTC()
	result, err := s.Exec(
		`UPDATE companion_passenger_requests SET status = ?, updated_at = ?
		 WHERE id = ? AND user_id = ?`,
		status, now, reqID, u.ID,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to update request")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		utils.WriteErr(w, http.StatusNotFound, "request not found")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": companionStatusLabelPassenger(status)})
}

func (s *Services) HandleBookCompanionDriverPost(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	postID := utils.URLParam(r, "postID")
	var body struct {
		Seats              int    `json:"seats"`
		PassengerRequestID string `json:"passengerRequestId"`
	}
	_ = json.NewDecoder(r.Body).Decode(&body)
	seats := body.Seats
	if seats < 1 {
		seats = 1
	}

	var ownerID, status, origin, dest string
	var seatsTotal, seatsBooked, priceCents int
	var departAt time.Time
	err := s.QueryRow(
		`SELECT user_id, status, seats_total, seats_booked, origin_label, destination_label, depart_at, price_per_seat_cents
		 FROM companion_driver_posts WHERE id = ?`,
		postID,
	).Scan(&ownerID, &status, &seatsTotal, &seatsBooked, &origin, &dest, &departAt, &priceCents)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			utils.WriteErr(w, http.StatusNotFound, "post not found")
			return
		}
		utils.WriteErr(w, http.StatusInternalServerError, "failed to load post")
		return
	}
	if status != models.CompanionDriverActive {
		utils.WriteErr(w, http.StatusConflict, "post is not active")
		return
	}
	if strings.EqualFold(ownerID, u.ID) {
		utils.WriteErr(w, http.StatusBadRequest, "cannot book your own ride")
		return
	}
	if seatsBooked+seats > seatsTotal {
		utils.WriteErr(w, http.StatusConflict, "not enough seats available")
		return
	}

	var existingBooking string
	err = s.QueryRow(
		`SELECT id FROM companion_bookings WHERE driver_post_id = ? AND passenger_user_id = ?`,
		postID, u.ID,
	).Scan(&existingBooking)
	if err == nil {
		utils.WriteErr(w, http.StatusConflict, "you already booked this ride")
		return
	}
	if err != nil && !errors.Is(err, sql.ErrNoRows) {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to check existing booking")
		return
	}

	now := time.Now().UTC()
	bookingID := uuid.NewString()
	tx, err := s.DB.Begin()
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to start transaction")
		return
	}
	defer tx.Rollback()

	passengerReqID := strings.TrimSpace(body.PassengerRequestID)
	if passengerReqID == "" {
		passengerReqID = uuid.NewString()
		windowEnd := departAt.Add(30 * time.Minute)
		if _, err := utils.TxExec(tx,
			`INSERT INTO companion_passenger_requests(
				id, user_id, origin_label, destination_label,
				window_start, window_end, passenger_count, max_price_cents,
				preferences, status, matched_driver_post_id, source, created_at, updated_at
			) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
			passengerReqID, u.ID, origin, dest, departAt, windowEnd, seats, priceCents,
			"Booked via Browse Rides", models.CompanionPassengerMatched, postID, "direct_booking", now, now,
		); err != nil {
			if strings.Contains(strings.ToLower(err.Error()), `column "source"`) {
				utils.WriteErr(w, http.StatusInternalServerError, "database migration required: restart backend to apply companion updates")
				return
			}
			utils.WriteErr(w, http.StatusInternalServerError, "failed to create passenger request")
			return
		}
	} else if _, err := utils.TxExec(tx,
		`UPDATE companion_passenger_requests
		 SET status = ?, matched_driver_post_id = ?, updated_at = ?
		 WHERE id = ? AND user_id = ?`,
		models.CompanionPassengerMatched, postID, now, passengerReqID, u.ID,
	); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to update passenger request")
		return
	}

	if _, err := utils.TxExec(tx,
		`INSERT INTO companion_bookings(id, driver_post_id, passenger_user_id, passenger_request_id, seats, status, created_at)
		 VALUES(?,?,?,?,?,'confirmed',?)`,
		bookingID, postID, u.ID, passengerReqID, seats, now,
	); err != nil {
		if sqlErrIsUniqueViolation(err) {
			utils.WriteErr(w, http.StatusConflict, "you already booked this ride")
			return
		}
		utils.WriteErr(w, http.StatusInternalServerError, "failed to create booking")
		return
	}
	if _, err := utils.TxExec(tx,
		`UPDATE companion_driver_posts SET seats_booked = seats_booked + ?, updated_at = ? WHERE id = ?`,
		seats, now, postID,
	); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to update seats")
		return
	}
	if err := tx.Commit(); err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to commit booking")
		return
	}
	utils.WriteJSON(w, http.StatusCreated, map[string]any{
		"bookingId":          bookingID,
		"passengerRequestId": passengerReqID,
	})
}

func (s *Services) HandleListCompanionSuggestedMatches(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	rows, err := s.Query(`
		SELECT m.id, m.driver_post_id, m.passenger_request_id, m.match_score, m.reasons_json, m.status
		FROM companion_matches m
		JOIN companion_driver_posts dp ON dp.id = m.driver_post_id
		LEFT JOIN companion_passenger_requests pr ON pr.id = m.passenger_request_id
		WHERE m.status = ? AND (dp.user_id = ? OR pr.user_id = ?)
		ORDER BY m.match_score DESC`,
		models.CompanionMatchPending, u.ID, u.ID,
	)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list matches")
		return
	}
	defer rows.Close()

	items := []map[string]any{}
	for rows.Next() {
		var id, driverPostID, passengerReqID, status string
		var reasonsJSON []byte
		var matchScore int
		if err := rows.Scan(&id, &driverPostID, &passengerReqID, &matchScore, &reasonsJSON, &status); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan match")
			return
		}
		reasons := []string{}
		_ = json.Unmarshal(reasonsJSON, &reasons)
		items = append(items, map[string]any{
			"id":                 id,
			"driverPostId":       driverPostID,
			"passengerRequestId": passengerReqID,
			"matchScore":         matchScore,
			"reasons":            reasons,
			"status":             companionStatusLabelMatch(status),
		})
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"matches": items})
}

func (s *Services) HandleAdminListCompanionBookings(w http.ResponseWriter, _ *http.Request) {
	rows, err := s.Query(`
		SELECT b.id, b.driver_post_id, b.passenger_user_id, b.passenger_request_id, b.seats, b.status, b.created_at,
		       pu.name, du.name, dp.origin_label, dp.destination_label, dp.depart_at
		FROM companion_bookings b
		JOIN users pu ON pu.id = b.passenger_user_id
		JOIN companion_driver_posts dp ON dp.id = b.driver_post_id
		JOIN users du ON du.id = dp.user_id
		ORDER BY b.created_at DESC`)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list bookings")
		return
	}
	defer rows.Close()
	items := []map[string]any{}
	for rows.Next() {
		var id, postID, passengerID, passengerName, driverName, origin, dest, status string
		var reqID sql.NullString
		var seats int
		var createdAt, departAt time.Time
		if err := rows.Scan(&id, &postID, &passengerID, &reqID, &seats, &status, &createdAt, &passengerName, &driverName, &origin, &dest, &departAt); err != nil {
			utils.WriteErr(w, http.StatusInternalServerError, "failed to scan booking")
			return
		}
		item := map[string]any{
			"id": id, "driverPostId": postID, "passengerId": passengerID, "passenger": passengerName,
			"driver": driverName, "from": origin, "to": dest, "seats": seats, "status": status,
			"date": formatCompanionDate(departAt), "time": formatCompanionTime(departAt),
			"bookedAt": createdAt.UTC().Format("2006-01-02 03:04 PM"),
		}
		if reqID.Valid {
			item["passengerRequestId"] = reqID.String
		}
		items = append(items, item)
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"items": items})
}

func (s *Services) HandleAdminCompanionPassengerRequestStatus(w http.ResponseWriter, r *http.Request) {
	reqID := utils.URLParam(r, "requestID")
	var body struct {
		Status string `json:"status"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	status := strings.TrimSpace(strings.ToLower(body.Status))
	switch status {
	case models.CompanionPassengerLooking, models.CompanionPassengerMatched, models.CompanionPassengerCompleted, models.CompanionPassengerCancelled:
	default:
		utils.WriteErr(w, http.StatusBadRequest, "invalid status")
		return
	}
	now := time.Now().UTC()
	result, err := s.Exec(`UPDATE companion_passenger_requests SET status = ?, updated_at = ? WHERE id = ?`, status, now, reqID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to update passenger request")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		utils.WriteErr(w, http.StatusNotFound, "request not found")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"status": companionStatusLabelPassenger(status)})
}
