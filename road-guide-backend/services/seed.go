package services

import (
	"database/sql"
	"encoding/json"
	"errors"
	"time"

	"ROAD-GUIDE-BACKEND/models"
	"ROAD-GUIDE-BACKEND/utils"

	"github.com/google/uuid"
	"golang.org/x/crypto/bcrypt"
)

func SeedAdmin(db *sql.DB) error {
	identifier := utils.NormalizeIdentifier(utils.Getenv("ADMIN_SEED_IDENTIFIER", "admin"))
	password := utils.Getenv("ADMIN_SEED_PASSWORD", "admin1234")
	name := utils.Getenv("ADMIN_SEED_NAME", "Road Guide Admin")

	var existing string
	err := db.QueryRow(utils.RebindPostgres("SELECT id FROM users WHERE identifier = ?"), identifier).Scan(&existing)
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
		utils.RebindPostgres(
			`INSERT INTO users(id, identifier, password_hash, name, role, created_at, updated_at)
		 VALUES(?, ?, ?, ?, ?, ?, ?)`,
		),
		uuid.NewString(), identifier, string(hash), name, models.RoleAdmin, now, now,
	)
	return err
}

func SeedPOIs(db *sql.DB) error {
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
			utils.RebindPostgres(
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
