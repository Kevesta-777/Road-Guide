package services

import (
	"database/sql"

	"ROAD-GUIDE-BACKEND/config"
	"ROAD-GUIDE-BACKEND/models"
	"ROAD-GUIDE-BACKEND/utils"
)

type Services struct {
	DB            *sql.DB
	Config        *config.Config
	ClaimGuidance models.ClaimGuidance
}

func New(cfg *config.Config, db *sql.DB) *Services {
	return &Services{
		DB:            db,
		Config:        cfg,
		ClaimGuidance: cfg.ClaimGuidance,
	}
}

func (s *Services) Exec(query string, args ...any) (sql.Result, error) {
	return s.DB.Exec(utils.RebindPostgres(query), args...)
}

func (s *Services) Query(query string, args ...any) (*sql.Rows, error) {
	return s.DB.Query(utils.RebindPostgres(query), args...)
}

func (s *Services) QueryRow(query string, args ...any) *sql.Row {
	return s.DB.QueryRow(utils.RebindPostgres(query), args...)
}
