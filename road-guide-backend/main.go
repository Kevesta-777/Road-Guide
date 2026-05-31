package main

import (
	"context"
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

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	"github.com/go-chi/cors"
	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	_ "github.com/jackc/pgx/v5/stdlib"
	"github.com/joho/godotenv"
	"golang.org/x/crypto/bcrypt"
)

const (
	roleVisitor  = "visitor"
	roleBusiness = "business"
	roleAdmin    = "admin"
)

type app struct {
	db                 *sql.DB
	jwtSecret          []byte
	claimGuidance      claimGuidance
	uploadDir          string
	publicUploadPrefix string
}

type claimGuidance struct {
	ContactPhone               string `json:"contactPhone"`
	RegistrationAgentAddress   string `json:"registrationAgentAddress"`
	AvailableRegistrationHours string `json:"availableRegistrationHours"`
	AdditionalInstructions     string `json:"additionalInstructions"`
}

type authClaims struct {
	UserID string `json:"userId"`
	Role   string `json:"role"`
	jwt.RegisteredClaims
}

type user struct {
	ID         string    `json:"id"`
	Identifier string    `json:"identifier"`
	Name       string    `json:"name"`
	Role       string    `json:"role"`
	CreatedAt  time.Time `json:"createdAt"`
	UpdatedAt  time.Time `json:"updatedAt"`
}

func main() {
	_ = godotenv.Load()

	databaseURL := getenv("DATABASE_URL", "postgres://postgres:postgres@localhost:5432/road_guide?sslmode=disable")
	jwtSecret := getenv("JWT_SECRET", "dev-secret-change-me")
	addr := getenv("APP_ADDR", ":8090")
	uploadDir := getenv("UPLOAD_DIR", "./uploads")
	publicUploadPrefix := getenv("PUBLIC_UPLOAD_PREFIX", "/uploads")

	if err := os.MkdirAll(uploadDir, 0o755); err != nil {
		log.Fatalf("create upload dir: %v", err)
	}

	db, err := sql.Open("pgx", databaseURL)
	if err != nil {
		log.Fatalf("open postgres: %v", err)
	}
	defer db.Close()
	db.SetMaxOpenConns(10)
	db.SetMaxIdleConns(5)

	if err := migrate(db); err != nil {
		log.Fatalf("migrate db: %v", err)
	}
	if err := seedAdmin(db); err != nil {
		log.Fatalf("seed admin: %v", err)
	}
	if err := seedPOIs(db); err != nil {
		log.Fatalf("seed pois: %v", err)
	}

	application := &app{
		db:                 db,
		jwtSecret:          []byte(jwtSecret),
		uploadDir:          uploadDir,
		publicUploadPrefix: publicUploadPrefix,
		claimGuidance: claimGuidance{
			ContactPhone:               getenv("CLAIM_CONTACT_PHONE", "+18658969348"),
			RegistrationAgentAddress:   getenv("CLAIM_AGENT_ADDRESS", "Road Guide Support Center, Los Angeles"),
			AvailableRegistrationHours: getenv("CLAIM_HOURS", "Mon-Fri 09:00-18:00"),
			AdditionalInstructions:     getenv("CLAIM_INSTRUCTIONS", "Please submit your business license and a valid owner ID."),
		},
	}

	r := chi.NewRouter()
	r.Use(middleware.RequestID)
	r.Use(middleware.RealIP)
	r.Use(middleware.Recoverer)
	r.Use(middleware.Timeout(60 * time.Second))
	r.Use(cors.Handler(cors.Options{
		AllowedOrigins:   []string{"*"},
		AllowedMethods:   []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowedHeaders:   []string{"Accept", "Authorization", "Content-Type"},
		AllowCredentials: false,
		MaxAge:           300,
	}))

	r.Get("/healthz", func(w http.ResponseWriter, _ *http.Request) {
		writeJSON(w, http.StatusOK, map[string]any{"status": "ok"})
	})

	r.Mount("/uploads", http.StripPrefix("/uploads", http.FileServer(http.Dir(uploadDir))))

	r.Route("/api/v1", func(api chi.Router) {
		api.Post("/auth/register", application.handleRegister)
		api.Post("/auth/login", application.handleLogin)
		api.Get("/places/panoramas", application.handleListPlacePanoramas)
		api.Get("/places/detail", application.handleGetPlaceDetail)

		api.Group(func(authed chi.Router) {
			authed.Use(application.requireAuth)
			authed.Get("/auth/me", application.handleMe)

			authed.Get("/business-claims/{poiID}", application.handleClaimStatus)
			authed.Post("/business-claims/{poiID}/requests", application.handleCreateClaimRequest)

			authed.Post("/business-pois/resolve", application.handleResolveBusinessPOI)
			authed.Get("/business-pois/mine", application.handleListMyBusinessPOIs)
			authed.Get("/business-pois/{poiID}", application.handleGetBusinessPOI)
			authed.Put("/business-pois/{poiID}", application.handleUpdateBusinessPOI)
			authed.Post("/business-pois/{poiID}/media", application.handleUploadPOIMedia)
			authed.Delete("/business-pois/{poiID}/media/{mediaID}", application.handleDeletePOIMedia)

			authed.Get("/friends", application.handleListFriends)
			authed.Post("/friends", application.handleAddFriend)
			authed.Delete("/friends/{profileID}", application.handleRemoveFriend)
			authed.Get("/users/profile/{profileID}", application.handleLookupUserProfile)

			authed.Route("/admin", func(admin chi.Router) {
				admin.Use(application.requireRole(roleAdmin))
				admin.Get("/registration-requests", application.handleListRegistrationRequests)
				admin.Post("/registration-requests/{requestID}/approve", application.handleApproveRegistrationRequest)
				admin.Post("/registration-requests/{requestID}/reject", application.handleRejectRegistrationRequest)

				admin.Get("/users", application.handleListUsers)
				admin.Put("/users/{userID}/role", application.handleSetUserRole)
				admin.Put("/users/{userID}/assignments", application.handleSetUserAssignments)
				admin.Get("/business-pois", application.handleListBusinessPOIs)
				admin.Post("/business-pois", application.handleCreateBusinessPOI)

				admin.Get("/panoramas", application.handleListPanoramas)
				admin.Post("/panoramas/{mediaID}/approve", application.handleApprovePanorama)
				admin.Post("/panoramas/{mediaID}/reject", application.handleRejectPanorama)
			})
		})
	})

	log.Printf("main api listening on %s", addr)
	log.Printf("database url: %s", databaseURL)
	if err := http.ListenAndServe(addr, r); err != nil {
		log.Fatal(err)
	}
}

