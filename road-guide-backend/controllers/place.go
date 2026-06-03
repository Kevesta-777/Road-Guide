package controllers

import (
	"net/http"

	"ROAD-GUIDE-BACKEND/services"
)

type PlaceController struct {
	svc *services.Services
}

func (c *PlaceController) ListPlacePanoramas(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleListPlacePanoramas(w, r)
}

func (c *PlaceController) GetPlaceDetail(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleGetPlaceDetail(w, r)
}
