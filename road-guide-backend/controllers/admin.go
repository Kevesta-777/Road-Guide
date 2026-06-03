package controllers

import (
	"net/http"

	"ROAD-GUIDE-BACKEND/services"
)

type AdminController struct {
	svc *services.Services
}

func (c *AdminController) ListRegistrationRequests(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleListRegistrationRequests(w, r)
}

func (c *AdminController) ApproveRegistrationRequest(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleApproveRegistrationRequest(w, r)
}

func (c *AdminController) RejectRegistrationRequest(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleRejectRegistrationRequest(w, r)
}

func (c *AdminController) ListUsers(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleListUsers(w, r)
}

func (c *AdminController) SetUserRole(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleSetUserRole(w, r)
}

func (c *AdminController) SetUserAssignments(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleSetUserAssignments(w, r)
}

func (c *AdminController) ListBusinessPOIs(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleListBusinessPOIs(w, r)
}

func (c *AdminController) CreateBusinessPOI(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleCreateBusinessPOI(w, r)
}

func (c *AdminController) ListPanoramas(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleListPanoramas(w, r)
}

func (c *AdminController) ApprovePanorama(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleApprovePanorama(w, r)
}

func (c *AdminController) RejectPanorama(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleRejectPanorama(w, r)
}
