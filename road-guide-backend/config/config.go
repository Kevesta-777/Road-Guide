package config

import (
	"ROAD-GUIDE-BACKEND/models"
	"ROAD-GUIDE-BACKEND/utils"

	"github.com/joho/godotenv"
)

type Config struct {
	DatabaseURL        string
	JWTSecret          []byte
	Addr               string
	UploadDir          string
	PublicUploadPrefix string
	ClaimGuidance      models.ClaimGuidance
}

func Load() Config {
	_ = godotenv.Load()
	return Config{
		DatabaseURL:        utils.Getenv("DATABASE_URL", "postgres://postgres:postgres@localhost:5432/road_guide?sslmode=disable"),
		JWTSecret:          []byte(utils.Getenv("JWT_SECRET", "dev-secret-change-me")),
		Addr:               utils.Getenv("APP_ADDR", ":8090"),
		UploadDir:          utils.Getenv("UPLOAD_DIR", "./uploads"),
		PublicUploadPrefix: utils.Getenv("PUBLIC_UPLOAD_PREFIX", "/uploads"),
		ClaimGuidance: models.ClaimGuidance{
			ContactPhone:               utils.Getenv("CLAIM_CONTACT_PHONE", "+18658969348"),
			RegistrationAgentAddress:   utils.Getenv("CLAIM_AGENT_ADDRESS", "Road Guide Support Center, Los Angeles"),
			AvailableRegistrationHours: utils.Getenv("CLAIM_HOURS", "Mon-Fri 09:00-18:00"),
			AdditionalInstructions:     utils.Getenv("CLAIM_INSTRUCTIONS", "Please submit your business license and a valid owner ID."),
		},
	}
}
