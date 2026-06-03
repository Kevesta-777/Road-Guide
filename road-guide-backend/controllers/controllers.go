package controllers

import "ROAD-GUIDE-BACKEND/services"

type Controllers struct {
	Auth         *AuthController
	Business     *BusinessController
	Admin        *AdminController
	Friend       *FriendController
	Place        *PlaceController
	Companion    *CompanionController
	Subscription *SubscriptionController
}

func New(svc *services.Services) *Controllers {
	return &Controllers{
		Auth:         &AuthController{svc: svc},
		Business:     &BusinessController{svc: svc},
		Admin:        &AdminController{svc: svc},
		Friend:       &FriendController{svc: svc},
		Place:        &PlaceController{svc: svc},
		Companion:    &CompanionController{svc: svc},
		Subscription: &SubscriptionController{svc: svc},
	}
}