func migrate(db *sql.DB) error {
	ddl := []string{
		`CREATE TABLE IF NOT EXISTS users (
			id TEXT PRIMARY KEY,
			identifier TEXT UNIQUE NOT NULL,
			password_hash TEXT NOT NULL,
			name TEXT NOT NULL,
			role TEXT NOT NULL DEFAULT 'visitor',
			created_at TIMESTAMPTZ NOT NULL,
			updated_at TIMESTAMPTZ NOT NULL
		);`,
		`DO $migrate$
		BEGIN
		  IF EXISTS (
		    SELECT 1 FROM information_schema.columns
		    WHERE table_schema = current_schema()
		      AND table_name = 'users'
		      AND column_name = 'email'
		  ) THEN
		    IF NOT EXISTS (
		      SELECT 1 FROM information_schema.columns
		      WHERE table_schema = current_schema()
		        AND table_name = 'users'
		        AND column_name = 'identifier'
		    ) THEN
		      ALTER TABLE users ADD COLUMN identifier TEXT;
		    END IF;
		    UPDATE users
		      SET identifier = LOWER(TRIM(SPLIT_PART(email, '@', 1)))
		      WHERE (identifier IS NULL OR TRIM(identifier) = '')
		        AND email ILIKE '%@roadguide.local';
		    UPDATE users
		      SET identifier = LOWER(TRIM(email))
		      WHERE identifier IS NULL OR TRIM(identifier) = '';
		    UPDATE users
		      SET identifier = id
		      WHERE identifier IS NULL OR TRIM(identifier) = '';
		    ALTER TABLE users DROP COLUMN email;
		  END IF;
		END
		$migrate$;`,
		`CREATE UNIQUE INDEX IF NOT EXISTS idx_users_identifier ON users(identifier);`,
		`ALTER TABLE users ALTER COLUMN identifier SET NOT NULL;`,
		`CREATE TABLE IF NOT EXISTS business_pois (
			id TEXT PRIMARY KEY,
			name TEXT NOT NULL,
			address TEXT NOT NULL,
			description TEXT NOT NULL DEFAULT '',
			category TEXT NOT NULL DEFAULT '',
			metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
			created_at TIMESTAMPTZ NOT NULL,
			updated_at TIMESTAMPTZ NOT NULL
		);`,
		`CREATE TABLE IF NOT EXISTS business_user_pois (
			user_id TEXT NOT NULL,
			poi_id TEXT NOT NULL,
			created_at TIMESTAMPTZ NOT NULL,
			PRIMARY KEY (user_id, poi_id),
			FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
			FOREIGN KEY (poi_id) REFERENCES business_pois(id) ON DELETE CASCADE
		);`,
		`CREATE TABLE IF NOT EXISTS business_registration_requests (
			id TEXT PRIMARY KEY,
			user_id TEXT NOT NULL,
			poi_id TEXT NOT NULL,
			status TEXT NOT NULL DEFAULT 'pending',
			message TEXT NOT NULL DEFAULT '',
			admin_note TEXT NOT NULL DEFAULT '',
			processed_by_user_id TEXT,
			created_at TIMESTAMPTZ NOT NULL,
			updated_at TIMESTAMPTZ NOT NULL,
			FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
			FOREIGN KEY (poi_id) REFERENCES business_pois(id) ON DELETE CASCADE
		);`,
		`CREATE TABLE IF NOT EXISTS poi_media (
			id TEXT PRIMARY KEY,
			poi_id TEXT NOT NULL,
			kind TEXT NOT NULL,
			url TEXT NOT NULL,
			caption TEXT NOT NULL DEFAULT '',
			sort_order INTEGER NOT NULL DEFAULT 0,
			created_by_user_id TEXT NOT NULL,
			created_at TIMESTAMPTZ NOT NULL,
			FOREIGN KEY (poi_id) REFERENCES business_pois(id) ON DELETE CASCADE,
			FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE CASCADE
		);`,
		`ALTER TABLE business_pois ADD COLUMN IF NOT EXISTS external_ref TEXT;`,
		`ALTER TABLE business_pois ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;`,
		`ALTER TABLE business_pois ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;`,
		`ALTER TABLE business_pois ADD COLUMN IF NOT EXISTS category TEXT NOT NULL DEFAULT '';`,
		`UPDATE business_pois
		 SET category = TRIM(metadata_json->>'category')
		 WHERE category = '' AND COALESCE(TRIM(metadata_json->>'category'), '') <> '';`,
		`CREATE UNIQUE INDEX IF NOT EXISTS idx_business_pois_external_ref ON business_pois(external_ref) WHERE external_ref IS NOT NULL;`,
		`CREATE INDEX IF NOT EXISTS idx_biz_req_user_status ON business_registration_requests(user_id, status);`,
		`CREATE INDEX IF NOT EXISTS idx_biz_req_poi_status ON business_registration_requests(poi_id, status);`,
		`CREATE INDEX IF NOT EXISTS idx_poi_media_poi ON poi_media(poi_id);`,
		`ALTER TABLE poi_media ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'pending';`,
		`ALTER TABLE poi_media ADD COLUMN IF NOT EXISTS admin_note TEXT NOT NULL DEFAULT '';`,
		`ALTER TABLE poi_media ADD COLUMN IF NOT EXISTS views INTEGER NOT NULL DEFAULT 0;`,
		`ALTER TABLE poi_media ADD COLUMN IF NOT EXISTS file_size_bytes BIGINT NOT NULL DEFAULT 0;`,
		`CREATE INDEX IF NOT EXISTS idx_poi_media_kind_status ON poi_media(kind, status);`,
		`CREATE TABLE IF NOT EXISTS user_friends (
			user_id TEXT NOT NULL,
			friend_user_id TEXT NOT NULL,
			created_at TIMESTAMPTZ NOT NULL,
			PRIMARY KEY (user_id, friend_user_id),
			FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
			FOREIGN KEY (friend_user_id) REFERENCES users(id) ON DELETE CASCADE
		);`,
		`CREATE INDEX IF NOT EXISTS idx_user_friends_user ON user_friends(user_id);`,
	}
	for _, statement := range ddl {
		if _, err := db.Exec(statement); err != nil {
			return err
		}
	}
	return nil
}

func seedAdmin(db *sql.DB) error {
	identifier := normalizeIdentifier(getenv("ADMIN_SEED_IDENTIFIER", "admin"))
	password := getenv("ADMIN_SEED_PASSWORD", "admin1234")
	name := getenv("ADMIN_SEED_NAME", "Road Guide Admin")

	var existing string
	err := db.QueryRow(rebindPostgres("SELECT id FROM users WHERE identifier = ?"), identifier).Scan(&existing)
	if err == nil {
		return nil
	}
	if !errors.Is(err, sql.ErrNoRows) {
		return err
	}

	hash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return err
	}
	now := time.Now().UTC()
	_, err = db.Exec(
		rebindPostgres(
			`INSERT INTO users(id, identifier, password_hash, name, role, created_at, updated_at)
		 VALUES(?, ?, ?, ?, ?, ?, ?)`,
		),
		uuid.NewString(), identifier, string(hash), name, roleAdmin, now, now,
	)
	return err
}

