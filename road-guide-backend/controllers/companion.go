package controllers

import (
	"net/http"

	"ROAD-GUIDE-BACKEND/services"
)

type CompanionController struct {
	svc *services.Services
}

func (c *CompanionController) ListDriverPosts(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleListCompanionDriverPosts(w, r)
}

func (c *CompanionController) CreateDriverPost(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleCreateCompanionDriverPost(w, r)
}

func (c *CompanionController) UpdateDriverPost(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleUpdateCompanionDriverPost(w, r)
}

func (c *CompanionController) BookDriverPost(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleBookCompanionDriverPost(w, r)
}

func (c *CompanionController) ListPassengerRequests(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleListCompanionPassengerRequests(w, r)
}

func (c *CompanionController) CreatePassengerRequest(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleCreateCompanionPassengerRequest(w, r)
}

func (c *CompanionController) UpdatePassengerRequest(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleUpdateCompanionPassengerRequest(w, r)
}

func (c *CompanionController) ListSuggestedMatches(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleListCompanionSuggestedMatches(w, r)
}

func (c *AdminController) AdminListCompanionDriverPosts(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminListCompanionDriverPosts(w, r)
}

func (c *AdminController) AdminListCompanionPassengerRequests(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminListCompanionPassengerRequests(w, r)
}

func (c *AdminController) AdminListCompanionBookings(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminListCompanionBookings(w, r)
}

func (c *AdminController) AdminListCompanionMatches(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminListCompanionMatches(w, r)
}

func (c *AdminController) AdminCompanionMatchNotify(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminCompanionMatchNotify(w, r)
}

func (c *AdminController) AdminCompanionMatchDismiss(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminCompanionMatchDismiss(w, r)
}

func (c *AdminController) AdminCompanionDriverPostStatus(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminCompanionDriverPostStatus(w, r)
}

func (c *AdminController) AdminCompanionPassengerRequestStatus(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminCompanionPassengerRequestStatus(w, r)
}

func (c *AdminController) AdminCompanionRecomputeMatches(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAdminCompanionRecomputeMatches(w, r)
}
