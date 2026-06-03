package services

import (
	"database/sql"
	"encoding/json"
	"net/http"
	"strings"
	"time"

	"ROAD-GUIDE-BACKEND/middleware"
	"ROAD-GUIDE-BACKEND/models"
	"ROAD-GUIDE-BACKEND/utils"

	"github.com/google/uuid"
)

func subscriptionMigrateDDL() []string {
	return []string{
		`CREATE TABLE IF NOT EXISTS subscription_plans (
			id TEXT PRIMARY KEY,
			name TEXT NOT NULL,
			plan_type TEXT NOT NULL,
			price_cents INTEGER NOT NULL DEFAULT 0,
			billing_period TEXT NOT NULL DEFAULT 'month',
			description TEXT NOT NULL DEFAULT '',
			features_json JSONB NOT NULL DEFAULT '[]'::jsonb,
			active BOOLEAN NOT NULL DEFAULT true,
			created_at TIMESTAMPTZ NOT NULL,
			updated_at TIMESTAMPTZ NOT NULL
		);`,
		`CREATE TABLE IF NOT EXISTS user_subscriptions (
			id TEXT PRIMARY KEY,
			user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
			plan_id TEXT NOT NULL REFERENCES subscription_plans(id),
			status TEXT NOT NULL DEFAULT 'active',
			started_at TIMESTAMPTZ NOT NULL,
			expires_at TIMESTAMPTZ,
			cancelled_at TIMESTAMPTZ,
			created_at TIMESTAMPTZ NOT NULL,
			updated_at TIMESTAMPTZ NOT NULL
		);`,
		`CREATE INDEX IF NOT EXISTS idx_user_subscriptions_user ON user_subscriptions(user_id);`,
		`CREATE TABLE IF NOT EXISTS premium_content_features (
			id TEXT PRIMARY KEY,
			name TEXT NOT NULL,
			category TEXT NOT NULL,
			enabled BOOLEAN NOT NULL DEFAULT true,
			premium_only BOOLEAN NOT NULL DEFAULT true,
			sort_order INTEGER NOT NULL DEFAULT 0,
			created_at TIMESTAMPTZ NOT NULL,
			updated_at TIMESTAMPTZ NOT NULL
		);`,
		`ALTER TABLE companion_passenger_requests ADD COLUMN IF NOT EXISTS source TEXT NOT NULL DEFAULT 'request';`,
	}
}

func SeedSubscriptions(db *sql.DB) error {
	now := time.Now().UTC()
	var count int
	if err := db.QueryRow(utils.RebindPostgres(`SELECT COUNT(*) FROM subscription_plans`)).Scan(&count); err != nil {
		return err
	}
	if count == 0 {
		freeFeatures, _ := json.Marshal([]map[string]any{{"name": "Basic map navigation", "included": true}})
		premiumFeatures, _ := json.Marshal([]map[string]any{{"name": "Companion Finder driver posting", "included": true}})
		for _, plan := range []struct {
			id, name, planType, billing, description string
			priceCents                               int
			features                                 []byte
		}{
			{"plan-free", "Free", models.SubscriptionPlanFree, "forever", "Basic features", 0, freeFeatures},
			{"plan-premium", "Premium", models.SubscriptionPlanPremium, "month", "Power user features", 999, premiumFeatures},
		} {
			if _, err := db.Exec(utils.RebindPostgres(`
				INSERT INTO subscription_plans(id, name, plan_type, price_cents, billing_period, description, features_json, active, created_at, updated_at)
				VALUES(?,?,?,?,?,?,?,true,?,?)`),
				plan.id, plan.name, plan.planType, plan.priceCents, plan.billing, plan.description, string(plan.features), now, now,
			); err != nil {
				return err
			}
		}
	}
	var contentCount int
	if err := db.QueryRow(utils.RebindPostgres(`SELECT COUNT(*) FROM premium_content_features`)).Scan(&contentCount); err != nil {
		return err
	}
	if contentCount == 0 {
		for _, item := range []struct {
			id, name, category string
			sort               int
		}{
			{"pc-1", "Companion Finder Driver Posting", "Feature", 1},
			{"pc-2", "Offline Map Downloads", "Feature", 2},
		} {
			if _, err := db.Exec(utils.RebindPostgres(`
				INSERT INTO premium_content_features(id, name, category, enabled, premium_only, sort_order, created_at, updated_at)
				VALUES(?,?,?,true,true,?,?,?)`),
				item.id, item.name, item.category, item.sort, now, now,
			); err != nil {
				return err
			}
		}
	}
	return nil
}