func seedPOIs(db *sql.DB) error {
	var count int
	if err := db.QueryRow("SELECT COUNT(*) FROM business_pois").Scan(&count); err != nil {
		return err
	}
	if count > 0 {
		return nil
	}
	now := time.Now().UTC()
	seedRows := []struct {
		Name     string
		Address  string
		Category string
	}{
		{Name: "Road Guide Cafe", Address: "", Category: "Restaurant"},
		{Name: "p", Address: "", Category: "Other"},
		{Name: "Jeju View Hotel", Address: "999 Coastline Ave, Jeju", Category: "Entertainment"},
	}
	for _, row := range seedRows {
		metadataJSON, _ := json.Marshal(map[string]any{"category": row.Category})
		_, err := db.Exec(
			rebindPostgres(
				`INSERT INTO business_pois(id, name, address, description, category, metadata_json, created_at, updated_at)
			 VALUES(?, ?, ?, ?, ?, ?, ?, ?)`,
			),
			uuid.NewString(), row.Name, row.Address, "", row.Category, string(metadataJSON), now, now,
		)
		if err != nil {
			return err
		}
	}
	return nil
}

func (a *app) handleRegister(w http.ResponseWriter, r *http.Request) {
	var body struct {
		Identifier string `json:"identifier"`
		Password   string `json:"password"`
		Name       string `json:"name"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		writeErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	body.Identifier = normalizeIdentifier(body.Identifier)
	body.Name = strings.TrimSpace(body.Name)
	if body.Name == "" {
		body.Name = body.Identifier
	}
	if body.Identifier == "" || body.Password == "" {
		writeErr(w, http.StatusBadRequest, "identifier and password are required")
		return
	}
	if len(body.Identifier) < 2 {
		writeErr(w, http.StatusBadRequest, "identifier must be at least 2 characters")
		return
	}
	if len(body.Password) < 6 {
		writeErr(w, http.StatusBadRequest, "password must be at least 6 characters")
		return
	}

	hash, err := bcrypt.GenerateFromPassword([]byte(body.Password), bcrypt.DefaultCost)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to hash password")
		return
	}
	now := time.Now().UTC()
	newUser := user{
		ID:         uuid.NewString(),
		Identifier: body.Identifier,
		Name:       body.Name,
		Role:       roleVisitor,
		CreatedAt:  now,
		UpdatedAt:  now,
	}
	_, err = a.exec(
		`INSERT INTO users(id, identifier, password_hash, name, role, created_at, updated_at)
		 VALUES(?, ?, ?, ?, ?, ?, ?)`,
		newUser.ID, newUser.Identifier, string(hash), newUser.Name, newUser.Role, newUser.CreatedAt, newUser.UpdatedAt,
	)
	if err != nil {
		if strings.Contains(strings.ToLower(err.Error()), "unique") {
			writeErr(w, http.StatusConflict, "identifier already registered")
			return
		}
		writeErr(w, http.StatusInternalServerError, "failed to create user")
		return
	}
	token, err := a.issueToken(newUser)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to issue token")
		return
	}
	writeJSON(w, http.StatusCreated, map[string]any{
		"user":  newUser,
		"token": token,
	})
}

func (a *app) handleLogin(w http.ResponseWriter, r *http.Request) {
	var body struct {
		Identifier string `json:"identifier"`
		Password   string `json:"password"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		writeErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	body.Identifier = normalizeIdentifier(body.Identifier)
	if body.Identifier == "" || body.Password == "" {
		writeErr(w, http.StatusBadRequest, "identifier and password are required")
		return
	}

	var u user
	var passwordHash string
	err := a.queryRow(
		`SELECT id, identifier, name, role, password_hash, created_at, updated_at
		 FROM users WHERE identifier = ?`,
		body.Identifier,
	).Scan(&u.ID, &u.Identifier, &u.Name, &u.Role, &passwordHash, &u.CreatedAt, &u.UpdatedAt)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			writeErr(w, http.StatusUnauthorized, "invalid credentials")
			return
		}
		writeErr(w, http.StatusInternalServerError, "failed to fetch user")
		return
	}
	if bcrypt.CompareHashAndPassword([]byte(passwordHash), []byte(body.Password)) != nil {
		writeErr(w, http.StatusUnauthorized, "invalid credentials")
		return
	}
	token, err := a.issueToken(u)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to issue token")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{
		"user":  u,
		"token": token,
	})
}

func (a *app) handleMe(w http.ResponseWriter, r *http.Request) {
	u := userFromContext(r)
	writeJSON(w, http.StatusOK, map[string]any{"user": u})
}

func (a *app) handleClaimStatus(w http.ResponseWriter, r *http.Request) {
	u := userFromContext(r)
	poiID := chi.URLParam(r, "poiID")
	owned, err := a.userOwnsPOI(u.ID, poiID)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to verify ownership")
		return
	}

	if u.Role == roleBusiness && owned {
		writeJSON(w, http.StatusOK, map[string]any{
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
	if err := a.queryRow(
		`SELECT COUNT(*) FROM business_registration_requests
		 WHERE user_id = ? AND poi_id = ? AND status = 'pending'`,
		u.ID, poiID,
	).Scan(&hasPending); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to query registration request")
		return
	}

	status := "not_assigned"
	if hasPending > 0 {
		status = "pending_registration"
	}

	writeJSON(w, http.StatusOK, map[string]any{
		"status":               status,
		"canEditBusiness":      false,
		"message":              "Please complete your business registration.",
		"registrationGuidance": a.claimGuidance,
	})
}

func (a *app) handleCreateClaimRequest(w http.ResponseWriter, r *http.Request) {
	u := userFromContext(r)
	poiID := chi.URLParam(r, "poiID")

	if _, err := a.mustGetPOI(poiID); err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			writeErr(w, http.StatusNotFound, "poi not found")
			return
		}
		writeErr(w, http.StatusInternalServerError, "failed to load poi")
		return
	}

	var body struct {
		Message string `json:"message"`
	}
	_ = json.NewDecoder(r.Body).Decode(&body)

	var hasPending int
	if err := a.queryRow(
		`SELECT COUNT(*) FROM business_registration_requests
		 WHERE user_id = ? AND poi_id = ? AND status = 'pending'`,
		u.ID, poiID,
	).Scan(&hasPending); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to query request")
		return
	}
	if hasPending > 0 {
		writeErr(w, http.StatusConflict, "pending request already exists")
		return
	}

	now := time.Now().UTC()
	id := uuid.NewString()
	_, err := a.exec(
		`INSERT INTO business_registration_requests(
			 id, user_id, poi_id, status, message, created_at, updated_at
		 ) VALUES(?, ?, ?, 'pending', ?, ?, ?)`,
		id, u.ID, poiID, strings.TrimSpace(body.Message), now, now,
	)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to create request")
		return
	}
	writeJSON(w, http.StatusCreated, map[string]any{
		"id":      id,
		"status":  "pending",
		"message": "Your request has been submitted for admin review.",
	})
}

