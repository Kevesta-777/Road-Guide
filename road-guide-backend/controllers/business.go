package controllers

import (
	"net/http"

	"ROAD-GUIDE-BACKEND/services"
)

type BusinessController struct {
	svc *services.Services
}

func (c *BusinessController) ClaimStatus(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleClaimStatus(w, r)
}

func (c *BusinessController) CreateClaimRequest(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleCreateClaimRequest(w, r)
}

func (c *BusinessController) ResolveBusinessPOI(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleResolveBusinessPOI(w, r)
}

func (c *BusinessController) ListMyBusinessPOIs(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleListMyBusinessPOIs(w, r)
}

func (c *BusinessController) GetBusinessPOI(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleGetBusinessPOI(w, r)
}

func (c *BusinessController) UpdateBusinessPOI(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleUpdateBusinessPOI(w, r)
}

func (c *BusinessController) UploadPOIMedia(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleUploadPOIMedia(w, r)
}

func (c *BusinessController) DeletePOIMedia(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleDeletePOIMedia(w, r)
}