type subscriptionSnapshot struct {
	Active       bool
	PlanID       string
	PlanName     string
	PlanType     string
	ExpiresAt    *time.Time
	CanOfferRide bool
}

func (s *Services) userSubscriptionSnapshot(userID string) (subscriptionSnapshot, error) {
	snap := subscriptionSnapshot{PlanID: "plan-free", PlanName: "Free", PlanType: models.SubscriptionPlanFree}
	var subID, planID, planName, planType, status string
	var expiresAt sql.NullTime
	err := s.QueryRow(`
		SELECT s.id, p.id, p.name, p.plan_type, s.status, s.expires_at
		FROM user_subscriptions s
		JOIN subscription_plans p ON p.id = s.plan_id
		WHERE s.user_id = ? AND s.status = ?
		ORDER BY s.started_at DESC LIMIT 1`, userID, models.SubscriptionStatusActive,
	).Scan(&subID, &planID, &planName, &planType, &status, &expiresAt)
	if err == sql.ErrNoRows {
		return snap, nil
	}
	if err != nil {
		return snap, err
	}
	if expiresAt.Valid && expiresAt.Time.Before(time.Now().UTC()) {
		_, _ = s.Exec(`UPDATE user_subscriptions SET status = ?, updated_at = ? WHERE id = ?`, models.SubscriptionStatusExpired, time.Now().UTC(), subID)
		return snap, nil
	}
	snap.Active = planType == models.SubscriptionPlanPremium
	snap.PlanID = planID
	snap.PlanName = planName
	snap.PlanType = planType
	snap.CanOfferRide = snap.Active
	if expiresAt.Valid {
		t := expiresAt.Time.UTC()
		snap.ExpiresAt = &t
	}
	return snap, nil
}

func (s *Services) HandleGetSubscriptionStatus(w http.ResponseWriter, r *http.Request) {
	u := middleware.UserFromContext(r)
	snap, err := s.userSubscriptionSnapshot(u.ID)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to load subscription")
		return
	}
	out := map[string]any{"active": snap.Active, "planId": snap.PlanID, "planName": snap.PlanName, "planType": snap.PlanType, "canOfferRide": snap.CanOfferRide}
	if snap.ExpiresAt != nil {
		out["expiresAt"] = snap.ExpiresAt.Format(time.RFC3339)
	}
	utils.WriteJSON(w, http.StatusOK, out)
}

func (s *Services) HandleAdminListSubscriptionPlans(w http.ResponseWriter, _ *http.Request) {
	rows, err := s.Query(`SELECT p.id, p.name, p.plan_type, p.price_cents, p.billing_period, p.description, p.features_json,
		(SELECT COUNT(*) FROM user_subscriptions us WHERE us.plan_id = p.id AND us.status = 'active') FROM subscription_plans p ORDER BY p.price_cents`)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list plans")
		return
	}
	defer rows.Close()
	items := []map[string]any{}
	for rows.Next() {
		var id, name, planType, billing, description string
		var featuresJSON []byte
		var priceCents, users int
		_ = rows.Scan(&id, &name, &planType, &priceCents, &billing, &description, &featuresJSON, &users)
		features := []map[string]any{}
		_ = json.Unmarshal(featuresJSON, &features)
		items = append(items, map[string]any{"id": id, "name": name, "type": planType, "price": float64(priceCents) / 100, "billingPeriod": billing, "users": users, "description": description, "features": features})
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"items": items})
}

func (s *Services) HandleAdminUpdateSubscriptionPlan(w http.ResponseWriter, r *http.Request) {
	planID := utils.URLParam(r, "planID")
	var body struct {
		Price *float64 `json:"price"`
	}
	_ = json.NewDecoder(r.Body).Decode(&body)
	if body.Price != nil {
		_, _ = s.Exec(`UPDATE subscription_plans SET price_cents = ?, updated_at = ? WHERE id = ?`, int(*body.Price*100), time.Now().UTC(), planID)
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"ok": true})
}

