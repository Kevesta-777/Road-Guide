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

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"golang.org/x/crypto/bcrypt"
)

func (s *Services) HandleRegister(w http.ResponseWriter, r *http.Request) {
	var body struct {
		Identifier string `json:"identifier"`
		Password   string `json:"password"`
		Name       string `json:"name"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	body.Identifier = utils.NormalizeIdentifier(body.Identifier)
	body.Name = strings.TrimSpace(body.Name)
	if body.Name == "" {
		body.Name = body.Identifier
	}
	if body.Identifier == "" || body.Password == "" {
		utils.WriteErr(w, http.StatusBadRequest, "identifier and password are required")
		return
	}
	if len(body.Identifier) < 2 {
		utils.WriteErr(w, http.StatusBadRequest, "identifier must be at least 2 characters")
		return
	}
	if len(body.Password) < 6 {
		utils.WriteErr(w, http.StatusBadRequest, "password must be at least 6 characters")
		return
	}

	hash, err := bcrypt.GenerateFromPassword([]byte(body.Password), bcrypt.DefaultCost)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to hash password")
		return
	}
	now := time.Now().UTC()
	newUser := models.User{
		ID:         uuid.NewString(),
		Identifier: body.Identifier,
		Name:       body.Name,
		Role:       models.RoleVisitor,
		CreatedAt:  now,
		UpdatedAt:  now,
	}
	_, err = s.Exec(
		`INSERT INTO users(id, identifier, password_hash, name, role, created_at, updated_at)
		 VALUES(?, ?, ?, ?, ?, ?, ?)`,
		newUser.ID, newUser.Identifier, string(hash), newUser.Name, newUser.Role, newUser.CreatedAt, newUser.UpdatedAt,
	)
	if err != nil {
		if strings.Contains(strings.ToLower(err.Error()), "unique") {
			utils.WriteErr(w, http.StatusConflict, "identifier already registered")
			return
		}
		utils.WriteErr(w, http.StatusInternalServerError, "failed to create user")
		return
	}
	token, err := s.issueToken(newUser)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to issue token")
		return
	}
	utils.WriteJSON(w, http.StatusCreated, map[string]any{
		"user":  newUser,
		"token": token,
	})
}

func (s *Services) HandleLogin(w http.ResponseWriter, r *http.Request) {
	var body struct {
		Identifier string `json:"identifier"`
		Password   string `json:"password"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		utils.WriteErr(w, http.StatusBadRequest, "invalid json body")
		return
	}
	body.Identifier = utils.NormalizeIdentifier(body.Identifier)
	if body.Identifier == "" || body.Password == "" {
		utils.WriteErr(w, http.StatusBadRequest, "identifier and password are required")
		return
	}

	var u models.User
	var passwordHash string
	err := s.QueryRow(
		`SELECT id, identifier, name, role, password_hash, created_at, updated_at
		 FROM users WHERE identifier = ?`,
		body.Identifier,
	).Scan(&u.ID, &u.Identifier, &u.Name, &u.Role, &passwordHash, &u.CreatedAt, &u.UpdatedAt)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			utils.WriteErr(w, http.StatusUnauthorized, "invalid credentials")
			return
		}
		utils.WriteErr(w, http.StatusInternalServerError, "failed to fetch user")
		return
	}
	if bcrypt.CompareHashAndPassword([]byte(passwordHash), []byte(body.Password)) != nil {
		utils.WriteErr(w, http.StatusUnauthorized, "invalid credentials")
		return
	}
	token, err := s.issueToken(u)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to issue token")
		return
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{
		"user":  u,
		"token": token,
	})
}

func (s *Services) HandleMe(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	utils.WriteJSON(w, http.StatusOK, map[string]any{"user": u})
}

func (s *Services) issueToken(u models.User) (string, error) {
	now := time.Now().UTC()
	claims := models.AuthClaims{
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
	return token.SignedString(s.Config.JWTSecret)
}

func (s *Services) AuthenticateBearer(authHeader string) (models.User, int, string) {
	authHeader = strings.TrimSpace(authHeader)
	if !strings.HasPrefix(authHeader, "Bearer ") {
		return models.User{}, http.StatusUnauthorized, "missing bearer token"
	}
	rawToken := strings.TrimSpace(strings.TrimPrefix(authHeader, "Bearer "))
	token, err := jwt.ParseWithClaims(rawToken, &models.AuthClaims{}, func(_ *jwt.Token) (any, error) {
		return s.Config.JWTSecret, nil
	})
	if err != nil || !token.Valid {
		return models.User{}, http.StatusUnauthorized, "invalid token"
	}
	claims, ok := token.Claims.(*models.AuthClaims)
	if !ok {
		return models.User{}, http.StatusUnauthorized, "invalid token claims"
	}
	var u models.User
	err = s.QueryRow(
		`SELECT id, identifier, name, role, created_at, updated_at
		 FROM users WHERE id = ?`,
		claims.UserID,
	).Scan(&u.ID, &u.Identifier, &u.Name, &u.Role, &u.CreatedAt, &u.UpdatedAt)
	if err != nil {
		return models.User{}, http.StatusUnauthorized, "user not found"
	}
	return u, 0, ""
}