func (a *app) handleResolveBusinessPOI(w http.ResponseWriter, r *http.Request) {
	u := userFromContext(r)
	var body struct {
		ExternalRef string   `json:"externalRef"`
		Name        string   `json:"name"`
		Address     string   `json:"address"`
		Category    string   `json:"category"`
		Latitude    *float64 `json:"latitude"`
		Longitude   *float64 `json:"longitude"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		writeErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	externalRef := strings.TrimSpace(body.ExternalRef)
	if externalRef == "" {
		writeErr(w, http.StatusBadRequest, "externalRef is required")
		return
	}
	name := strings.TrimSpace(body.Name)
	if name == "" {
		name = "Unnamed Place"
	}
	address := strings.TrimSpace(body.Address)

	respond := func(poiID string, created bool, status int) {
		poi, getErr := a.mustGetPOI(poiID)
		if getErr != nil {
			writeErr(w, http.StatusInternalServerError, "failed to load poi")
			return
		}
		canEdit := false
		if u.Role == roleBusiness {
			owned, err := a.userOwnsPOI(u.ID, poiID)
			if err != nil {
				writeErr(w, http.StatusInternalServerError, "failed to verify ownership")
				return
			}
			canEdit = owned
		}
		writeJSON(w, status, map[string]any{
			"poi":               poi,
			"created":           created,
			"canEditBusiness":   canEdit,
		})
	}

	var existingID string
	err := a.queryRow("SELECT id FROM business_pois WHERE external_ref = ?", externalRef).Scan(&existingID)
	if err == nil {
		if mergeErr := a.mergePOICategoryIfEmpty(existingID, body.Category); mergeErr != nil {
			writeErr(w, http.StatusInternalServerError, "failed to update poi category")
			return
		}
		respond(existingID, false, http.StatusOK)
		return
	}
	if !errors.Is(err, sql.ErrNoRows) {
		writeErr(w, http.StatusInternalServerError, "failed to query poi")
		return
	}

	if u.Role == roleBusiness {
		if ownedID, ok, findErr := a.findOwnedPOINearUser(u.ID, body.Latitude, body.Longitude, name); findErr != nil {
			writeErr(w, http.StatusInternalServerError, "failed to match assigned poi")
			return
		} else if ok {
			_, _ = a.exec(
				`UPDATE business_pois SET external_ref = ?, updated_at = ? WHERE id = ?`,
				externalRef, time.Now().UTC(), ownedID,
			)
			respond(ownedID, false, http.StatusOK)
			return
		}
	}

	now := time.Now().UTC()
	id := uuid.NewString()
	category := normalizedCategory(body.Category)
	metadataJSON, _ := json.Marshal(metadataWithCategory(nil, category))
	_, err = a.exec(
		`INSERT INTO business_pois(id, name, address, description, category, metadata_json, external_ref, latitude, longitude, created_at, updated_at)
		 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
		 ON CONFLICT (external_ref) WHERE external_ref IS NOT NULL DO NOTHING`,
		id, name, address, "", category, string(metadataJSON), externalRef, body.Latitude, body.Longitude, now, now,
	)
	if err != nil {
		log.Printf("resolve poi insert failed: %v", err)
		writeErr(w, http.StatusInternalServerError, "failed to create poi")
		return
	}

	var resolvedID string
	if err := a.queryRow("SELECT id FROM business_pois WHERE external_ref = ?", externalRef).Scan(&resolvedID); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to resolve poi")
		return
	}
	respond(resolvedID, resolvedID == id, http.StatusCreated)
}

func (a *app) handleListMyBusinessPOIs(w http.ResponseWriter, r *http.Request) {
	u := userFromContext(r)
	poiIDs, err := a.listAssignedPOIIDs(u.ID)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to list assigned pois")
		return
	}
	pois := make([]map[string]any, 0, len(poiIDs))
	for _, poiID := range poiIDs {
		poi, getErr := a.mustGetPOI(poiID)
		if getErr != nil {
			if errors.Is(getErr, sql.ErrNoRows) {
				continue
			}
			writeErr(w, http.StatusInternalServerError, "failed to load poi")
			return
		}
		pois = append(pois, poi)
	}
	writeJSON(w, http.StatusOK, map[string]any{"pois": pois})
}

func (a *app) handleGetBusinessPOI(w http.ResponseWriter, r *http.Request) {
	u := userFromContext(r)
	poiID := chi.URLParam(r, "poiID")
	if ok, err := a.canAccessPOI(u, poiID); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed auth check")
		return
	} else if !ok {
		writeErr(w, http.StatusForbidden, "you do not have access to this business")
		return
	}

	poi, err := a.mustGetPOI(poiID)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			writeErr(w, http.StatusNotFound, "poi not found")
			return
		}
		writeErr(w, http.StatusInternalServerError, "failed to load poi")
		return
	}
	media, err := a.listPOIMedia(poiID)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to load media")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{
		"poi":   poi,
		"media": media,
	})
}

func (a *app) handleUpdateBusinessPOI(w http.ResponseWriter, r *http.Request) {
	u := userFromContext(r)
	poiID := chi.URLParam(r, "poiID")
	if ok, err := a.canAccessPOI(u, poiID); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed auth check")
		return
	} else if !ok {
		writeErr(w, http.StatusForbidden, "you do not have access to this business")
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
		writeErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	if body.Metadata == nil {
		body.Metadata = map[string]any{}
	}
	category := normalizedCategory(body.Category)
	if category == "" {
		category = categoryFromMetadata(body.Metadata)
	}
	if category != "" {
		body.Metadata["category"] = category
	}
	metadataJSON, _ := json.Marshal(body.Metadata)
	if body.Name == "" || body.Address == "" {
		writeErr(w, http.StatusBadRequest, "name and address are required")
		return
	}
	_, err := a.exec(
		`UPDATE business_pois
		 SET name = ?, address = ?, description = ?, category = ?, metadata_json = ?, updated_at = ?
		 WHERE id = ?`,
		body.Name, body.Address, body.Description, category, string(metadataJSON), time.Now().UTC(), poiID,
	)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to update poi")
		return
	}
	poi, err := a.mustGetPOI(poiID)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to read updated poi")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"poi": poi})
}

func (a *app) handleUploadPOIMedia(w http.ResponseWriter, r *http.Request) {
	u := userFromContext(r)
	poiID := chi.URLParam(r, "poiID")
	if ok, err := a.canAccessPOI(u, poiID); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed auth check")
		return
	} else if !ok {
		writeErr(w, http.StatusForbidden, "you do not have access to this business")
		return
	}

	if err := r.ParseMultipartForm(20 << 20); err != nil {
		writeErr(w, http.StatusBadRequest, "invalid multipart body")
		return
	}
	kind := strings.TrimSpace(r.FormValue("kind"))
	if kind != "photo" && kind != "panorama" {
		writeErr(w, http.StatusBadRequest, "kind must be photo or panorama")
		return
	}
	caption := strings.TrimSpace(r.FormValue("caption"))
	sortOrder, _ := strconv.Atoi(r.FormValue("sortOrder"))

	file, header, err := r.FormFile("file")
	if err != nil {
		writeErr(w, http.StatusBadRequest, "file is required")
		return
	}
	defer file.Close()

	publicURL, fileSize, err := a.saveUpload(file, header)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to store media")
		return
	}
	mediaStatus := "approved"
	if kind == "panorama" {
		mediaStatus = "pending"
	}
	id := uuid.NewString()
	now := time.Now().UTC()
	_, err = a.exec(
		`INSERT INTO poi_media(
			 id, poi_id, kind, url, caption, sort_order, created_by_user_id, created_at,
			 status, admin_note, views, file_size_bytes
		 ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, '', 0, ?)`,
		id, poiID, kind, publicURL, caption, sortOrder, u.ID, now, mediaStatus, fileSize,
	)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to save media metadata")
		return
	}
	writeJSON(w, http.StatusCreated, map[string]any{
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

func (a *app) handleDeletePOIMedia(w http.ResponseWriter, r *http.Request) {
	u := userFromContext(r)
	poiID := chi.URLParam(r, "poiID")
	mediaID := chi.URLParam(r, "mediaID")

	if ok, err := a.canAccessPOI(u, poiID); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed auth check")
		return
	} else if !ok {
		writeErr(w, http.StatusForbidden, "you do not have access to this business")
		return
	}

	var url string
	err := a.queryRow("SELECT url FROM poi_media WHERE id = ? AND poi_id = ?", mediaID, poiID).Scan(&url)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			writeErr(w, http.StatusNotFound, "media not found")
			return
		}
		writeErr(w, http.StatusInternalServerError, "failed to load media")
		return
	}
	_, err = a.exec("DELETE FROM poi_media WHERE id = ? AND poi_id = ?", mediaID, poiID)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to delete media")
		return
	}
	localPath := strings.TrimPrefix(url, a.publicUploadPrefix+"/")
	if localPath != "" {
		_ = os.Remove(filepath.Join(a.uploadDir, localPath))
	}
	writeJSON(w, http.StatusOK, map[string]any{"status": "deleted"})
}

func (a *app) handleLookupUserProfile(w http.ResponseWriter, r *http.Request) {
	profileID := strings.TrimSpace(chi.URLParam(r, "profileID"))
	if !isValidProfileID(profileID) {
		writeErr(w, http.StatusBadRequest, "invalid profile id")
		return
	}
	profile, err := a.lookupUserProfile(profileID)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			writeErr(w, http.StatusNotFound, "profile not found")
			return
		}
		writeErr(w, http.StatusInternalServerError, "failed to lookup profile")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"profile": profile})
}

func (a *app) handleListFriends(w http.ResponseWriter, r *http.Request) {
	u := userFromContext(r)
	rows, err := a.query(
		`SELECT u.id, u.identifier, u.name, uf.created_at
		 FROM user_friends uf
		 JOIN users u ON u.id = uf.friend_user_id
		 WHERE uf.user_id = ?
		 ORDER BY uf.created_at DESC`,
		u.ID,
	)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to list friends")
		return
	}
	defer rows.Close()
	friends := []map[string]any{}
	for rows.Next() {
		var profileID, identifier, name string
		var createdAt time.Time
		if err := rows.Scan(&profileID, &identifier, &name, &createdAt); err != nil {
			writeErr(w, http.StatusInternalServerError, "failed to scan friends")
			return
		}
		friends = append(friends, map[string]any{
			"profileId":  profileID,
			"identifier": identifier,
			"name":       name,
			"addedAt":    createdAt,
		})
	}
	writeJSON(w, http.StatusOK, map[string]any{"friends": friends})
}

func (a *app) handleAddFriend(w http.ResponseWriter, r *http.Request) {
	u := userFromContext(r)
	var body struct {
		ProfileID string `json:"profileId"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		writeErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	profileID := strings.TrimSpace(body.ProfileID)
	if !isValidProfileID(profileID) {
		writeErr(w, http.StatusBadRequest, "invalid profile id")
		return
	}
	if strings.EqualFold(profileID, u.ID) {
		writeErr(w, http.StatusBadRequest, "cannot add yourself")
		return
	}
	if _, err := a.lookupUserProfile(profileID); err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			writeErr(w, http.StatusNotFound, "profile not found")
			return
		}
		writeErr(w, http.StatusInternalServerError, "failed to lookup profile")
		return
	}
	var existing int
	if err := a.queryRow(
		`SELECT COUNT(*) FROM user_friends WHERE user_id = ? AND friend_user_id = ?`,
		u.ID, profileID,
	).Scan(&existing); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to check friendship")
		return
	}
	if existing > 0 {
		writeErr(w, http.StatusConflict, "already friends")
		return
	}
	now := time.Now().UTC()
	_, err := a.exec(
		`INSERT INTO user_friends(user_id, friend_user_id, created_at)
		 VALUES(?, ?, ?)`,
		u.ID, profileID, now,
	)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to add friend")
		return
	}
	profile, err := a.lookupUserProfile(profileID)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to load friend profile")
		return
	}
	writeJSON(w, http.StatusCreated, map[string]any{"friend": profile})
}