func (s *Services) HandleAdminListPremiumContent(w http.ResponseWriter, _ *http.Request) {
	rows, err := s.Query(`SELECT id, name, category, enabled, premium_only FROM premium_content_features ORDER BY sort_order`)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list content")
		return
	}
	defer rows.Close()
	items := []map[string]any{}
	for rows.Next() {
		var id, name, category string
		var enabled, premiumOnly bool
		_ = rows.Scan(&id, &name, &category, &enabled, &premiumOnly)
		items = append(items, map[string]any{"id": id, "name": name, "category": category, "enabled": enabled, "premiumOnly": premiumOnly})
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"items": items})
}

func (s *Services) HandleAdminUpdatePremiumContent(w http.ResponseWriter, r *http.Request) {
	contentID := utils.URLParam(r, "contentID")
	var body struct {
		Enabled *bool `json:"enabled"`
	}
	_ = json.NewDecoder(r.Body).Decode(&body)
	if body.Enabled != nil {
		_, _ = s.Exec(`UPDATE premium_content_features SET enabled = ?, updated_at = ? WHERE id = ?`, *body.Enabled, time.Now().UTC(), contentID)
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"ok": true})
}

func (s *Services) HandleAdminListUserSubscriptions(w http.ResponseWriter, _ *http.Request) {
	rows, err := s.Query(`SELECT s.id, s.user_id, u.name, u.identifier, p.name, p.plan_type, s.status, s.started_at, s.expires_at, s.created_at
		FROM user_subscriptions s JOIN users u ON u.id = s.user_id JOIN subscription_plans p ON p.id = s.plan_id ORDER BY s.created_at DESC`)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to list subscriptions")
		return
	}
	defer rows.Close()
	items := []map[string]any{}
	for rows.Next() {
		var id, userID, userName, identifier, planName, planType, status string
		var startedAt, createdAt time.Time
		var expiresAt sql.NullTime
		_ = rows.Scan(&id, &userID, &userName, &identifier, &planName, &planType, &status, &startedAt, &expiresAt, &createdAt)
		item := map[string]any{"id": id, "userId": userID, "userName": userName, "identifier": identifier, "planName": planName, "planType": planType, "status": status, "startedAt": startedAt.UTC().Format(time.RFC3339), "createdAt": createdAt.UTC().Format("2006-01-02 03:04 PM")}
		if expiresAt.Valid {
			item["expiresAt"] = expiresAt.Time.UTC().Format(time.RFC3339)
		}
		items = append(items, item)
	}
	utils.WriteJSON(w, http.StatusOK, map[string]any{"items": items})
}

func (s *Services) HandleAdminUpsertUserSubscription(w http.ResponseWriter, r *http.Request) {
	var body struct {
		UserID    string `json:"userId"`
		PlanID    string `json:"planId"`
		ExpiresAt string `json:"expiresAt"`
	}
	_ = json.NewDecoder(r.Body).Decode(&body)
	now := time.Now().UTC()
	_, _ = s.Exec(`UPDATE user_subscriptions SET status = ?, cancelled_at = ?, updated_at = ? WHERE user_id = ? AND status = ?`, models.SubscriptionStatusCancelled, now, now, body.UserID, models.SubscriptionStatusActive)
	subID := uuid.NewString()
	var expiresArg any
	if t, err := time.Parse(time.RFC3339, strings.TrimSpace(body.ExpiresAt)); err == nil {
		expiresArg = t.UTC()
	} else if body.PlanID == "plan-premium" {
		expiresArg = now.AddDate(0, 1, 0)
	}
	_, err := s.Exec(`INSERT INTO user_subscriptions(id, user_id, plan_id, status, started_at, expires_at, created_at, updated_at) VALUES(?,?,?,?,?,?,?,?)`,
		subID, body.UserID, body.PlanID, models.SubscriptionStatusActive, now, expiresArg, now, now)
	if err != nil {
		utils.WriteErr(w, http.StatusInternalServerError, "failed to create subscription")
		return
	}
	utils.WriteJSON(w, http.StatusCreated, map[string]any{"id": subID})
}

func (s *Services) HandleAdminPatchUserSubscription(w http.ResponseWriter, r *http.Request) {
	subID := utils.URLParam(r, "subscriptionID")
	var body struct {
		Status string `json:"status"`
	}
	_ = json.NewDecoder(r.Body).Decode(&body)
	_, _ = s.Exec(`UPDATE user_subscriptions SET status = ?, updated_at = ? WHERE id = ?`, strings.TrimSpace(body.Status), time.Now().UTC(), subID)
	utils.WriteJSON(w, http.StatusOK, map[string]any{"ok": true})
}
