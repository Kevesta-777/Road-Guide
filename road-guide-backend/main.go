package main

import (
	"database/sql"
	"log"
	"os"

	"ROAD-GUIDE-BACKEND/config"
	"ROAD-GUIDE-BACKEND/controllers"
	"ROAD-GUIDE-BACKEND/routers"
	"ROAD-GUIDE-BACKEND/services"

	_ "github.com/jackc/pgx/v5/stdlib"
)

func main() {
	cfg := config.Load()

	if err := os.MkdirAll(cfg.UploadDir, 0o755); err != nil {
		log.Fatalf("create upload dir: %v", err)
	}

	db, err := sql.Open("pgx", cfg.DatabaseURL)
	if err != nil {
		log.Fatalf("open postgres: %v", err)
	}
	defer db.Close()
	db.SetMaxOpenConns(10)
	db.SetMaxIdleConns(5)

	if err := services.Migrate(db); err != nil {
		log.Fatalf("migrate db: %v", err)
	}
	if err := services.SeedAdmin(db); err != nil {
		log.Fatalf("seed admin: %v", err)
	}
	if err := services.SeedPOIs(db); err != nil {
		log.Fatalf("seed pois: %v", err)
	}
	if err := services.SeedCompanion(db); err != nil {
		log.Fatalf("seed companion: %v", err)
	}
	if err := services.SeedSubscriptions(db); err != nil {
		log.Fatalf("seed subscriptions: %v", err)
	}

	svc := services.New(&cfg, db)
	ctrl := controllers.New(svc)
	r := routers.Setup(cfg, ctrl, svc)

	log.Printf("main api listening on %s", cfg.Addr)
	log.Printf("database url: %s", cfg.DatabaseURL)
	if err := r.Run(cfg.Addr); err != nil {
		log.Fatal(err)
	}
}
