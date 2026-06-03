package middleware

import (
	"net/http"

	"ROAD-GUIDE-BACKEND/models"
)

type ContextKey string

const UserContextKey ContextKey = "user"

func UserFromContext(r *http.Request) models.User {
	if v := r.Context().Value(UserContextKey); v != nil {
		if u, ok := v.(models.User); ok {
			return u
		}
	}
	return models.User{}
}
