package services

import (
	"database/sql"
)

func Migrate(db *sql.DB) error {
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
	ddl = append(ddl, companionMigrateDDL()...)
	ddl = append(ddl, subscriptionMigrateDDL()...)
	for _, statement := range ddl {
		if _, err := db.Exec(statement); err != nil {
			return err
		}
	}
	return nil
}
