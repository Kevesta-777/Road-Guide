package controllers

import (
	"net/http"

	"ROAD-GUIDE-BACKEND/services"
)

type AuthController struct {
	svc *services.Services
}

func (c *AuthController) Register(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleRegister(w, r)
}

func (c *AuthController) Login(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleLogin(w, r)
}

func (c *AuthController) Me(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleMe(w, r)
}