func (a *app) handleRemoveFriend(w http.ResponseWriter, r *http.Request) {
	u := userFromContext(r)
	profileID := strings.TrimSpace(chi.URLParam(r, "profileID"))
	if !isValidProfileID(profileID) {
		writeErr(w, http.StatusBadRequest, "invalid profile id")
		return
	}
	result, err := a.exec(
		`DELETE FROM user_friends WHERE user_id = ? AND friend_user_id = ?`,
		u.ID, profileID,
	)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to remove friend")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		writeErr(w, http.StatusNotFound, "friend not found")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"status": "removed"})
}

func (a *app) lookupUserProfile(profileID string) (map[string]any, error) {
	var identifier, name string
	err := a.queryRow(
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

func (a *app) handleListRegistrationRequests(w http.ResponseWriter, r *http.Request) {
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

	rows, err := a.query(query, args...)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to list requests")
		return
	}
	defer rows.Close()
	response := []map[string]any{}
	for rows.Next() {
		var id, userID, poiID, reqStatus, msg, adminNote string
		var createdAt, updatedAt time.Time
		var userIdentifier, userName, poiName string
		if err := rows.Scan(&id, &userID, &poiID, &reqStatus, &msg, &adminNote, &createdAt, &updatedAt, &userIdentifier, &userName, &poiName); err != nil {
			writeErr(w, http.StatusInternalServerError, "failed to scan requests")
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
	writeJSON(w, http.StatusOK, map[string]any{"requests": response})
}

func (a *app) handleApproveRegistrationRequest(w http.ResponseWriter, r *http.Request) {
	adminUser := userFromContext(r)
	requestID := chi.URLParam(r, "requestID")
	var body struct {
		AssignPoiIDs []string `json:"assignPoiIds"`
		AdminNote    string   `json:"adminNote"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		writeErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	var reqUserID, reqPOIID, status string
	err := a.queryRow(
		`SELECT user_id, poi_id, status FROM business_registration_requests WHERE id = ?`,
		requestID,
	).Scan(&reqUserID, &reqPOIID, &status)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			writeErr(w, http.StatusNotFound, "request not found")
			return
		}
		writeErr(w, http.StatusInternalServerError, "failed to fetch request")
		return
	}
	if status != "pending" {
		writeErr(w, http.StatusConflict, "request already processed")
		return
	}
	assignments := uniqueStrings(append(body.AssignPoiIDs, reqPOIID))
	now := time.Now().UTC()

	tx, err := a.db.Begin()
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to start transaction")
		return
	}
	defer tx.Rollback()

	if _, err := txExec(tx, "UPDATE users SET role = ?, updated_at = ? WHERE id = ?", roleBusiness, now, reqUserID); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to update role")
		return
	}
	for _, poiID := range assignments {
		if _, err := txExec(
			tx,
			`INSERT INTO business_user_pois(user_id, poi_id, created_at)
			 VALUES(?, ?, ?)
			 ON CONFLICT(user_id, poi_id) DO NOTHING`,
			reqUserID, poiID, now,
		); err != nil {
			writeErr(w, http.StatusInternalServerError, "failed to assign poi")
			return
		}
	}
	if _, err := txExec(
		tx,
		`UPDATE business_registration_requests
		 SET status = 'approved', admin_note = ?, processed_by_user_id = ?, updated_at = ?
		 WHERE id = ?`,
		strings.TrimSpace(body.AdminNote), adminUser.ID, now, requestID,
	); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to update request status")
		return
	}
	if err := tx.Commit(); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to commit")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"status": "approved"})
}

func (a *app) handleRejectRegistrationRequest(w http.ResponseWriter, r *http.Request) {
	adminUser := userFromContext(r)
	requestID := chi.URLParam(r, "requestID")
	var body struct {
		AdminNote string `json:"adminNote"`
	}
	_ = json.NewDecoder(r.Body).Decode(&body)
	result, err := a.exec(
		`UPDATE business_registration_requests
		 SET status = 'rejected', admin_note = ?, processed_by_user_id = ?, updated_at = ?
		 WHERE id = ? AND status = 'pending'`,
		strings.TrimSpace(body.AdminNote), adminUser.ID, time.Now().UTC(), requestID,
	)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to reject request")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		writeErr(w, http.StatusNotFound, "pending request not found")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"status": "rejected"})
}

func (a *app) handleListUsers(w http.ResponseWriter, _ *http.Request) {
	rows, err := a.query(
		`SELECT id, identifier, name, role, created_at, updated_at
		 FROM users ORDER BY created_at DESC`,
	)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to list users")
		return
	}
	defer rows.Close()
	users := []map[string]any{}
	for rows.Next() {
		var u user
		if err := rows.Scan(&u.ID, &u.Identifier, &u.Name, &u.Role, &u.CreatedAt, &u.UpdatedAt); err != nil {
			writeErr(w, http.StatusInternalServerError, "failed to scan users")
			return
		}
		assignments, err := a.listAssignedPOIIDs(u.ID)
		if err != nil {
			writeErr(w, http.StatusInternalServerError, "failed to scan assignments")
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
	writeJSON(w, http.StatusOK, map[string]any{"users": users})
}

func (a *app) handleSetUserRole(w http.ResponseWriter, r *http.Request) {
	userID := chi.URLParam(r, "userID")
	var body struct {
		Role string `json:"role"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		writeErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	if body.Role != roleVisitor && body.Role != roleBusiness && body.Role != roleAdmin {
		writeErr(w, http.StatusBadRequest, "invalid role")
		return
	}
	result, err := a.exec("UPDATE users SET role = ?, updated_at = ? WHERE id = ?", body.Role, time.Now().UTC(), userID)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to update role")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		writeErr(w, http.StatusNotFound, "user not found")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"status": "updated"})
}

func (a *app) handleSetUserAssignments(w http.ResponseWriter, r *http.Request) {
	userID := chi.URLParam(r, "userID")
	var body struct {
		PoiIDs []string `json:"poiIds"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		writeErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	tx, err := a.db.Begin()
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to begin transaction")
		return
	}
	defer tx.Rollback()
	if _, err := txExec(tx, "DELETE FROM business_user_pois WHERE user_id = ?", userID); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to clear assignments")
		return
	}
	now := time.Now().UTC()
	for _, poiID := range uniqueStrings(body.PoiIDs) {
		if poiID == "" {
			continue
		}
		if _, err := txExec(
			tx,
			`INSERT INTO business_user_pois(user_id, poi_id, created_at)
			 VALUES(?, ?, ?)`,
			userID, poiID, now,
		); err != nil {
			writeErr(w, http.StatusInternalServerError, "failed to create assignment")
			return
		}
	}
	if err := tx.Commit(); err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to commit assignments")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"status": "updated"})
}

func (a *app) handleListBusinessPOIs(w http.ResponseWriter, _ *http.Request) {
	rows, err := a.query(
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
		writeErr(w, http.StatusInternalServerError, "failed to list pois")
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
			writeErr(w, http.StatusInternalServerError, "failed to scan pois")
			return
		}
		entry := map[string]any{
			"id":            id,
			"name":          name,
			"address":       address,
			"description":   description,
			"category":      normalizedCategory(category),
			"metadata":      decodeMap(metadataJSON),
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
		pois = append(pois, withPOICategory(entry))
	}
	writeJSON(w, http.StatusOK, map[string]any{"pois": pois})
}

func (a *app) handleCreateBusinessPOI(w http.ResponseWriter, r *http.Request) {
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
		writeErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	name := strings.TrimSpace(body.Name)
	address := strings.TrimSpace(body.Address)
	if name == "" || address == "" {
		writeErr(w, http.StatusBadRequest, "name and address are required")
		return
	}
	externalRef := strings.TrimSpace(body.ExternalRef)
	if externalRef == "" {
		externalRef = "admin:" + uuid.NewString()
	}
	metadata := map[string]any{}
	category := normalizedCategory(body.Category)
	if category != "" {
		metadata["category"] = category
	}
	metadataJSON, _ := json.Marshal(metadata)
	now := time.Now().UTC()
	id := uuid.NewString()
	_, err := a.exec(
		`INSERT INTO business_pois(id, name, address, description, category, metadata_json, external_ref, latitude, longitude, created_at, updated_at)
		 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
		id, name, address, strings.TrimSpace(body.Description), category, string(metadataJSON),
		externalRef, body.Latitude, body.Longitude, now, now,
	)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to create poi")
		return
	}
	poi, err := a.mustGetPOI(id)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to load created poi")
		return
	}
	writeJSON(w, http.StatusCreated, map[string]any{"poi": poi})
}

func (a *app) handleGetPlaceDetail(w http.ResponseWriter, r *http.Request) {
	externalRef := strings.TrimSpace(r.URL.Query().Get("externalRef"))
	if externalRef == "" {
		writeErr(w, http.StatusBadRequest, "externalRef is required")
		return
	}

	var poiID string
	err := a.queryRow("SELECT id FROM business_pois WHERE external_ref = ?", externalRef).Scan(&poiID)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			writeJSON(w, http.StatusOK, map[string]any{"hasBusinessData": false})
			return
		}
		writeErr(w, http.StatusInternalServerError, "failed to resolve place")
		return
	}

	poi, err := a.mustGetPOI(poiID)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to read business place")
		return
	}
	photos, err := a.listApprovedPhotos(poiID)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to list photos")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{
		"hasBusinessData": true,
		"poi":             poi,
		"photos":          photos,
	})
}

