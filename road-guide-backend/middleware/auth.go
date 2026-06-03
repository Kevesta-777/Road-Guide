package middleware

import (
	"context"
	"net/http"

	"ROAD-GUIDE-BACKEND/models"
	"ROAD-GUIDE-BACKEND/utils"

	"github.com/gin-gonic/gin"
)

type BearerAuthenticator interface {
	AuthenticateBearer(authHeader string) (models.User, int, string)
}

func RequireAuth(auth BearerAuthenticator) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, status, message := auth.AuthenticateBearer(c.GetHeader("Authorization"))
		if status != 0 {
			c.AbortWithStatusJSON(status, gin.H{"error": message})
			return
		}
		ctx := context.WithValue(c.Request.Context(), UserContextKey, u)
		ctx = context.WithValue(ctx, utils.GinContextKey, c)
		c.Request = c.Request.WithContext(ctx)
		c.Next()
	}
}

func RequireRole(role string) gin.HandlerFunc {
	return func(c *gin.Context) {
		u := UserFromContext(c.Request)
		if u.Role != role {
			c.AbortWithStatusJSON(http.StatusForbidden, gin.H{"error": "forbidden"})
			return
		}
		c.Next()
	}
}
