package controllers

import (
	"net/http"

	"ROAD-GUIDE-BACKEND/services"
)

type SubscriptionController struct {
	svc *services.Services
}

func (c *SubscriptionController) GetStatus(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleGetSubscriptionStatus(w, r)
}

func (c *AdminController) AdminListSubscriptionPlans(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminListSubscriptionPlans(w, r)
}

func (c *AdminController) AdminUpdateSubscriptionPlan(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminUpdateSubscriptionPlan(w, r)
}

func (c *AdminController) AdminListPremiumContent(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminListPremiumContent(w, r)
}

func (c *AdminController) AdminUpdatePremiumContent(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminUpdatePremiumContent(w, r)
}

func (c *AdminController) AdminListUserSubscriptions(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminListUserSubscriptions(w, r)
}

func (c *AdminController) AdminUpsertUserSubscription(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminUpsertUserSubscription(w, r)
}

func (c *AdminController) AdminPatchUserSubscription(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminPatchUserSubscription(w, r)
}