func (a *app) handleListPlacePanoramas(w http.ResponseWriter, r *http.Request) {
	externalRef := strings.TrimSpace(r.URL.Query().Get("externalRef"))
	if externalRef == "" {
		writeErr(w, http.StatusBadRequest, "externalRef is required")
		return
	}

	var poiID string
	err := a.queryRow("SELECT id FROM business_pois WHERE external_ref = ?", externalRef).Scan(&poiID)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			writeJSON(w, http.StatusOK, map[string]any{"poiId": "", "panoramas": []any{}})
			return
		}
		writeErr(w, http.StatusInternalServerError, "failed to resolve place")
		return
	}

	panoramas, err := a.listApprovedPanoramas(poiID)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to list panoramas")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{
		"poiId":     poiID,
		"panoramas": panoramas,
	})
}

func (a *app) listApprovedPhotos(poiID string) ([]map[string]any, error) {
	rows, err := a.query(
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

func (a *app) listApprovedPanoramas(poiID string) ([]map[string]any, error) {
	rows, err := a.query(
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

func (a *app) handleListPanoramas(w http.ResponseWriter, r *http.Request) {
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

	rows, err := a.query(query, args...)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to list panoramas")
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
			writeErr(w, http.StatusInternalServerError, "failed to scan panorama")
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
			"status":        panoramaStatusLabel(status),
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
		writeErr(w, http.StatusInternalServerError, "failed to iterate panoramas")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"items": items})
}

func (a *app) handleApprovePanorama(w http.ResponseWriter, r *http.Request) {
	mediaID := chi.URLParam(r, "mediaID")
	result, err := a.exec(
		`UPDATE poi_media SET status = 'approved', admin_note = ''
		 WHERE id = ? AND kind = 'panorama' AND status = 'pending'`,
		mediaID,
	)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to approve panorama")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		writeErr(w, http.StatusNotFound, "pending panorama not found")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"status": "approved"})
}

func (a *app) handleRejectPanorama(w http.ResponseWriter, r *http.Request) {
	mediaID := chi.URLParam(r, "mediaID")
	var body struct {
		AdminNote string `json:"adminNote"`
	}
	_ = json.NewDecoder(r.Body).Decode(&body)
	note := strings.TrimSpace(body.AdminNote)
	if note == "" {
		note = "Rejected by admin."
	}
	result, err := a.exec(
		`UPDATE poi_media SET status = 'rejected', admin_note = ?
		 WHERE id = ? AND kind = 'panorama' AND status = 'pending'`,
		note, mediaID,
	)
	if err != nil {
		writeErr(w, http.StatusInternalServerError, "failed to reject panorama")
		return
	}
	affected, _ := result.RowsAffected()
	if affected == 0 {
		writeErr(w, http.StatusNotFound, "pending panorama not found")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"status": "rejected"})
}

func panoramaStatusLabel(status string) string {
	switch strings.ToLower(strings.TrimSpace(status)) {
	case "approved":
		return "Approved"
	case "rejected":
		return "Rejected"
	default:
		return "Pending"
	}
}

func (a *app) saveUpload(file multipart.File, header *multipart.FileHeader) (string, int64, error) {
	ext := strings.ToLower(filepath.Ext(header.Filename))
	if ext == "" {
		ext = ".bin"
	}
	name := uuid.NewString() + ext
	targetPath := filepath.Join(a.uploadDir, name)
	out, err := os.Create(targetPath)
	if err != nil {
		return "", 0, err
	}
	written, err := io.Copy(out, file)
	out.Close()
	if err != nil {
		return "", 0, err
	}
	return strings.TrimSuffix(a.publicUploadPrefix, "/") + "/" + name, written, nil
}

func (a *app) mustGetPOI(poiID string) (map[string]any, error) {
	var id, name, address, description, category, metadataJSON, externalRef sql.NullString
	var latitude, longitude sql.NullFloat64
	var createdAt, updatedAt time.Time
	err := a.queryRow(
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
		"category":    normalizedCategory(category.String),
		"metadata":    decodeMap(metadataJSON.String),
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
	return withPOICategory(result), nil
}

func (a *app) listPOIMedia(poiID string) ([]map[string]any, error) {
	rows, err := a.query(
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

func (a *app) userOwnsPOI(userID, poiID string) (bool, error) {
	var count int
	err := a.queryRow(
		`SELECT COUNT(*) FROM business_user_pois WHERE user_id = ? AND poi_id = ?`,
		userID, poiID,
	).Scan(&count)
	return count > 0, err
}

func (a *app) findOwnedPOINearUser(userID string, lat, lng *float64, name string) (string, bool, error) {
	if lat == nil || lng == nil {
		return "", false, nil
	}
	rows, err := a.query(
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
	normalizedName := normalizePlaceName(name)
	for rows.Next() {
		var poiID, poiName string
		var poiLat, poiLng sql.NullFloat64
		if err := rows.Scan(&poiID, &poiName, &poiLat, &poiLng); err != nil {
			return "", false, err
		}
		if !poiLat.Valid || !poiLng.Valid {
			continue
		}
		if mathAbs(poiLat.Float64-*lat) > tolerance || mathAbs(poiLng.Float64-*lng) > tolerance {
			continue
		}
		if normalizedName != "" && normalizePlaceName(poiName) != "" &&
			normalizePlaceName(poiName) != normalizedName {
			continue
		}
		return poiID, true, nil
	}
	return "", false, rows.Err()
}

func normalizePlaceName(name string) string {
	return strings.ToLower(strings.TrimSpace(name))
}

func mathAbs(v float64) float64 {
	if v < 0 {
		return -v
	}
	return v
}

func (a *app) canAccessPOI(u user, poiID string) (bool, error) {
	if u.Role == roleAdmin {
		return true, nil
	}
	if u.Role != roleBusiness {
		return false, nil
	}
	return a.userOwnsPOI(u.ID, poiID)
}

func (a *app) listAssignedPOIIDs(userID string) ([]string, error) {
	rows, err := a.query(
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

func (a *app) issueToken(u user) (string, error) {
	now := time.Now().UTC()
	claims := authClaims{
		UserID: u.ID,
		Role:   u.Role,
		RegisteredClaims: jwt.RegisteredClaims{
			Subject:   u.ID,
			Issuer:    "road-guide-backend",
			IssuedAt:  jwt.NewNumericDate(now),
			ExpiresAt: jwt.NewNumericDate(now.Add(72 * time.Hour)),
		},
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(a.jwtSecret)
}

type contextKey string

const userContextKey contextKey = "user"

func (a *app) requireAuth(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		authHeader := strings.TrimSpace(r.Header.Get("Authorization"))
		if !strings.HasPrefix(authHeader, "Bearer ") {
			writeErr(w, http.StatusUnauthorized, "missing bearer token")
			return
		}
		rawToken := strings.TrimSpace(strings.TrimPrefix(authHeader, "Bearer "))
		token, err := jwt.ParseWithClaims(rawToken, &authClaims{}, func(_ *jwt.Token) (any, error) {
			return a.jwtSecret, nil
		})
		if err != nil || !token.Valid {
			writeErr(w, http.StatusUnauthorized, "invalid token")
			return
		}
		claims, ok := token.Claims.(*authClaims)
		if !ok {
			writeErr(w, http.StatusUnauthorized, "invalid token claims")
			return
		}
		var u user
		err = a.queryRow(
			`SELECT id, identifier, name, role, created_at, updated_at
			 FROM users WHERE id = ?`,
			claims.UserID,
		).Scan(&u.ID, &u.Identifier, &u.Name, &u.Role, &u.CreatedAt, &u.UpdatedAt)
		if err != nil {
			writeErr(w, http.StatusUnauthorized, "user not found")
			return
		}
		ctx := context.WithValue(r.Context(), userContextKey, u)
		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

func (a *app) requireRole(role string) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			u := userFromContext(r)
			if u.Role != role {
				writeErr(w, http.StatusForbidden, "forbidden")
				return
			}
			next.ServeHTTP(w, r)
		})
	}
}

func userFromContext(r *http.Request) user {
	if v := r.Context().Value(userContextKey); v != nil {
		if u, ok := v.(user); ok {
			return u
		}
	}
	return user{}
}

func (a *app) exec(query string, args ...any) (sql.Result, error) {
	return a.db.Exec(rebindPostgres(query), args...)
}

func (a *app) query(query string, args ...any) (*sql.Rows, error) {
	return a.db.Query(rebindPostgres(query), args...)
}

func (a *app) queryRow(query string, args ...any) *sql.Row {
	return a.db.QueryRow(rebindPostgres(query), args...)
}

func txExec(tx *sql.Tx, query string, args ...any) (sql.Result, error) {
	return tx.Exec(rebindPostgres(query), args...)
}

func rebindPostgres(query string) string {
	if !strings.Contains(query, "?") {
		return query
	}
	var builder strings.Builder
	builder.Grow(len(query) + 12)
	placeholder := 1
	for _, ch := range query {
		if ch == '?' {
			builder.WriteString("$")
			builder.WriteString(strconv.Itoa(placeholder))
			placeholder++
			continue
		}
		builder.WriteRune(ch)
	}
	return builder.String()
}

func writeErr(w http.ResponseWriter, status int, message string) {
	writeJSON(w, status, map[string]any{"error": message})
}

func writeJSON(w http.ResponseWriter, status int, payload any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(payload)
}

func decodeMap(raw string) map[string]any {
	out := map[string]any{}
	if raw == "" {
		return out
	}
	_ = json.Unmarshal([]byte(raw), &out)
	return out
}

func normalizedCategory(raw string) string {
	return strings.TrimSpace(raw)
}

func categoryFromMetadata(metadata map[string]any) string {
	if metadata == nil {
		return ""
	}
	raw, _ := metadata["category"].(string)
	return normalizedCategory(raw)
}

func metadataWithCategory(existing map[string]any, category string) map[string]any {
	meta := existing
	if meta == nil {
		meta = map[string]any{}
	}
	if category = normalizedCategory(category); category != "" {
		meta["category"] = category
	}
	return meta
}

func withPOICategory(poi map[string]any) map[string]any {
	if cat, ok := poi["category"].(string); ok && normalizedCategory(cat) != "" {
		poi["category"] = normalizedCategory(cat)
		return poi
	}
	metadata, _ := poi["metadata"].(map[string]any)
	poi["category"] = categoryFromMetadata(metadata)
	return poi
}

func (a *app) mergePOICategoryIfEmpty(poiID, category string) error {
	category = normalizedCategory(category)
	if category == "" {
		return nil
	}
	var existingCategory, metadataJSON string
	if err := a.queryRow(
		`SELECT category, metadata_json FROM business_pois WHERE id = ?`,
		poiID,
	).Scan(&existingCategory, &metadataJSON); err != nil {
		return err
	}
	if normalizedCategory(existingCategory) != "" {
		return nil
	}
	meta := decodeMap(metadataJSON)
	meta["category"] = category
	encoded, _ := json.Marshal(meta)
	_, err := a.exec(
		`UPDATE business_pois SET category = ?, metadata_json = ?, updated_at = ? WHERE id = ?`,
		category, string(encoded), time.Now().UTC(), poiID,
	)
	return err
}

func uniqueStrings(items []string) []string {
	seen := map[string]struct{}{}
	result := make([]string, 0, len(items))
	for _, item := range items {
		item = strings.TrimSpace(item)
		if item == "" {
			continue
		}
		if _, ok := seen[item]; ok {
			continue
		}
		seen[item] = struct{}{}
		result = append(result, item)
	}
	return result
}

func getenv(name, fallback string) string {
	if value := strings.TrimSpace(os.Getenv(name)); value != "" {
		return value
	}
	return fallback
}

func normalizeIdentifier(value string) string {
	return strings.ToLower(strings.TrimSpace(value))
}
