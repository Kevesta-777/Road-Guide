package controllers

import (
	"net/http"

	"ROAD-GUIDE-BACKEND/services"
)

type FriendController struct {
	svc *services.Services
}

func (c *FriendController) LookupUserProfile(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleLookupUserProfile(w, r)
}

func (c *FriendController) ListFriends(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleListFriends(w, r)
}

func (c *FriendController) AddFriend(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleAddFriend(w, r)
}

func (c *FriendController) RemoveFriend(w http.ResponseWriter, r *http.Request) {
	c.svc.HandleRemoveFriend(w, r)
}
